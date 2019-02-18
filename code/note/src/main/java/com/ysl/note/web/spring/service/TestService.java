package com.ysl.note.web.spring.service;

import com.ysl.note.web.bean.Result;

/**
 * @author YSL
 * 2019-02-18 20:45
 */
public interface TestService {
    /**
     * MethodInterceptor中测试方法
     */
    Result getValidStr(Result res);
}
