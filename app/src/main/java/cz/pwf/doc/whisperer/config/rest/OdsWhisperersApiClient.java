package cz.pwf.whisperer.config.rest;

import cz.pwf.whisperer.pwf_ods_whisperers_api_client.handler.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Třída pro konfigurace REST API klienta, komunikujícího s DWH REST API.
 */
@Configuration
@ComponentScan("cz.pwf.whisperer.pwf_ods_whisperers_api_client")
public class OdsWhisperersApiClient {
    @Value("${rest.client.pwf_ods_whisperers.url:https://restapidv.pwfdata.corp}")
    private String apiBaseUrl;

    @Value("${rest.client.pwf_ods_whisperers.debugging:true}")
    private boolean apiDebuggingEnabled;

    @Value("${rest.client.pwf_ods_whisperers.username:}")
    private String username;

    @Value("${rest.client.pwf_ods_whisperers.password:}")
    private String password;

    @Bean
    @Primary
    public ApiClient pwfOdsWhisperersApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiBaseUrl);
        apiClient.setDebugging(apiDebuggingEnabled);
        apiClient.setUsername(username);
        apiClient.setPassword(password);
        return apiClient;
    }
}