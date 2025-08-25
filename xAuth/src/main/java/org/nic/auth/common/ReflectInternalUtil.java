package org.nic.auth.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@Component
public class ReflectInternalUtil implements ApplicationContextAware {
    
    private final static String REFLECT_NAME = "Reflect";

    private static Map<String, Object> dicSystemReflect = new HashMap<>();

    private static ApplicationContext _context;

    public static ApplicationContext getContext() {
        return _context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _context = applicationContext;
    }

    public static Object set(Object model, String fieldName, Object data) {
        Object result = null;
        try {
            if (model == null) {
                return result;
            }
            if (fieldName == null || fieldName.isEmpty()) {
                return result;
            }

            HashMap<String, Method> methods = getMappingMethods(model.getClass());

            if (methods == null) {
                return result;
            }

            String methodName = "set" + fieldName.toLowerCase();
            Method method = methods.get(methodName);

            if (method != null && data != null) {
                Class<?> classParameter = method.getParameterTypes()[0];
                Object objVal = data;
                if (classParameter != data.getClass()) {
                    Object objValTmp = DataConvert.parse(classParameter, objVal);
                    if (objValTmp != null) {
                        objVal = objValTmp;
                    }
                }
                result = method.invoke(model, objVal);
            }
        } catch (Exception ex) {
            //todo log
        }

        return result;
    }

    public static Object get(Object model, String fieldName) {
        Object result = null;
        try {
            if (model == null) {
                return result;
            }
            if (fieldName == null || fieldName.isEmpty()) {
                return result;
            }

            HashMap<String, Method> methods = getMappingMethods(model.getClass());
            if (methods == null) {
                return result;
            }

            String methodName = "get" + fieldName.toLowerCase();
            Method method = methods.get(methodName);
            if (method != null) {
                result = method.invoke(model, (Object[]) null);
            }
        } catch (Exception ex) {
            //todo log
        }
        return result;
    }

    public static HashMap<String, Field> getMappingFields(Class<?> clazz) {
        return getMappingFields(clazz, true);
    }

    public static <TAnnotation extends Annotation> HashMap<String, TAnnotation> getMappingAnnotations(final Class<?> clazz, final Class<TAnnotation> annotationClass) {
        if (annotationClass == null || clazz == null) {
            return new HashMap<>();
        }

        HashMap<String, TAnnotation> result;
        String key = String.format("%s.%s.Annotations.@%s", REFLECT_NAME, clazz.getTypeName(), annotationClass.getSimpleName());
        if (dicSystemReflect.containsKey(key)) {
            result = (HashMap<String, TAnnotation>) dicSystemReflect.get(key);
        } else {
            result = getDeepMappingAnnotations(null, clazz, annotationClass);
            if (result != null) {
                dicSystemReflect.put(key, result);
            }
        }
        return result == null ? new HashMap<>() : result;
    }

    private static <TAnnotation extends Annotation> HashMap<String, TAnnotation> getDeepMappingAnnotations(final String parentName, final Class<?> clazz, final Class<TAnnotation> annotationClass) {
        HashMap<String, TAnnotation> annotationHashMap = new HashMap<>();
        for (Field field : getMappingFields(clazz).values()) {
            TAnnotation annotation = getMappingFieldAnnotation(clazz, annotationClass, field.getName());

            String pathName = field.getName();
            if (!DataConvert.isNullOrEmpty(parentName)) {
                pathName = String.format("%s.%s", parentName, field.getName());
            }

            Class<?> tClass = field.getType();
            if (tClass.getTypeName().contains("[")) {
                tClass = tClass.getComponentType();
            } else if (tClass.getTypeName().contains("java.util.List")) {
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                tClass = ReflectInternalUtil.getDeepGenericType(pt);
            }
            if (!tClass.toString().contains(" java.") && !tClass.isEnum()) {
                HashMap<String, TAnnotation> tmp = getDeepMappingAnnotations(pathName, tClass, annotationClass);
                for (String s : tmp.keySet()) {
                    annotationHashMap.put(s, tmp.get(s));
                }
            }

            if (annotation != null) {
                annotationHashMap.put(pathName, annotation);
            }
        }

        return annotationHashMap;
    }

    public static Class<?> getDeepGenericType(ParameterizedType pt) {
        Class<?> clazz = null;
        if (pt != null) {
            Type[] types = pt.getActualTypeArguments();
            if (types.length > 0) {
                ParameterizedType ptype = null;
                if (types[0].getTypeName().contains("[")) {
                    clazz = ((Class<?>) types[0]).getComponentType();
                    return clazz;
                } else if (types[0].getTypeName().contains("java.util.List")) {
                    ptype = (ParameterizedType) types[0];
                }

                if (ptype != null && ptype.getActualTypeArguments().length > 0) {
                    clazz = getDeepGenericType(ptype);
                } else {
                    assert types[0] instanceof Class<?>;
                    clazz = (Class<?>) types[0];
                }
            }
        }
        return clazz;
    }

    public static HashMap<String, Field> getMappingFields(final Class<?> clazz, boolean isDeepFind) {
        if (clazz == null) {
            return new HashMap<>();
        }

        HashMap<String, Field> result;
        String key = String.format("%s.%s.Field", REFLECT_NAME, clazz.getTypeName());
        if (dicSystemReflect.containsKey(key)) {
            result = (HashMap<String, Field>) dicSystemReflect.get(key);
        } else {
            result = new HashMap<>();
            List<Field> fields = getFieldsByClassLoop(clazz, isDeepFind);
            if (!fields.isEmpty()) {
                for (Field field : fields) {
                    result.putIfAbsent(field.getName().toLowerCase(), field);
                }
                dicSystemReflect.put(key, result);
            }
        }
        return result;
    }

