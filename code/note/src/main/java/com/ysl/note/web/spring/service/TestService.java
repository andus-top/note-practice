package com.ysl.note.web.spring.service;

import com.ysl.note.web.bean.Result;

/**
 * Spring Service 暂无实现
 * @author YSL
 * 2019-02-18 20:45
 */
public interface TestService {
    /**
     * 防止MethodInterceptor类报错
     */
    Result getValidParam(Result res);
}
