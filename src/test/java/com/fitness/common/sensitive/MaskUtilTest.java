package com.fitness.common.sensitive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskUtilTest {

    // ========== 手机号 ==========
    @Test
    void maskPhone_normalPhone_returnsMasked() {
        assertEquals("138****1234", MaskUtil.maskPhone("13800131234"));
    }

    @Test
    void maskPhone_shortPhone_returnsOriginal() {
        assertEquals("123", MaskUtil.maskPhone("123"));
    }

    @Test
    void maskPhone_null_returnsNull() {
        assertNull(MaskUtil.maskPhone(null));
    }

    @Test
    void maskPhone_empty_returnsEmpty() {
        assertEquals("", MaskUtil.maskPhone(""));
    }

    // ========== 邮箱 ==========
    @Test
    void maskEmail_normalEmail_returnsMasked() {
        assertEquals("zha***@qq.com", MaskUtil.maskEmail("zhangsan@qq.com"));
    }

    @Test
    void maskEmail_shortLocal_returnsMasked() {
        assertEquals("ab***@gmail.com", MaskUtil.maskEmail("ab@gmail.com"));
    }

    @Test
    void maskEmail_noAtSign_returnsOriginal() {
        assertEquals("invalid", MaskUtil.maskEmail("invalid"));
    }

    @Test
    void maskEmail_null_returnsNull() {
        assertNull(MaskUtil.maskEmail(null));
    }

    // ========== 姓名 ==========
    @Test
    void maskName_twoChars_returnsMasked() {
        assertEquals("张*", MaskUtil.maskName("张三"));
    }

    @Test
    void maskName_threeChars_returnsMasked() {
        assertEquals("张*明", MaskUtil.maskName("张小明"));
    }

    @Test
    void maskName_fourChars_returnsMasked() {
        assertEquals("张**明", MaskUtil.maskName("张小明明"));
    }

    @Test
    void maskName_singleChar_returnsOriginal() {
        assertEquals("张", MaskUtil.maskName("张"));
    }

    @Test
    void maskName_null_returnsNull() {
        assertNull(MaskUtil.maskName(null));
    }

    // ========== 地址 ==========
    @Test
    void maskAddress_longAddress_returnsMasked() {
        assertEquals("北京市朝阳区***", MaskUtil.maskAddress("北京市朝阳区三里屯太古里"));
    }

    @Test
    void maskAddress_shortAddress_returnsMasked() {
        assertEquals("***", MaskUtil.maskAddress("短地址"));
    }

    @Test
    void maskAddress_null_returnsNull() {
        assertNull(MaskUtil.maskAddress(null));
    }

    // ========== 分发方法 ==========
    @Test
    void mask_phoneType_callsMaskPhone() {
        assertEquals("138****1234", MaskUtil.mask("13800131234", SensitiveType.PHONE));
    }

    @Test
    void mask_emailType_callsMaskEmail() {
        assertEquals("zha***@qq.com", MaskUtil.mask("zhangsan@qq.com", SensitiveType.EMAIL));
    }

    @Test
    void mask_nameType_callsMaskName() {
        assertEquals("张*明", MaskUtil.mask("张小明", SensitiveType.NAME));
    }

    @Test
    void mask_addressType_callsMaskAddress() {
        assertEquals("北京市朝阳区***", MaskUtil.mask("北京市朝阳区三里屯太古里", SensitiveType.ADDRESS));
    }
}
