package org.nic.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nic.auth.EnumAuthValidateLevel.Ultimate;

public abstract class BaseAuthValidate {
    public BaseAuthValidate(String authValidateName, EnumAuthValidateLevel... authValidateLevels) {
        this.authValidateLevels = authValidateLevels;
        this.authValidateName = authValidateName;
    }

    private final EnumAuthValidateLevel[] authValidateLevels;
    private final String authValidateName;
    private BaseAuthValidate next;

    public EnumAuthValidateLevel[] getAuthValidateLevel() {
        return authValidateLevels;
    }

    public String getAuthValidateName() {
        return authValidateName;
    }

    public BaseAuthValidate getNext() {
        return next;
    }

    private BaseAuthValidate[] validators;

    private static BaseAuthValidate base;

    public static BaseAuthValidate getBase() {
        return base;
    }

    public static void setValidateChain(BaseAuthValidate... validators) {
        if (validators == null || validators.length == 0) {
            return;
        }

        //response chain
        base = validators[0];
        if (validators.length > 1) {
            if (base.next == null) {
                base.next = validators[1];
            } else {
                BaseAuthValidate tmp = base;
                while (tmp.next != null) {
                    tmp = base.next;
                }
                tmp.next = validators[1];
            }
            for (int i = validators.length - 2; i >= 0; i--) {
                validators[i].next = validators[i + 1];
            }
        }
    }

    public List<AuthValidateResult> run(AuthValidateMeta meta) {
        return run(meta, Ultimate);
    }

    public List<AuthValidateResult> run(AuthValidateMeta meta, EnumAuthValidateLevel authLevel) {
        List<AuthValidateResult> results = new ArrayList<>();
        BaseAuthValidate tmp = base;
        while (tmp != null) {
            AuthValidateResult executeResult;
            if (Arrays.stream(tmp.getAuthValidateLevel()).anyMatch(f -> f == authLevel)) {
                executeResult = tmp.exec(meta);
            } else {
                executeResult = new AuthValidateResult(tmp.getAuthValidateName());
                executeResult.setMessage("非特殊处理等级，忽略该验证操作");
            }
            results.add(executeResult);
            if (executeResult != null && !executeResult.isSuccessful()) {
                break;
            }
            tmp = tmp.next;
        }
        return results;
    }

    public static AuthValidateResult getErrorResult(List<AuthValidateResult> results) {
        for (AuthValidateResult res : results) {
            if (res != null && !res.isSuccessful()) {
                return res;
            }
        }
        return null;
    }

    protected abstract AuthValidateResult exec(AuthValidateMeta meta);
}
