package org.nic.auth;

import org.nic.auth.common.DataConvert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import org.nic.auth.common.ReflectInternalUtil;
import org.nic.auth.common.ThreadTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import static org.nic.auth.EnumAuthValidateConfigurationKeyType.*;
import static org.nic.auth.EnumAuthValidateMajorKeyType.AuthValidateBean;

@Component
public class AuthValidateRunner{

    @Autowired
    ApplicationContext context;

    private int executeIndex;

    public int getExecuteIndex() {
        return executeIndex;
    }

    public AuthValidateRunner(int index) {
        this.executeIndex = index;
    }

    public AuthValidateRunner() {
        new AuthValidateRunner(6001);
    }

    public void run() {
        BaseAuthValidate.setValidateChain(HotCache.inst().getAndSet(AuthValidateBean, "", c -> {
            //todo using the system var to do
            String strValidateBeans = System.getenv().get("Config.AuthValidate.ValidateBeans");
            if (DataConvert.isNullOrEmpty(strValidateBeans)) {
                return new BaseAuthValidate[0];
            }
            String[] beanNames = strValidateBeans.split(",");
            BaseAuthValidate[] tmpBeans = new BaseAuthValidate[beanNames.length];
            for (int i = 0; i < beanNames.length; i++) {
                String beanName = beanNames[i];
                Object bean = ReflectInternalUtil.getBean(beanName);
                if (bean != null) {
                    tmpBeans[i] = (BaseAuthValidate) bean;
                }
            }
            return tmpBeans;
        }).registerRelationConfig(ValidateBeans).value());

        try {
            ThreadTimer.run(new ThreadTimer.BaseCaller() {
                @Override
                public void call(Object o) {
                    String[] beanDefinitionNames = context.getBeanDefinitionNames();

                    String authMethodRegisterUrl = System.getenv().get("Config.AuthValidate.MethodRegisterUrl");
                    if (DataConvert.isNullOrEmpty(authMethodRegisterUrl)) {
                        return;
                    }

                    if (!Boolean.parseBoolean(System.getenv().get("Config.AuthValidate.Switcher"))) {
                        return;
                    }

                    List<ApiMethodMeta> apiMethodMetas = new ArrayList<>();
                    for (String name : beanDefinitionNames) {
                        Object bean = context.getBean(name);
                        if (bean != null) {
                            DnfRestController restClass = AnnotationUtils.findAnnotation(bean.getClass(), DnfRestController.class);
                            if (restClass != null) {
                                for (Method method : bean.getClass().getMethods()) {
                                    try {
                                        ApiAuth apiAuth = AnnotationUtils.findAnnotation(method, ApiAuth.class);
                                        DnfRestPath dnfRestPath = AnnotationUtils.findAnnotation(method, DnfRestPath.class);
                                        if (apiAuth != null && dnfRestPath != null && !DataConvert.isNullOrEmpty(apiAuth.value())) {
                                            for (String apiPath : dnfRestPath.path()) {
                                                ApiMethodMeta meta = new ApiMethodMeta(
                                                        System.getenv().get("AppName"),
                                                        UUID.fromString(apiAuth.value()),
                                                        apiPath, dnfRestPath.version(),
                                                        dnfRestPath.description(),
                                                        apiAuth.level().getValue(),
                                                        apiAuth.validateFields());
                                                apiMethodMetas.add(meta);
                                            }
                                        }
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }
                    }

                    apiMethodMetas.forEach(meta -> {
                        try {
                            BaseResponse<String> rep = JsonConvert.to(BaseResponse.class, HttpHelper.doPost(authMethodRegisterUrl, JsonConvert.toJson(meta)));
                            if (rep == null || rep.getData() != null) {
                            }
                        } catch (Exception ex) {
                        }
                    });

                }
            });
        } catch (InterruptedException e) {

        }
    }
}
