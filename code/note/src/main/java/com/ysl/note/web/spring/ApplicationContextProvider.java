package com.ysl.note.web.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * 获取 spring applicationContext
 * 获取 servletContext
 * @author YSL
 * 2/14/19 9:24 PM
 */
public class ApplicationContextProvider implements ApplicationContextAware, ServletContextAware {

    private static Logger log = LoggerFactory.getLogger(ApplicationContextProvider.class);

    private static ApplicationContext applicationContext;
    private static ServletContext servletContext;

    /**
     * 实现ApplicationContextAware接口, 注入applicationContext到静态变量中.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextProvider.applicationContext=applicationContext;
        // 这样就可以直接使用applicationContext对象了。比如在tld的function的class类中使用
        //applicationContext.getBean("beanName");
    }

    /**
     * 实现ServletContextAware接口, 注入servletContext到静态变量中.
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        ApplicationContextProvider.servletContext=servletContext;
    }

    /**
     * 通过beanName 获取bean
     */
    public static <T>T getSpringBean(String springBeanName) {
        return (T)applicationContext.getBean(springBeanName);
    }

}