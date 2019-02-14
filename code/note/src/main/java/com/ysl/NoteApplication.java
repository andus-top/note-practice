package com.ysl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author YSL
 * 2019-02-02 12:32
 */
//@EnableTransactionManagement// 开启注解事务
@SpringBootApplication // @Configuration + @EnableAutoConfiguration + @ComponentScan。
public class NoteApplication {

    public static void main (String[]args){
        SpringApplication.run(NoteApplication.class, args);
    }
}
