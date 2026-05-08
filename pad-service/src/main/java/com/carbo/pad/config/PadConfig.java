package com.carbo.pad.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PadConfig {
    @Bean(name = IntegrationContextUtils.INTEGRATION_DATATYPE_CHANNEL_MESSAGE_CONVERTER_BEAN_NAME)
    MappingJackson2MessageConverter mappingJackson2MessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
