package org.nic.auth.common;

import java.io.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataConvert {

    private static ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    private static ThreadLocal<HashMap<String, SimpleDateFormat>> formatters = new ThreadLocal<>();

    /*
     * 判定对象是不是未空
     * */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        String sTmp = DataConvert.parse(String.class, obj);
        if (sTmp == null) {
            return true;
        }
        if (sTmp.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /*
     * 判定对象是不是未空
     * */
    public static boolean isNull(Object obj) {
        if (obj == null) {
            return true;
        }
        return false;
    }

    /*
     * 通用数据转换方法
     * */
    public static <T> T parse(String clazzName, Object obj) {
        try {
            return parse(clazzName, obj, null);
        } catch (Exception ex) {
            //todo log
        }
        return null;
    }

    /*
     * 通用数据转换方法
     * */
    public static <T> T parse(String clazzName, Object obj, Object defaultValue) {
        try {
            Class<?> clazz = String.class;
            switch (clazzName) {
                case "String":
                    clazz = String.class;
                    break;
                case "int":
                case "Integer":
                    clazz = Integer.class;
                    break;
                case "Short":
                    clazz = Short.class;
                    break;
                case "Long":
                    clazz = Long.class;
                    break;
                case "Double":
                    clazz = Double.class;
                    break;
                case "Byte":
                    clazz = Byte.class;
                    break;
                case "Float":
                    clazz = Float.class;
                    break;
                case "Bool":
                case "boolean":
                case "Boolean":
                    clazz = Boolean.class;
                    break;
                case "Datetime":
                case "Date":
                    clazz = Date.class;
                    break;
                default:
                    break;
            }
            return (T) parse(clazz, obj, null);
        } catch (Exception ex) {
            //todo log
        }
        if (obj != null) {
            return (T) obj;
        } else {
            return null;
        }
    }

    /*
     * 通用数据转换方法
     * */
    public static <T> T parse(Class<T> clazz, Object obj) {
        try {
            return parse(clazz, obj, null);
        } catch (Exception ex) {
            //todo log
        }
        return null;
    }

    /*
     * 通用数据转换方法
     * */
    @SuppressWarnings({"deprecation", "unchecked"})
    public static <T> T parse(Class<T> clazz, Object obj, Object defaultValue) {
        if (obj == null) {
            obj = "";
        } else {
            if (obj.getClass() == clazz) {
                return (T) obj;
            }
        }

        T result = null;
        String strData = obj.toString();
        if (obj.getClass() == Date.class) {
            strData = formatter.get().format(obj);
        }

        try {
            switch (clazz.getName()) {
                case "java.lang.Integer":
                case "int":
                    if (defaultValue == null) {
                        defaultValue = 0;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    int index = strData.indexOf(".");
                    if (index > 0) {
                        strData = strData.substring(0, index);
                    }

                    result = (T) (Object) Integer.parseInt(strData);
                    break;
                case "java.lang.Long":
                case "long":
                    if (defaultValue == null) {
                        defaultValue = 0L;
                    }

                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    int index1 = strData.indexOf(".");
                    if (index1 > 0) {
                        strData = strData.substring(0, index1);
                    }

                    result = (T) (Object) Long.parseLong(strData);
                    break;
                case "java.lang.Short":
                case "short":
                    if (defaultValue == null) {
                        defaultValue = 0;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    result = (T) (Object) Short.parseShort(strData);
                    break;
                case "java.lang.Float":
                case "float":
                    if (defaultValue == null) {
                        defaultValue = 0f;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    result = (T) (Object) Float.parseFloat(strData);
                    break;
                case "java.lang.Double":
                case "double":
                    if (defaultValue == null) {
                        defaultValue = 0d;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    result = (T) (Object) Double.parseDouble(strData);
                    break;
                case "java.lang.Byte":
                case "byte":
                    if (defaultValue == null) {
                        defaultValue = 0;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    result = (T) (Object) Byte.parseByte(strData);
                    break;
                case "java.lang.Boolean":
                case "boolean":
                    if (defaultValue == null) {
                        defaultValue = false;
                    }
                    if (DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) defaultValue;
                        break;
                    }

                    result = (T) (Object) Boolean.parseBoolean(strData);
                    break;
                case "java.util.Date":
                    if (defaultValue == null) {
                        defaultValue = new Date();
                    }
                    if (!DataConvert.isNullOrEmpty(strData) && strData.length() >= 10) {
                        if (strData.indexOf("T") > 0) {
                            strData = strData.replace("T", " ");
                        }

                        if (strData.length() == 10) {
                            strData = strData + " 00:00:00.000";
                        } else {
                            if (strData.length() > 24) {
                                strData = strData.substring(0, 23);
                            }
                        }
                        if (strData.indexOf(".") < 0) {
                            strData = strData + ".000";
                        }
                        result = (T) (Object) formatter.get().parse(strData);
                    }
                    break;
                case "java.lang.Char":
                    if (defaultValue == null) {
                        defaultValue = 0;
                    }
                    result = (T) (Object) strData.charAt(0);
                    break;
                case "java.util.UUID":
                    if (defaultValue == null) {
                        defaultValue = UUID.randomUUID();
                    }
                    if (!DataConvert.isNullOrEmpty(strData)) {
                        result = (T) (Object) UUID.fromString(strData);
                    }
                    break;
                case "java.lang.String":
                    if (obj.getClass().isLocalClass() || obj.getClass().getName().contains("ArrayList") || obj.getClass().getName().contains("[")) {
                        result = (T) JSON.toJSONString(obj);
                    } else {
                        result = (T) strData;
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception ex) {
            //todo log
        } finally {
            if (result == null) {
                result = (T) defaultValue;
            }
        }
        return result;
    }

    public static Date localToUTC(Date localDate) {
        long localTimeInMillis = localDate.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localTimeInMillis);
        calendar.add(Calendar.MILLISECOND, -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)));
        return new Date(calendar.getTimeInMillis());
    }

    public static String formatDateString(String pattern) {
        HashMap<String, SimpleDateFormat> dic = formatters.get();
        if (dic == null) {
            dic = new HashMap<>();
        }
        /*try {*/
        if (DataConvert.isNullOrEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        }
        SimpleDateFormat myformatter = null;
        if (dic.containsKey(pattern)) {
            myformatter = dic.get(pattern);
        } else {
            myformatter = new SimpleDateFormat(pattern);
            dic.put(pattern, myformatter);
            formatters.set(dic);
        }
        return myformatter.format(new Date());
    }

    public static Date utcToLocal(Date utcTime) {
        SimpleDateFormat sdf = formatter.get();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf.setTimeZone(TimeZone.getDefault());
        Date locatlDate = null;
        String localTime = sdf.format(utcTime.getTime());
        try {
            locatlDate = sdf.parse(localTime);
        } catch (Exception ex) {
            //todo log
        }
        return locatlDate;
    }

    public static Double round(double source, double slength) {
        double rate = Math.pow(10, slength);
        return (double) Math.round(source * rate) / rate;
    }

    public static String trim(String source, String trimChar) {
        if (DataConvert.isNullOrEmpty(source)) {
            return source;
        }

        if (source.startsWith(trimChar)) {
            source = source.substring(trimChar.length());
        }
        if (source.endsWith(trimChar)) {
            source = source.substring(0, source.length() - trimChar.length());
        }
        return source;
    }

    public static String padLeft(Object source, String c, int totalLength) {
        String data = parse(String.class, source);
        if (data == null) {
            data = "";
        }

        if (data.length() < totalLength) {
            for (int i = 0; i < totalLength - data.length(); i++) {
                data = c + data;
            }
        }

        return data;
    }

    public static Date timeAdd(EnumDatetimeType datetimeType, long rate) {
        long currentTime = System.currentTimeMillis();
        long durationMillisecond = 1;
        switch (datetimeType) {
            case WEEK:
                durationMillisecond = 604800000 * rate;
                break;
            case DAY:
                durationMillisecond = 86400000 * rate;
                break;
            case HOUR:
                durationMillisecond = 3600000 * rate;
                break;
            case MINUTE:
                durationMillisecond = 60000 * rate;
                break;
            case SECOND:
                durationMillisecond = 1000 * rate;
                break;
            case MILLISECOND:
            default:
                durationMillisecond = rate;
                break;
        }
        return new Date(currentTime + durationMillisecond);
    }

    public static boolean bytesToFile(byte[] bytes, String outputFilePath) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        if (DataConvert.isNullOrEmpty(outputFilePath)) {
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            int len = 1024;

            //文件的拷贝
            byte[] buffer = new byte[1024];
            int index = bytes.length / 1024 + (bytes.length % 1024 > 0 ? 1 : 0);
            for (int i = 0; i < index; i++) {
                if (bytes.length < ((i + 1) * 1024)) {
                    len = bytes.length - i * 1024;
                    buffer = new byte[len];
                }

                for (int i1 = 0; i1 < len; i1++) {
                    buffer[i1] = bytes[i1 + i * 1024];
                }
                fos.write(buffer, 0, len);
            }
        } catch (Exception ex) {

        }
        return true;
    }

    public static <TResult> TResult[] toArray(Collection<TResult> keySet, Class<?> clazz) {
        if (keySet == null || keySet.size() == 0) {
            return (TResult[]) Array.newInstance(clazz, 0);
        }

        TResult[] result = (TResult[]) Array.newInstance(clazz, keySet.size());
        try {
            int index = 0;
            for (TResult tResult : keySet) {
                result[index] = tResult;
                index++;
            }
        } catch (Exception ex) {

        }

        return result;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
