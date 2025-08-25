package org.nic.auth;

import java.util.UUID;

public class AuthMethodMeta {
    private String appName;
    private UUID apiMethodId;
    private String apiRoutePath;
    private String apiVersion;
    private String apiDesc;
    private int authValidateLevel = 0;
    private String[] validateFields;

    public AuthMethodMeta() {
    }

    public AuthMethodMeta(String appName, UUID apiMethodId, String apiRoutePath, String apiVersion, String apiDesc) {
        this.appName = appName;
        this.apiMethodId = apiMethodId;
        this.apiRoutePath = apiRoutePath;
        this.apiVersion = apiVersion;
        this.apiDesc = apiDesc;
    }

    public AuthMethodMeta(String appName, UUID apiMethodId, String apiRoutePath, String apiVersion, String apiDesc, int authValidateLevel) {
        this.appName = appName;
        this.apiMethodId = apiMethodId;
        this.apiRoutePath = apiRoutePath;
        this.apiVersion = apiVersion;
        this.apiDesc = apiDesc;
        this.authValidateLevel = authValidateLevel;
    }

    public AuthMethodMeta(String appName, UUID apiMethodId, String apiRoutePath, String apiVersion, String apiDesc, int authValidateLevel, String[] validateFields) {
        this.appName = appName;
        this.apiMethodId = apiMethodId;
        this.apiRoutePath = apiRoutePath;
        this.apiVersion = apiVersion;
        this.apiDesc = apiDesc;
        this.authValidateLevel = authValidateLevel;
        this.validateFields = validateFields;
    }

    public String[] getValidateFields() {
        return validateFields;
    }

    public void setValidateFields(String[] validateFields) {
        this.validateFields = validateFields;
    }

    public int getAuthValidateLevel() {
        return authValidateLevel;
    }

    public void setAuthValidateLevel(int authValidateLevel) {
        this.authValidateLevel = authValidateLevel;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public UUID getApiMethodId() {
        return apiMethodId;
    }

    public void setApiMethodId(UUID apiMethodId) {
        this.apiMethodId = apiMethodId;
    }

    public String getApiRoutePath() {
        return apiRoutePath;
    }

    public void setApiRoutePath(String apiRoutePath) {
        this.apiRoutePath = apiRoutePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiDesc() {
        return apiDesc;
    }

    public void setApiDesc(String apiDesc) {
        this.apiDesc = apiDesc;
    }
}
