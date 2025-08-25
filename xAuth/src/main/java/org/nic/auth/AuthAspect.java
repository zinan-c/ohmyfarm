package org.nic.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dnf.utils.aop.AspectAnnotationData;
import com.dnf.utils.aop.BaseAspect;
import com.dnf.utils.base.IConfigReader;
import com.dnf.utils.base.controller.DnfRestPath;
import com.dnf.utils.base.db.BaseResponse;
import com.dnf.utils.cache.HotCache;
import com.dnf.utils.common.DataConvert;
import com.dnf.utils.common.JsonConvert;
import com.dnf.utils.encrypt.ApiSignFunction;
import com.dnf.utils.encrypt.MD5Helper;
import com.dnf.utils.io.HttpHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Objects;

import static com.dnf.auth.EnumAuthValidateMajorKeyType.QueryString;

@Aspect
@Component
public class AuthAspect extends BaseAspect {
    @Autowired
    private IConfigReader configReader;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public static ThreadLocal<AuthValidateMeta> validateMeta = new ThreadLocal();

    @Pointcut("@annotation(ApiAuth)")
    public void apiAuthPoint() {
    }

    public AuthValidateMeta getValidateMeta() {
        AuthValidateMeta meta = new AuthValidateMeta();
        if (httpServletRequest == null) {
            return meta;
        }

        String appKey = ApiSignFunction.getRequestData(httpServletRequest, "AppKey");
        String signed = ApiSignFunction.getRequestData(httpServletRequest, "Signed");
        long timestamp = DataConvert.parse(long.class, ApiSignFunction.getRequestData(httpServletRequest, "Timestamp"), System.currentTimeMillis());
        boolean excludeNullValue = DataConvert.parse(boolean.class, ApiSignFunction.getRequestData(httpServletRequest, "ExcludeNullValue"), true);
        String dynamicKey = ApiSignFunction.getRequestData(httpServletRequest, "DynamicKey");
        String method = ApiSignFunction.getRequestData(httpServletRequest, "Method");
        String signmethod = ApiSignFunction.getRequestData(httpServletRequest, "SignMethod");
        String methodversion = ApiSignFunction.getRequestData(httpServletRequest, "MethodfVersion");

        if (DataConvert.isNullOrEmpty(appKey) || DataConvert.isNullOrEmpty(signed)) {
            HashMap<String, String> dicQueryString = getDicQueryString();
            if (dicQueryString != null) {
                dicQueryString.remove("appkey");
                dicQueryString.remove("signed");
                dicQueryString.remove("excludenullvalue");
                dicQueryString.remove("timestamp");
                dicQueryString.remove("method");
                dicQueryString.remove("signmethod");
                dicQueryString.remove("methodversion");
                dicQueryString.remove("dynamickey");
                dicQueryString.remove("source");
                for (String s : dicQueryString.keySet()) {
                    if (dicQueryString.get(s) != null && dicQueryString.get(s).toLowerCase().equals("null")) {
                        dicQueryString.replace(s, null);
                    }
                }
                meta.setSource(dicQueryString);
            }
        }

        meta.setAppKey(appKey);
        meta.setSigned(signed);
        meta.setTimestamp(timestamp);
        meta.setExcludeNullValue(excludeNullValue);
        meta.setDynamicKey(dynamicKey);
        meta.setSignMethod(signmethod);
        meta.setMethod(method);
        meta.setMethodVersion(methodversion);
        meta.setFromClientIp(getUrlIpAddress());
        return meta;
    }

