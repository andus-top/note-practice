package com.ysl.note.web.spring.proxy;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;

/**
 * 判断当前参数是否需要被代理
 * @author YSL
 * 2019-02-15 09:24
 */
public class AutoProxyCreator extends AbstractAutoProxyCreator {

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass,
                                                    String beanName, TargetSource customTargetSource) throws BeansException {
        return isMatch(beanClass) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY;
    }

    /**
     * 判断是否是需要被代理的对象，自定义拦截规则
     * @param clazz 代理对象的类型
     * @return
     */
    private boolean isMatch(Class<?> clazz) {
        //有两个Class类型的类象，一个是调用isAssignableFrom方法的类对象（后称对象a），
        // 以及方法中作为参数的这个类对象(称之为对象b)，这两个对象如果满足以下条件则返回true，否则返回false：

        //a对象所对应类信息是b对象所对应的类信息的父类或者是父接口，简单理解即a是b的父类或接口
        //a对象所对应类信息与b对象所对应的类信息相同，简单理解即a和b为同一个类或同一个接口
        // FIXME: 自定义拦截规则
        if (Object.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }

}

