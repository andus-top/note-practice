package com.ysl.note.web;

import com.google.gson.Gson;
import com.ysl.note.web.bean.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * web(j2ee、框架)相关的一些记录
 * @author YSL
 * 2019-02-01 16:08
 */
public class Note {

    Logger logger = LoggerFactory.getLogger(Note.class);

    /**
     * 返回json字符串
     * @author YSL
     * 2019-02-01 16:18
     */
    public String returnJsonStr(HttpServletRequest request, HttpServletResponse response){
        Result result = new Result();

        result.setMsg("成功");
        result.setStatus(true);
        result.setData(new Object());

        Gson gson = new Gson();
        String json = gson.toJson(result);

        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        BufferedOutputStream bf = null;
        try {
            bf = new BufferedOutputStream(response.getOutputStream());
            bf.write(json.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
            } catch (IOException io) {
                logger.info(io.getMessage(), io);
            }
        }

        return bf.toString();
    }

    /**
     * 返回json字符串
     * @author YSL
     * 2019-02-01 16:18
     */
    public void returnJosnStr2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Result result = new Result();

        result.setMsg("成功");
        result.setStatus(true);
        result.setData(new Object());

        Gson gson = new Gson();
        String json = gson.toJson(result);

        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }


}
