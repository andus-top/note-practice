package com.ysl.note.web.configbak.springboot;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger2配置信息
 * @author YSL
 * 2018-09-06 17:53
 */
@EnableSwagger2
@Configuration
public class Swagger2Config {
    // 参考：https://blog.csdn.net/xiaojin21cen/article/details/78653506
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //为当前包路径
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 构建 api文档的详细信息函数
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("接口文档")
                //创建人20
                .contact(new Contact("YSL", "https://github.com/songlinsheep", "614580167@qq.com"))
                //版本号
                .version("1.0")
                //描述
                .description("数据接口，多以json进行数据传输")
                .build();
    }
}