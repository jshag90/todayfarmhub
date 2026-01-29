package com.dodamsoft.todayfarmhub.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.dodamsoft.todayfarmhub")
@EnableJpaRepositories("com.dodamsoft.todayfarmhub.repository")
@EntityScan("com.dodamsoft.todayfarmhub.entity")
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableScheduling
public class TodayfarmhubApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        java.security.Security.setProperty("networkaddress.cache.ttl", "10");

        SpringApplication.run(TodayfarmhubApplication.class, args);

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return super.configure(builder);
    }

}
