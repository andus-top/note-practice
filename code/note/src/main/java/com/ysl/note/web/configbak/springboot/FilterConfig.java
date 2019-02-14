package com.ysl.note.web.configbak.springboot;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;

/**
 * 过滤器
 * @author YSL
 * 2018-09-11 18:28
 */
//@Configuration
public class FilterConfig {

    /**
     * 使用Rest风格的URI，将页面普通的post请求转为指定的delete或者put请求
     * @author YSL
     * 2018-09-11 18:43
     */
    @Bean
    public FilterRegistrationBean hiddenHttpMethodFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new HiddenHttpMethodFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(100);
        registration.setName("HiddenHttpMethodFilter");
        return registration;
    }

    /**
     * HttpPutFormContentFilter过滤器的作用就是获取put表单的值，并将之传递到Controller中标注了method为RequestMethod.put的方法中
     * 该过滤器只能接受enctype值为application/x-www-form-urlencoded的表单
     * @author YSL
     * 2018-09-11 18:43
     */
    @Bean
    @Order(102)
    public FilterRegistrationBean httpPutFormContentFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new HttpPutFormContentFilter());
        registration.addUrlPatterns("/*");
        registration.setName("HttpPutFormContentFilter");
        return registration;
    }

}
