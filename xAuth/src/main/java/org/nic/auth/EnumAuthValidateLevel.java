package org.nic.auth;

public enum EnumAuthValidateLevel {
    Ultimate(0), Advanced(1), Basic (2), Low (3);

    private int level;

    EnumAuthValidateLevel(int level) {
        this.level = level;
    }

    public int getValue() {
        return this.level;
    }

    public static EnumAuthValidateLevel getEnum(int level) {
        switch (level) {
            case 0:
                return Ultimate;
            case 1:
                return Advanced;
            case 2:
                return Basic;
            case 3:
                return Low;
        }
        return Low;
    }
}
