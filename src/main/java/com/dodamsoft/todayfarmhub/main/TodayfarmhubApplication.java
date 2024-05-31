package com.dodamsoft.todayfarmhub.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.dodamsoft.todayfarmhub.*")
@EntityScan("com.dodamsoft.todayfarmhub.entity")
public class TodayfarmhubApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TodayfarmhubApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return super.configure(builder);
    }

}
