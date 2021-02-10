package com.ddf.boot.common.core.repeatable;

import lombok.Data;

/**
 * <p>防重校验属性</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 13:23
 */
@Data
public class RepeatableProperties {

    /**
     * 全局验证器
     */
    private String globalValidator;
}