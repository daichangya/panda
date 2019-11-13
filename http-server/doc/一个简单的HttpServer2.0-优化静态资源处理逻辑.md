###spring-mvc处理逻辑

	protected void writeContent(Resource resource, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			InputStream in = resource.getInputStream();
			try {
				StreamUtils.copy(in, outputMessage.getBody());
			}
			catch (NullPointerException ex) {
				// ignore, see SPR-13620
			}
			finally {
				try {
					in.close();
				}
				catch (Throwable ex) {
					// ignore, see SPR-12999
				}
			}
		}
		catch (FileNotFoundException ex) {
			// ignore, see SPR-12999
		}
	}
	
	/**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Leaves both streams open when done.
     * @param in the InputStream to copy from
     * @param out the OutputStream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }
	
压测报告1(直接返回字符串)
```
daichangya@daichangya:~$ wrk -t8 -c100 -d10s --latency   http://localhost:8080
Running 10s test @ http://localhost:8080
  8 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.42ms    4.03ms  90.19ms   93.31%
    Req/Sec    27.57k     9.12k   56.47k    74.62%
  Latency Distribution
     50%  326.00us
     75%  657.00us
     90%    3.23ms
     99%   19.74ms
  2198188 requests in 10.05s, 773.56MB read
Requests/sec: 218729.83
Transfer/sec:     76.97MB

```
压测报告2(读取静态文件)
```
daichangya@daichangya:~$ wrk -t8 -c100 -d10s --latency   http://localhost:8080/netty4.html
Running 10s test @ http://localhost:8080/netty4.html
  8 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.11ms    6.78ms 134.71ms   89.03%
    Req/Sec     2.50k   630.03     4.23k    77.50%
  Latency Distribution
     50%    4.21ms
     75%    8.11ms
     90%   13.48ms
     99%   32.52ms
  199248 requests in 10.01s, 68.22MB read
Requests/sec:  19895.54
Transfer/sec:      6.81MB
```