    private static List<Field> getFieldsByClassLoop(Class<?> clazz, boolean isDeepFind) {

        Field[] fields = clazz.getDeclaredFields();
        List<Field> list = new ArrayList<>(Arrays.asList(fields));

        if (isDeepFind) {
            Class<?> supper = clazz.getSuperclass();
            if (supper != null) {
                List<Field> supperFields = getFieldsByClassLoop(supper, isDeepFind);
                list.addAll(supperFields);
            }
        }

        return list;
    }

    public static HashMap<String, Method> getMappingMethods(Class<?> clazz) {
        if (clazz == null) {
            return new HashMap<>();
        }

        HashMap<String, Method> result;
        String key = String.format("%s.%s.Method", REFLECT_NAME, clazz.getTypeName());
        if (dicSystemReflect.containsKey(key)) {
            result = (HashMap<String, Method>) dicSystemReflect.get(key);
        } else {
            result = new HashMap<>();
            List<Method> methods = getMethodsByClassLoop(clazz);
            for (Method m : methods) {
                result.putIfAbsent(m.getName().toLowerCase(), m);
            }
            dicSystemReflect.put(key, result);
        }
        return result;
    }

    private static List<Method> getMethodsByClassLoop(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        return new ArrayList<>(Arrays.asList(methods));
    }

    public static <TAnnotation extends Annotation> TAnnotation getMappingFieldAnnotation(final Class<?> clazz, final Class<TAnnotation> annotationClass, String fieldName) {
        if (clazz == null) {
            return null;
        }
        if (DataConvert.isNullOrEmpty(fieldName)) {
            return null;
        }

        Field field = ReflectInternalUtil.getMappingFields(clazz).get(fieldName.toLowerCase());
        if (field == null) {
            return null;
        }

        TAnnotation annotation;
        String key = String.format("%s.%s.%s.@%s", REFLECT_NAME, clazz.getTypeName(), field.getName(), annotationClass.getSimpleName());
        if (dicSystemReflect.containsKey(key)) {
            annotation = (TAnnotation) dicSystemReflect.get(key);
        } else {
            annotation = AnnotationUtils.findAnnotation(field, annotationClass);
            if (annotation != null) {
                dicSystemReflect.put(key, annotation);
            }
        }
        return annotation;
    }

    public static String toMappingField(final Class<?> clazz, String fieldName) {
        Column myColumn = getMappingFieldAnnotation(clazz, Column.class, fieldName);
        if (myColumn == null) {
            return fieldName;
        }
        return myColumn.name();
    }

    public static <T> T getBean(String beanClassName) {
        try {
            Object bean = ReflectInternalUtil.getContext().getBean(beanClassName);
            return (T) bean;
        } catch (Exception ex) {
            //todo log
        }
        return null;
    }

    public static <T> T getBean(Class<?> beanClass) {
        try {
            Object bean = ReflectInternalUtil.getContext().getBean(beanClass);
            return (T) bean;
        } catch (Exception ex) {
            //todo log
        }
        return null;
    }

    public static Object registerBean(Class<?> beanDelegateClass, Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }
        Object bean = null;
        try {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                    ((ConfigurableApplicationContext) ReflectInternalUtil.getContext()).getBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);

            List<String> relationBeans = new ArrayList<>();
            Field[] fields = getMappingFields(beanClass, true).values().toArray(new Field[0]);
            for (Field field : fields) {
                Autowired autowired = AnnotationUtils.findAnnotation(field, Autowired.class);
                if (autowired != null) {
                    relationBeans.add(field.getName());
                }
            }

            for (String key : relationBeans) {
                Object tmpBean = ReflectInternalUtil.getContext().getBean(key);
                beanDefinitionBuilder.addPropertyValue(key, tmpBean);
            }

            AbstractBeanDefinition abstractBeanDefinition = beanDefinitionBuilder.getBeanDefinition();
            beanFactory.registerBeanDefinition(beanDelegateClass.getName(), abstractBeanDefinition);
            bean = getBean(beanDelegateClass.getName());
        } catch (Exception ex) {
            //TODO:log
        }

        return bean;
    }

    public static Object registerBean(String beanName, Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }
        Object bean = null;
        try {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                    ((ConfigurableApplicationContext) ReflectInternalUtil.getContext()).getBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);

            List<String> relationBeans = new ArrayList<>();
            Field[] fields = getMappingFields(beanClass, true).values().toArray(new Field[0]);
            for (Field field : fields) {
                Autowired autowired = AnnotationUtils.findAnnotation(field, Autowired.class);
                if (autowired != null) {
                    relationBeans.add(field.getName());
                }
            }

            for (String key : relationBeans) {
                Object tmpBean = ReflectInternalUtil.getContext().getBean(key);
                beanDefinitionBuilder.addPropertyValue(key, tmpBean);
            }

            AbstractBeanDefinition abstractBeanDefinition = beanDefinitionBuilder.getBeanDefinition();
            beanFactory.registerBeanDefinition(beanName, abstractBeanDefinition);
            bean = ReflectInternalUtil.getContext().getBean(beanName);
        } catch (Exception ex) {
            //TODO log
        }

        return bean;
    }

    public static Class<?> getGenericType(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj.getClass().getName().equals("java.util.ArrayList")) {
            if (obj instanceof List<?>){
                List<Object> list = (List<Object>) obj;
                if (!list.isEmpty() && list.get(0) != null) {
                    return list.get(0).getClass();
                }
            }
        } else if (obj.getClass().getName().indexOf("[L") > 0) {
            Object[] array = (Object[]) obj;
            if (array.length > 0 && array[0] != null) {
                return array[0].getClass();
            }
        } else {
            return obj.getClass();
        }
        return null;
    }

}

