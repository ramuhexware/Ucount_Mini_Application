package com.freddieapp.underwriting.config;

import com.freddieapp.underwriting.util.UcsApiUtil;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RestClient / RestTemplate Configuration with Sailpoint Service Integration,
 * TAM Credentials, MacVault Interceptors, and OpenAPI Documentation Tags.
 */
@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${edAdaptorAuthId:sailpoint_user_id}")
    private String edAdaptorAuthId;

    @Value("${edAdaptorAuthPwd:sailpoint_password}")
    private String edAdaptorAuthPwd;

    @Value("${tamCredFileLoc:/config/tam_credentials.properties}")
    private String tamCredFileLoc;

    @Value("${macVaultKeyFilePath:/config/macvault.key}")
    private String macVaultKeyFilePath;

    @Value("${isApiMacVaultEnable:true}")
    private boolean isApiMacVaultEnable;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);

            restTemplate = new RestTemplate(factory);
            logger.info("Calling Sailpoint service with userId: " + edAdaptorAuthId);

            Map<String, String> tamCredentials = UcsApiUtil.getTamCredProperty(tamCredFileLoc);
            logger.info("Calling sailpoint service with tam userName: " + tamCredentials.get(UcsApiUtil.TAM_USERNAME));

            if (isApiMacVaultEnable) {
                logger.info(" macVault Enabled: --------------------------- ");
                // restTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(edAdaptorAuthId, UcsApiUtil.readValueFromMacVaultFile(edAdaptorAuthId, macVaultKeyFilePath))));
                restTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(tamCredentials.get(UcsApiUtil.TAM_USERNAME), tamCredentials.get(UcsApiUtil.TAM_PASSWORD))));
            } else {
                restTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(edAdaptorAuthId, edAdaptorAuthPwd)));
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
            .info(new Info().title("Ucount Application").description("Ucount Application Documentation").version("1.0").license(license))
            .tags(makeTags());
    }

    private List<Tag> makeTags() {
        Map<String, String> tagsMap = Stream.of(new String[][] {
            {"Background", "Rest Api for Background"},
            {"ConsentAgreement", "Rest Api for ConsentAgreement"},
            {"Fees", "Rest Api for Fees"},
            {"File", "Rest Api for File"},
            {"FullApp Background PM", "Rest Api for FullApp Background PM"}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        List<Tag> tags = new ArrayList<>();
        tagsMap.forEach((name, description) -> {
            Tag tag = new Tag();
            tag.setName(name);
            tag.setDescription(description);
            tags.add(tag);
        });
        return tags;
    }
}
