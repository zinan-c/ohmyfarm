package org.nic.auth.common;

public enum EnumDatetimeType {
    WEEK(7 * 24 * 3600 * 1000, "星期"), DAY(24 * 3600 * 1000, "日"), HOUR(3600 * 1000, "小时"), MINUTE(60 * 1000, "分钟"), SECOND(1000, "秒"), MILLISECOND(1, "毫秒");

    private int value;
    private String text;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private EnumDatetimeType(int value, String text) {
        this.value = value;
        this.text = text;
    }
}
