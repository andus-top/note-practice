package com.ysl.note.web.tld;

import jodd.util.StringUtil;

/**
 * 用于前端前端调用的方法
 * @author YSL
 * 2018-09-29 10:37
 */
public class Fnc {

    /**
     * 有值显示 值+单位，无值显示 无
     * @param val 值
     * @param unit 单位
     * @author YSL
     * 2018-09-29 10:41
     */
    public static String showValue(String val, String unit) {
        if(StringUtil.isEmpty(val)){
            return "无";
        }
        return val + unit;
    }
}