    private HashMap<String, String> getDicQueryString() {
        String queryString = httpServletRequest.getQueryString();
        if (DataConvert.isNullOrEmpty(queryString)) {
            return null;
        }

        HashMap<String, String> dicQueryStrings = HotCache.inst().getAndSet(QueryString, "QueryString", null, f1 -> {
            HashMap<String, String> result = new HashMap<>();
            if (DataConvert.isNullOrEmpty(queryString) || queryString.indexOf("=") < 0) {
                return result;
            }

            for (String s : queryString.split("\\&")) {
                if (s.indexOf("=") > 0) {
                    String[] kv = s.split("\\=");
                    if (kv.length > 1) {
                        if (!result.containsKey(kv[0].toLowerCase())) {
                            try {
                                result.put(kv[0].toLowerCase(), URLDecoder.decode(kv[1], "UTF-8"));
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }
            return result;
        }, m -> {
            return MD5Helper.getMd5MessageDigest(queryString.getBytes());
        }).value();

        return dicQueryStrings;
    }

    private String getUrlIpAddress() {
        String ip = httpServletRequest.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }

    public static class IPAddressRule {
        private int[] ipValues;

        public IPAddressRule(String ipRule, int[] ipValues) {
            this.ipValues = ipValues;
            String[] metas = ipRule.split("\\.");
            if (metas.length < 0) {
                return;
            }

            int count = 0;
            for (String meta : metas) {
                if ("*".equals(meta.trim())) {
                    setValue(count, 0, 254);
                } else if (meta.indexOf("-") >= 0) {
                    String[] tmps = meta.split("\\-");
                    int low = meta.startsWith("-") ? 0 : DataConvert.parse(int.class, tmps[0]);
                    int high = meta.startsWith("-") ? DataConvert.parse(int.class, tmps[1]) : 254;
                    setValue(count, low, high);
                } else {
                    int val = DataConvert.parse(int.class, meta);
                    setValue(count, val, val);
                }
                count++;
            }
        }

        public boolean validate() {
            for (int i = 0; i < ipValues.length; i++) {
                switch (i) {
                    case 0:
                        if (ipValues[i] > c0_1 || ipValues[i] < c0_0) {
                            return false;
                        }
                        break;
                    case 1:
                        if (ipValues[i] > c1_1 || ipValues[i] < c1_0) {
                            return false;
                        }
                        break;
                    case 2:
                        if (ipValues[i] > c2_1 || ipValues[i] < c2_0) {
                            return false;
                        }
                        break;
                    case 3:
                        if (ipValues[i] > c3_1 || ipValues[i] < c3_0) {
                            return false;
                        }
                        break;
                }
            }
            return true;
        }

        private void setValue(int location, int lowvalue, int highvalue) {
            switch (location) {
                case 0:
                    c0_0 = lowvalue;
                    c0_1 = highvalue;
                    break;
                case 1:
                    c1_0 = lowvalue;
                    c1_1 = highvalue;
                    break;
                case 2:
                    c2_0 = lowvalue;
                    c2_1 = highvalue;
                    break;
                case 3:
                    c3_0 = lowvalue;
                    c3_1 = highvalue;
                    break;
            }
        }

        private int c0_0 = 0;
        private int c1_0 = 0;
        private int c2_0 = 0;
        private int c3_0 = 0;

        private int c0_1 = 254;
        private int c1_1 = 254;
        private int c2_1 = 254;
        private int c3_1 = 254;
    }

    private boolean validateIsUmlimitHost(String ip) {
        if (ip == null || ip.indexOf(".") < 0) {
            return false;
        }

        String[] rules = ((String) configReader.get(AuthRuleForUnlimitedIPAddress)).split("\\,");
        int[] ipValues = {-1, -1, -1, -1};
        String[] strIpValues = ip.split("\\.");
        for (int i = 0; i < strIpValues.length; i++) {
            ipValues[i] = DataConvert.parse(int.class, strIpValues[i]);
        }

        for (String r : rules) {
            IPAddressRule rule = new IPAddressRule(r, ipValues);
            if (rule.validate() == false) {
                return false;
            }
        }

        return true;
    }

    @Around(value = "apiAuthPoint()")
    public Object execAround(ProceedingJoinPoint pjp) {
        if (true == (boolean) configReader.get(AuthSwitcher)) {
            AspectAnnotationData<ApiAuth> aspectAnnotation = getAnnotationData(ApiAuth.class, pjp);
            if (aspectAnnotation != null) {
                BaseResponse rep = null;
                Method m = aspectAnnotation.getMethod();
                try {
                    rep = (BaseResponse) m.getReturnType().newInstance();
                } catch (Exception ex) {
                }
                ApiAuth apiAuth = aspectAnnotation.getAspectAnnotation();

                if (apiAuth != null) {
                    AuthValidateMeta meta = getValidateMeta();
                    if (apiAuth.value() == null || apiAuth.value().trim().length() == 0) {
                        meta.setAuthValidateLevel(EnumAuthValidateLevel.Low);
                    } else {
                        meta.setAuthValidateLevel(apiAuth.level());
                    }

                    if (apiAuth.validateFields() != null && apiAuth.validateFields().length > 0) {
                        meta.setValidateFields(String.join(",", apiAuth.validateFields()));
                    }

                    Object[] args = pjp.getArgs();
                    String[] suthNotSignedDataTypes = ((String) configReader.get(AuthNotSignedDataTypes)).split("\\,");
                    if (args != null && args.length > 0) {
                        boolean flagIgnoreData = false;
                        for (String endStr : suthNotSignedDataTypes) {
                            flagIgnoreData = args[0].getClass().getName().endsWith(endStr);
                            if (flagIgnoreData) {
                                break;
                            }
                        }

                        if (!flagIgnoreData) {
                            meta.setSource(args[0]);
                        }
                    }

                    //取得方法信息
                    DnfRestPath dnfRestPath = AnnotationUtils.findAnnotation(m, DnfRestPath.class);
                    if (dnfRestPath == null) {
                        rep.setSuccess(false);
                        return rep;
                    }
                    meta.setMethodVersion(dnfRestPath.version());

                    //取得入口信息
                    String authValidateUrl = configReader.get(AuthValidateUrl);
                    if (DataConvert.isNullOrEmpty(authValidateUrl)) {
                        rep.setSuccess(false);
                        return rep;
                    }

                    boolean isSuccessFlag = false;
                    JSONObject repJObject = null;
                    for (String apiPath : dnfRestPath.path()) {
                        meta.setMethod(apiPath);
                        String repString = null;
                        try {
                            repString = HttpHelper.doPost(authValidateUrl, JsonConvert.toJson(meta));
                            repJObject = JSON.parseObject(repString);
                        } catch (Exception ex) {
                        }
                        if (repJObject == null) {
                            rep.setSuccess(false);
                            return rep;
                        }

                        if (repJObject.get("data") != null) {
                            isSuccessFlag = true;
                            validateMeta.set(meta);
                        }
                    }

                    if (isSuccessFlag == false) {
                        rep.setSuccess(false);
                        return rep;
                    }
                }
            }
        }

        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return result;
    }
}
