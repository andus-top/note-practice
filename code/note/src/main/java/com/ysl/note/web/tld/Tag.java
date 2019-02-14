package com.ysl.note.web.tld;

import jodd.util.StringUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 日期格式化标签
 */
public class Tag extends TagSupport {

    private static final long serialVersionUID = 1L;

    private String val;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public int doStartTag() throws JspException {
        String outStr = "无数据";
        if (!StringUtil.isEmpty(val)) {
            Long time = Long.valueOf(val.trim());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            outStr = dateformat.format(c.getTime());
        }
        try {
            // pageContext获取当前的JspContext对象，并将outStr传递给JspWriter对象
            pageContext.getOut().write(outStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.doStartTag();
    }

}
