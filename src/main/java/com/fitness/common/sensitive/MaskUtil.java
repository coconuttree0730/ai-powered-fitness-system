package com.fitness.common.sensitive;

/**
 * 敏感数据脱敏工具类
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /**
     * 手机号脱敏: 保留前3后4，中间替换为 ****
     * 例: 13800138000 → 138****1234
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏: @ 前保留前3字符，@ 后保留完整
     * 例: zhangsan@qq.com → zha***@qq.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            return email;
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 3) {
            return local + "***" + domain;
        }
        return local.substring(0, 3) + "***" + domain;
    }

    /**
     * 姓名脱敏: 保留首尾字符，中间替换为 *
     * 例: 张小明 → 张*明, 张三 → 张*
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append('*');
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }

    /**
     * 地址脱敏: 保留前6字符 + ***
     * 例: 北京市朝阳区三里屯太古里 → 北京市朝阳区***
     */
    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        if (address.length() <= 6) {
            return "***";
        }
        return address.substring(0, 6) + "***";
    }

    /**
     * 根据敏感类型分发脱敏方法
     */
    public static String mask(String value, SensitiveType type) {
        if (value == null) {
            return null;
        }
        return switch (type) {
            case PHONE -> maskPhone(value);
            case EMAIL -> maskEmail(value);
            case NAME -> maskName(value);
            case ADDRESS -> maskAddress(value);
        };
    }
}
