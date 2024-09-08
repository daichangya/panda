## 微信公众号

扫码关注微信公众号，Java码界探秘。
![Java码界探秘](http://www.images.mdan.top/qrcode_for_gh_1e2587cc42b1_258_1587996055777.jpg)

[https://zthinker.com/](https://zthinker.com/)

** 这个项目是一个基于Netty的Java Web服务器，它提供了从简单HTTP服务器到集成Spring Boot等多个版本的演进。以下是对该项目的详细概述：

1.  **项目名称与简介**：
    *   项目名称：panda
    *   简介：一个基于Netty的Java Web服务器，随着版本的迭代，逐步增加了更多功能，如静态文件下载、controller支持、servlet支持以及最终集成Spring Boot等。
2.  **技术栈**：
    *   主要技术：Netty, Spring Boot, Spring Web
    *   Netty：一个高性能、异步事件驱动的NIO框架，用于快速开发可维护的高性能协议服务器和客户端。
    *   Spring Boot：简化了基于Spring的应用开发，通过自动配置和启动类让开发者快速搭建Spring应用。
    *   Spring Web：Spring框架的Web模块，提供了全面的Web开发支持。
3.  **功能与特性**：
    *   支持静态文件下载。
    *   添加了controller和servlet支持，便于开发Web应用。
    *   集成Spring Boot，使得项目可以更容易地部署和运行。
    *   提供了异步处理逻辑和HTTP流事件分块传输的支持，提高了应用的性能和响应能力。
4.  **社区与文档**：
    *   项目托管在GitHub上，便于开发者协作和贡献。
    *   提供了README文件，简要介绍了项目的使用方法和版本历史。
    *   扫码关注微信公众号“Java码界探秘”可能获取更多关于项目和相关技术的信息。
5.  **许可证**：
    *   项目采用Apache-2.0许可证，这是一个开源软件许可证，允许用户自由地使用、修改、分发和再许可软件，但需要遵守一定的条件和限制。
6.  **未来展望**：
    *   随着技术的不断发展和社区的不断贡献，panda项目可能会继续增加新的功能和优化现有功能，以满足更多开发者的需求。

```
外部客户端 --(HTTP请求)--> Netty网络层  
                                |  
                                V  
                  Spring Boot应用层  
                    /             \  
                Controller/    Session管理  
                Servlet         (包括异步处理)  
                    \             /  
                    业务逻辑处理  
                        |  
                        V  
```

# panda 一个 基于Netty的Java Web服务器
0.1
* http协议

1.0
* 一个简单的Http Server

2.0 
* 通讯模型改为netty
* 支持静态文件下载

3.0
* 添加controller支持

4.0
* 添加servlet支持
* 对接spring web

5.0
* 集成spring boot,替换内嵌tomcat
* Netty spring boot Spring Web

6.0.0
* 新建模块 spring-boot-starter-netty

6.0.1
* 添加 channelHandle 线程池

6.0.2
* add session处理逻辑

6.0.3
* add 异步处理逻辑

6.0.4
* add http stream event chunked
