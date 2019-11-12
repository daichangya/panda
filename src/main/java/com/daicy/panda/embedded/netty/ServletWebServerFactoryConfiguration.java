package com.daicy.panda.embedded.netty;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletWebServerFactoryConfiguration {

	@Configuration
	@ConditionalOnClass({ NettyWebServer.class})
	@ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
	public static class EmbeddedTomcat {

		@Bean
		public NettyServletWebServerFactory nettyServletWebServerFactory() {
			return new NettyServletWebServerFactory();
		}

	}
}
