package com.fetty.studiooffice.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.fetty.studiooffice.*")
@ComponentScan(basePackages = { "com.fetty.studiooffice.*" })
@EntityScan("com.fetty.studiooffice.*")
public class StudioOfficeApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(StudioOfficeApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(StudioOfficeApplication.class, args);
	}

}
