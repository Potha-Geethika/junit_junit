package com.carbo.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;

@Configuration
public class AzureAdConfig {
    @Value ("${user.azure-ad.clientId}")
    private String clientId;

    @Value ("${user.azure-ad.clientSecret}")
    private String clientSecret;

    @Value ("${user.azure-ad.tenantId}")
    private String tenantId;

    @Value ("${user.azure-ad.scopes}")
    private String scopes;

    @Bean
    public ClientSecretCredential clientSecretCredential() {
        return new ClientSecretCredentialBuilder().clientId(clientId).tenantId(tenantId).clientSecret(clientSecret).build();
    }

    @Bean
    public GraphServiceClient graphServiceClient(ClientSecretCredential credential) {
        return new GraphServiceClient(credential, scopes);
    }
}

