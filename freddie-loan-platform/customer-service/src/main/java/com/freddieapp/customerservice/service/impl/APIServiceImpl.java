package com.freddieapp.customerservice.service.impl;

import com.freddieapp.customerservice.client.OAuthTokenCache;
import com.freddieapp.customerservice.entity.CustomerSyncErrLog;
import com.freddieapp.customerservice.exception.UcsApiException;
import com.freddieapp.customerservice.repository.CustomerSyncErrLogRepository;
import com.freddieapp.customerservice.service.APIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
public class APIServiceImpl implements APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIServiceImpl.class);

    private final WebClient webClient;

    @Value("${api.byPassOimSync:false}")
    private boolean byPassOimSync;

    @Value("${api.byPassUser:false}")
    private boolean byPassUser;

    @Value("${api.byPassUserList:SYSTEM,BATCH_USER,ADMIN,TEST_USER}")
    private String byPassUserList;

    @Value("${api.retries.max:3}")
    private int apiRetriesMax;

    private final Duration apiRetriesDelay = Duration.ofSeconds(2);

    public APIServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Autowired
    private CustomerSyncErrLogRepository customerSyncErrLogRepository;

    @Autowired
    private OAuthTokenCache oAuthTokenCache;

    /**
     * This method will call the OIM sync service and save the error in database.
     *
     * @param url - the URL of IM sync
     * @param object - primary key to identify object
     * @param operation - Operation type
     * @param responseType - User response type
     */
    @Override
    @Async("oimDataSyncThreadpool")
    public <T> void get(String url, Object object, String operation, Class<T> responseType) {
        if (!byPassOimSync && !isUserBypassed(object)) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread Interrupted exception " + e);
            }
            LOGGER.info("ORG API: OIM Sync Get service call started... for URL-" + url + " Primary Key-" + object + " Opreation-" + operation + " Thread name " + Thread.currentThread().getName());
            webClient.get()
                    .uri(url)
                    .headers(h -> h.add("Authorization", oAuthTokenCache.getOAuthAccessToken()))
                    .exchangeToMono(clientResponse -> {
                        LOGGER.info("Recived response from OIM Sync application with status code " + clientResponse.statusCode().value());
                        if (clientResponse.statusCode().is4xxClientError()) {
                            throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), "Org API :: URL is wrong ");
                        } else if (clientResponse.statusCode().is5xxServerError()) {
                            throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), " Org API :: Error occured in OIM Sync :: for more details check OIM sync logs");
                        } else {
                            return clientResponse.bodyToMono(responseType);
                        }
                    })
                    .retryWhen(Retry.backoff(apiRetriesMax, apiRetriesDelay).jitter(0d).doAfterRetry(retrySignal -> {
                        LOGGER.info("Retried " + retrySignal.totalRetries());
                    })
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new UcsApiException(HttpStatus.valueOf(500),
                            " Retries exhausted ", retrySignal.failure())))
                    .doOnSuccess(clientResponse -> {
                        LOGGER.info("Received reply from " + url + " Primary Key-" + object + " Opreation-" + operation + " Thread name " + Thread.currentThread().getName() + " with response " + clientResponse);
                    })
                    .doOnError(Throwable.class, (msg) -> {
                        saveToErrorTable(url, object != null ? object.toString() : "N/A", operation, msg);
                        LOGGER.error("Exception while calling get for " + url + " for entity " + (object != null ? object.toString() : "N/A") + " while " + operation + " Exception msg " + msg + " Thread name " + Thread.currentThread().getName());
                    })
                    .subscribe();
        } else {
            LOGGER.info("OIM Sync is not happening because byPassOimSync = " + byPassOimSync);
        }
    }

    /**
     * Bypasses user synchronization for a specific user ID or system account.
     *
     * @param userId - the user ID to bypass
     * @param reason - reason for bypassing sync
     * @return boolean - true if bypassed successfully
     */
    @Override
    public boolean byPassUserSync(String userId, String reason) {
        LOGGER.info("Explicitly bypassing User Sync for User ID: [{}], Reason: [{}]", userId, reason);
        return true;
    }

    /**
     * Helper method to determine if a user or object identifier is bypassed.
     */
    private boolean isUserBypassed(Object userIdentifier) {
        if (byPassUser) {
            return true;
        }
        if (userIdentifier != null && StringUtils.hasText(byPassUserList)) {
            List<String> list = Arrays.asList(byPassUserList.split(","));
            return list.contains(userIdentifier.toString());
        }
        return false;
    }

    /**
     * Saves error details to database table via customerSyncErrLogRepository.
     */
    private void saveToErrorTable(String url, String objectKey, String operation, Throwable error) {
        try {
            if (customerSyncErrLogRepository != null) {
                CustomerSyncErrLog log = CustomerSyncErrLog.builder()
                        .apiEndpoint(url)
                        .errorMessage(error.getMessage())
                        .build();
                customerSyncErrLogRepository.save(log);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to save sync error log into database", ex);
        }
    }
}
