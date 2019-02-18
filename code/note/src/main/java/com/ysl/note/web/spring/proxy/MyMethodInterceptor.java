package com.ysl.note.web.spring.proxy;

import com.ysl.note.web.bean.Result;
import com.ysl.note.web.spring.service.TestService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 实现MethodInterceptor接口，充当AutoProxyCreator自动生成的代理对象的拦截器
 * @author YSL
 * 2019-02-15 09:26
 */
@Component
public class MyMethodInterceptor implements MethodInterceptor {
    @Autowired
    private TestService testService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        // 要修改的目标参数
        Result result = null;
        int flag = -1;

        // 目标方法的参数
        Object[] args = invocation.getArguments();
        for (int i=0; i<args.length; i++) {
            // 只修改指定类型参数
            // FIXME：规则自定义
            if(args[i] instanceof Result){
                Result res = (Result) args[i];
                result = testService.getValidStr(res);
                flag = i;
            }
        }
        // 修改目标参数
        if(flag >= 0 && result != null){
            args[flag] = result;
        }

        // 执行目标方法
        Object object = invocation.proceed();

        return object;
    }
}
