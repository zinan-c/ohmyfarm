package org.nic.auth;

public class AuthValidateMeta {
    private String appKey;
    private String method;
    private String methodVersion;
    private String signed;
    private String signMethod;
    private String validateFields;
    private long timestamp;
    private String fromClientIp;

    private EnumAuthValidateLevel authValidateLevel;
    private boolean excludeNullValue = true;
    private String dynamicKey;
    private Object source;

    public String getValidateFields() {
        return validateFields;
    }

    public void setValidateFields(String validateFields) {
        this.validateFields = validateFields;
    }

    public EnumAuthValidateLevel getAuthValidateLevel() {
        return authValidateLevel;
    }

    public void setAuthValidateLevel(EnumAuthValidateLevel authValidateLevel) {
        this.authValidateLevel = authValidateLevel;
    }

    public String getFromClientIp() {
        return fromClientIp;
    }

    public void setFromClientIp(String fromClientIp) {
        this.fromClientIp = fromClientIp;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodVersion() {
        return methodVersion;
    }

    public void setMethodVersion(String methodVersion) {
        this.methodVersion = methodVersion;
    }

    public String getSigned() {
        return signed;
    }

    public void setSigned(String signed) {
        this.signed = signed;
    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDynamicKey() {
        return dynamicKey;
    }

    public void setDynamicKey(String dynamicKey) {
        this.dynamicKey = dynamicKey;
    }

    public boolean isExcludeNullValue() {
        return excludeNullValue;
    }

    public void setExcludeNullValue(boolean excludeNullValue) {
        this.excludeNullValue = excludeNullValue;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
