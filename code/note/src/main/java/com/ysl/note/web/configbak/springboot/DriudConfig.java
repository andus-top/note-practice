package com.ysl.note.web.configbak.springboot;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * driud配置, 与application.properties配置重复
 * @author YSL
 * 2018-09-12 15:51
 */
//@Configuration
public class DriudConfig {
    /**
     * druid的监控
     * @author: YSL
     * 2018-09-12 16:33
     */
   // @Bean
    public ServletRegistrationBean servletRegistrationBean() {

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/original-service-provider/druid/*");

        Map< String, String > initParameters = new HashMap< String, String >();
        initParameters.put("resetEnable", "true"); // 禁用HTML页面上的“Rest All”功能

        //是否能够重置数据
        //servletRegistrationBean.addInitParameter("resetEnable","false");

        // session统计 监控
        initParameters.put("sessionStatEnable", "true");
        initParameters.put("sessionStatMaxCount", "1000");

        // 监控单个url调用的sql列表
        initParameters.put("profileEnable", "true");

        // ip白名单（没有配置或者为空，则允许所有访问）
//        if(Env.product.equals(DeployEnvManager.getEnv(Env.product))){
//            // 限制访问：生产环境vpn
//            initParameters.put("allow", "172.16.209.1");
//        }else{
//            // 限制访问：测试环境vpn
//            initParameters.put("allow", "172.16.209.131,127.0.0.1");
//        }
        // ip黑名单，如果某个ip同时存在，deny优先于allow
        initParameters.put("deny", "");

        initParameters.put("loginUsername", "admin"); // ++监控页面登录用户名
        initParameters.put("loginPassword", "admin"); // ++监控页面登录用户密码

        servletRegistrationBean.setInitParameters(initParameters);
        return servletRegistrationBean;
    }

    @Bean(initMethod = "init",destroyMethod = "close")
    @ConfigurationProperties(prefix="spring.datasource.druid")
    public DruidDataSource dataSource(){
        DruidDataSource dataSource=new DruidDataSource();
        //dataSource.setProxyFilters(Lists.newArrayList(statFilter()));
        return  dataSource;
    }

    //bean注解，成为spring的bean，利用filter将慢sql的日志打印出来
    //@Bean
    public Filter statFilter(){
        StatFilter statFilter=new StatFilter();
        //多长时间定义为慢sql，这里定义为5s
        statFilter.setSlowSqlMillis(5000);
        //是否打印出慢日志
        statFilter.setLogSlowSql(true);
        //是否将日志合并起来
        statFilter.setMergeSql(true);
        return  statFilter;
    }
}
