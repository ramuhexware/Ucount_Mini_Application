package com.freddieapp.customerservice.service;

public interface APIService {

    /**
     * This method will call the OIM sync service and save the error in database.
     *
     * @param url - the URL of IM sync
     * @param object - primary key to identify object
     * @param operation - Operation type
     * @param responseType - User response type
     * @param <T> - Type parameter
     */
    <T> void get(String url, Object object, String operation, Class<T> responseType);

    /**
     * Bypasses user synchronization for a specific user ID or system account.
     *
     * @param userId - the user ID to bypass
     * @param reason - reason for bypassing sync
     * @return boolean - true if bypassed successfully
     */
    boolean byPassUserSync(String userId, String reason);
}
