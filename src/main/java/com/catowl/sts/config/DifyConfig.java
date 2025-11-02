package com.catowl.sts.config;

import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DifyConfig {

    @Value("${dify.api-key}")
    private String apikey;

    @Value("${dify.base-url}")
    private String baseUrl;

    @Bean
    public DifyChatflowClient difyChatflowClient(){
        return DifyClientFactory.createChatWorkflowClient(apikey,baseUrl);
    }
}
