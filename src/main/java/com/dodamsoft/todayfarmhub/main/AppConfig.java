package com.dodamsoft.todayfarmhub.main;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {

    @Value("${today.farm.orgin.api.url}")
    String orginOpenAPIURL;
}
