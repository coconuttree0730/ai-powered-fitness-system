package com.fitness.modules.equipment.model.enums;

import lombok.Getter;

/**
 * 器材状态枚举
 */
@Getter
public enum EquipmentStatus {

    MAINTENANCE(0, "维修中"),
    NORMAL(1, "正常"),
    SCRAPPED(2, "已报废");

    private final Integer code;
    private final String name;

    EquipmentStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static EquipmentStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EquipmentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取名称
     *
     * @param code 状态码
     * @return 状态名称
     */
    public static String getNameByCode(Integer code) {
        EquipmentStatus status = getByCode(code);
        return status != null ? status.getName() : "";
    }
}
