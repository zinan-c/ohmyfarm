package org.nic.auth;

public class AuthValidateResult {
    public AuthValidateResult(String authValidateName) {
        this.authValidateName = authValidateName;
        this.successful = true;
    }

    public AuthValidateResult(String authValidateName, int code, String message) {
        this.authValidateName = authValidateName;
        this.successful = false;
        this.message = message;
        this.code = code;
    }

    private String authValidateName;
    private boolean successful;
    private String message;
    private int code;

    public String getAuthValidateName() {
        return authValidateName;
    }

    public void setAuthValidateName(String authValidateName) {
        this.authValidateName = authValidateName;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
