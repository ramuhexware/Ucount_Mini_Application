package com.freddieapp.underwriting.util;

import java.util.HashMap;
import java.util.Map;

public class UcsApiUtil {

    public static final String TAM_USERNAME = "TAM_USERNAME";
    public static final String TAM_PASSWORD = "TAM_PASSWORD";

    public static Map<String, String> getTamCredProperty(String tamCredFileLoc) {
        Map<String, String> creds = new HashMap<>();
        creds.put(TAM_USERNAME, "sailpoint_service_user");
        creds.put(TAM_PASSWORD, "sailpoint_service_password_secret");
        return creds;
    }

    public static String readValueFromMacVaultFile(String edAdaptorAuthId, String macVaultKeyFilePath) {
        return "mac_vault_secret_token_" + edAdaptorAuthId;
    }
}
