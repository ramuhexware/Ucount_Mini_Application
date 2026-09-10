package com.freddieapp.customerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class Swagger2Config {

    private static final Logger logger = LoggerFactory.getLogger(Swagger2Config.class);

    @Value("${ed.adaptor.auth.id:admin}")
    private String edAdaptorAuthId;

    @Value("${ed.adaptor.auth.pwd:password}")
    private String edAdaptorAuthPwd;

    @Value("${api.macvault.enable:false}")
    private boolean isApiMacVaultEnable;

    @Value("${tam.cred.file.loc:classpath:tam-cred.properties}")
    private String tamCredFileLoc;

    private static final String TAM_USERNAME = "TAM_USERNAME";
    private static final String TAM_PASSWORD = "TAM_PASSWORD";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);

            restTemplate = new RestTemplate(factory);
            logger.info("Calling Sailpoint service with userId:" + edAdaptorAuthId);

            Map<String, String> tamCredentials = getTamCredProperty(tamCredFileLoc);
            logger.info("Calling Sailpoint service with tam userName: " + tamCredentials.get(TAM_USERNAME));

            if (isApiMacVaultEnable) {
                logger.info("------------- macVault Enabled: ---------------------------");
                restTemplate.setInterceptors(Arrays.asList(
                        new BasicAuthenticationInterceptor(tamCredentials.get(TAM_USERNAME), tamCredentials.get(TAM_PASSWORD))
                ));
            } else {
                restTemplate.setInterceptors(Arrays.asList(
                        new BasicAuthenticationInterceptor(edAdaptorAuthId, edAdaptorAuthPwd)
                ));
            }
        } catch (Exception ex) {
            logger.error("Exception creating Rest Template", ex);
        }
        return restTemplate;
    }

    @Bean
    public OpenAPI api() {
        License license = new License();
        license.setName("Apache License 2.0");
        license.setUrl("Apache License 2.0");

        return new OpenAPI()
                .info(new Info()
                        .title("Ucount Application")
                        .description("Ucount Application Documentation")
                        .version("1.0")
                        .license(license))
                .tags(makeTags());
    }

    private List<Tag> makeTags() {
        Map<String, String> tagsMap = Stream.of(new String[][] {
                { "Background", "Rest Api for Background" },
                { "ConsentAgreement", "Rest Api for ConsentAgreement" },
                { "Fees", "Rest Api for Fees" },
                { "File", "Rest Api for File" },
                { "FullApp Background PM", "Rest Api for FullApp Background PM" },
                { "FullApp Cert", "Rest Api for FullApp Cert" },
                { "FullApp Financial Information", "Rest Api for FullApp Financial Information" },
                { "FullApp Privacy Compliance", "Rest Api for FullApp Privacy Compliance" },
                { "FullApp Quality Control", "Rest Api for FullApp Quality Control" },
                { "FullApp Summary", "Rest Api for FullApp Summary" }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        return tagsMap.entrySet().stream()
                .map(entry -> new Tag().name(entry.getKey()).description(entry.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, String> getTamCredProperty(String fileLoc) {
        Map<String, String> creds = new HashMap<>();
        creds.put(TAM_USERNAME, edAdaptorAuthId);
        creds.put(TAM_PASSWORD, edAdaptorAuthPwd);
        return creds;
    }
}
