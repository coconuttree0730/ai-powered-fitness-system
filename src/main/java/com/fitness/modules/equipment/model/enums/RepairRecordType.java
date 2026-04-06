package com.fitness.modules.equipment.model.enums;

import lombok.Getter;

/**
 * 报修记录类型枚举
 */
@Getter
public enum RepairRecordType {

    SUBMIT(1, "提交报修"),
    STATUS_CHANGE(2, "状态变更"),
    REMARK(3, "处理备注"),
    CANCEL(4, "取消报修");

    private final Integer code;
    private final String name;

    RepairRecordType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static RepairRecordType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RepairRecordType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
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
        RepairRecordType type = getByCode(code);
        return type != null ? type.getName() : "";
    }
}
