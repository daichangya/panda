####基础了解
* https://www.cnblogs.com/skyessay/p/7461994.html

spring.factories 文件内容如下：其中第二行要是自己的类名全路径

org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.daicy.panda.netty.embedded.ServletWebServerFactoryConfiguration
  
spring.provides 文件内容如下：provides 后面的值是 maven 中项目的 artifactId 值

provides: netty-embed-core http-server

pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>com.daicy.panda</groupId>
		<artifactId>parent</artifactId>
		<version>6.0.0-SNAPSHOT</version>
	</parent>
	<groupId>com.daicy.panda</groupId>
	<artifactId>spring-boot-starter-netty</artifactId>
	<version>6.0.0-SNAPSHOT</version>
	<name>spring-boot-starter-netty</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>com.daicy.panda</groupId>
			<artifactId>http-server</artifactId>
			<version>${parent.version}</version>
		</dependency>
	</dependencies>

</project>

```