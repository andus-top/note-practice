package com.ysl.note.web.util;

import jodd.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * @author YSL
 * 2018-09-20 21:15
 */
public class DateUtil {

    /**
    * yyyy-MM-dd HH:mm:ss
    */
    public static SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
    * yyyy-MM-dd HH:mm:ss SSS
    */
    public static SimpleDateFormat YMDHMSSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    /**
    * yyyyMMdd
    */
    public static SimpleDateFormat YMD = new SimpleDateFormat("yyyyMMdd");
    /**
    * yyyy-MM-dd
    */
    public static SimpleDateFormat Y_M_D = new SimpleDateFormat("yyyy-MM-dd");
    /**
    * yyyy-MM
    */
    public static SimpleDateFormat Y_M = new SimpleDateFormat("yyyy-MM");
    /**
     * yyyy/MM/dd
     */
    public static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * 多少小时多少分钟
     */
    public static String formatTime = "%s h %s m";

    /**
     * 字符串日期格式化
     * @param str 字符串日期
     * @param sdf SimpleDateFormat obj
     * @return 格式化后的日期
     * @author YSL
     * 2018-09-21 10:34
     */
    public static String formatDate(String str, SimpleDateFormat sdf){
        if(StringUtil.isEmpty(str) || sdf == null){
            return null;
        }
        Date date = strToDate(str, sdf);
        String formatDate = sdf.format(date);
        return formatDate;
    }

    /**
     * 日期格式化
     * @param date 日期
     * @param sdf SimpleDateFormat obj
     * @return 格式化后的日期
     * @author YSL
     * 2018-09-21 10:34
     */
    public static String formatDate(Date date, SimpleDateFormat sdf){
        if(date == null || sdf == null){
            return null;
        }
        String formatDate = sdf.format(date);
        return formatDate;
    }

    /**
     * 字符串转换成日期
     * @param str
     * @param sdf  SimpleDateFormat obj
     * @return date
     */
    public static Date strToDate(String str, SimpleDateFormat sdf) {
        if(StringUtils.isBlank(str) || sdf == null){
            return null;
        }
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    /**
     * 日期转成字符串
     * @param date
     * @param sdf SimpleDateFormat obj
     * @author YSL
     * 2018-09-21 14:55
     */
    public static String dateToString(Date date, SimpleDateFormat sdf){
        if(date == null || sdf == null){
            return null;
        }
        String tm = sdf.format(date);
        return tm;
    }

    /**
     * 指定日期加上天数后的日期
     * @param num 为增加的天数
     * @param date 创建时间
     * @param  sdf SimpleDateFormat obj
     * @return 变化后的时间
     * @throws ParseException
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String addDay(String date, int num, SimpleDateFormat sdf){
        if(StringUtil.isEmpty(date) || sdf == null ){
            return null;
        }
        Date tm = strToDate(date, sdf);
        String endDay = addDay(tm, num, sdf);
        return endDay;
    }

    /**
     * 指定日期加上天数后的日期
     * @param num 为增加的天数
     * @param date 创建时间
     * @param sdf SimpleDateFormat obj
     * @return 变化后的时间
     * @throws ParseException
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String addDay(Date date, int num, SimpleDateFormat sdf){
        if(date == null || sdf == null ){
            return null;
        }

        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.DATE, num);// num为增加的天数
        date = ca.getTime();
        String enddate = sdf.format(date);
        return enddate;
    }

    /**
     * 获取指定日期所在月份第一天
     * @param  date 指定日期
     * @param  sdf SimpleDateFormat obj
     * @return tmFormat格式，字符串的时间
     * @throw
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String getMonthBegin(String date, SimpleDateFormat sdf){
        if(StringUtil.isEmpty(date) || sdf == null ){
            return null;
        }
        Date tm = strToDate(date, sdf);
        String monthBegin = getMonthBegin(tm, sdf);
        return monthBegin;
    }

    /**
     * 获取指定日期所在月份第一天
     * @param  date 指定日期
     * @param  sdf SimpleDateFormat obj
     * @return tmFormat格式，字符串的时间
     * @throw
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String getMonthBegin(Date date, SimpleDateFormat sdf) {

        if(date == null || sdf == null){
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND,0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天日期
        String beginDate = sdf.format(c.getTime());

        return beginDate;
    }

    /**
     * 获取指定日期所在月份最后一天
     * @param  date 指定日期
     * @param  sdf SimpleDateFormat obj
     * @return tmFormat格式，字符串的时间
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String getMonthEnd(String date, SimpleDateFormat sdf){
        if(StringUtil.isEmpty(date) || sdf == null ){
            return null;
        }
        Date tm = strToDate(date, sdf);
        String monthEnd = getMonthEnd(tm, sdf);
        return monthEnd;
    }

    /**
     * 获取指定日期所在月份最后一天
     * @param  date 指定日期
     * @param  sdf SimpleDateFormat obj
     * @return tmFormat格式，字符串的时间
     * @author YSL
     * 2018-09-21 09:53
     */
    public static String getMonthEnd(Date date, SimpleDateFormat sdf) {
        if(date == null || sdf == null){
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND,59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天
        String endDate = sdf.format(c.getTime());

        return endDate;
    }

    /**
     * 获取某日期所在周的开始日期
     * @param dateStr   当前日期
     * @param weekBeginDayFlag 设定以周几作为一周的起始日期（周天为1，周一为2，以此类推）
     * @param sdf 时间格式
     * @throws ParseException
     */
    public static String getWeekBegin(String dateStr, int weekBeginDayFlag, SimpleDateFormat sdf){
        return getWeekBegin(strToDate(dateStr, sdf), weekBeginDayFlag, sdf);
    }


    /**
     * 获取某日期所在周的开始日期
     * @param date   当前日期
     * @param weekBeginDayFlag 设定以周几作为一周的起始日期（周天为1，周一为2，以此类推）
     * @param sdf 时间格式
     * @throws ParseException
     */
    public static String getWeekBegin(Date date, int weekBeginDayFlag, SimpleDateFormat sdf) {

        if(date == null) return null;

        // 获取当周的第一天
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek >= weekBeginDayFlag) {
            dayofweek -= 7;
        }
        cal.add(Calendar.DAY_OF_MONTH, (weekBeginDayFlag-7) - dayofweek);
        return sdf.format(cal.getTime());
    }

    /**
     * 获取某日期所在周的开始日期
     * @param dateStr
     * @param weekBeginDayFlag 设定以周几作为一周的起始日期（周天为1，周一为2，以此类推）
     * @param sdf 时间格式
     * @return
     * @throws ParseException
     */
    public static String getWeekEnd(String dateStr, int weekBeginDayFlag, SimpleDateFormat sdf) {
        return getWeekEnd(strToDate(dateStr, sdf), weekBeginDayFlag, sdf);
    }

    /**
     * 获取某日期所在周的开始日期
     * @param date
     * @param weekBeginDayFlag 设定以周几作为一周的起始日期（周天为1，周一为2，以此类推）
     * @param sdf 时间格式
     * @return
     * @throws ParseException
     */
    public static String getWeekEnd(Date date, int weekBeginDayFlag, SimpleDateFormat sdf) {

        if(date == null) return null;

        String weekBeginDay = getWeekBegin(date, weekBeginDayFlag, sdf);
        Date datetime = strToDate(weekBeginDay, sdf);

        if(datetime == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(datetime);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        return sdf.format(cal.getTime());
    }


    /**
     * Date 转 LocalDate
     * @param date java.util.Date
     * @return LocalDate
     * @author YSL
     * 2019-01-10 18:07
     */
    public static LocalDate toLocalDate(Date date) {
        if(date == null) return null;
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        LocalDate localDate = localDateTime.toLocalDate();
        return localDate;
    }

    /**
     * Date 转 LocalDate
     * @param date 字符串日期
     * @param sdf 日期格式
     * @return LocalDate
     * @author YSL
     * 2019-01-10 18:06
     */
    public static LocalDate toLocalDate(String date, SimpleDateFormat sdf) {
        return toLocalDate(strToDate(date, sdf));
    }

    /**
     * 天数差
     * @param localDate1 小日期
     * @param localDate2 大日期
     * @return 天数差
     * @author YSL
     * 2019-01-10 18:16
     */
    public static Integer differentDays(LocalDate localDate1, LocalDate localDate2){
        if(localDate1 == null || localDate2 == null) return null;
        Long diff = ChronoUnit.DAYS.between(localDate1, localDate2);
        return diff.intValue();
    }

    /**
     * 天数差
     * @param date1 小日期
     * @param date2 大日期
     * @return 天数差
     * @author YSL
     * 2019-01-10 19:03
     */
    public static Integer differentDays(Date date1, Date date2){
        Integer diff = differentDays(toLocalDate(date1), toLocalDate(date2));
        return diff;
    }

    /**
     * 天数差
     * @param date1 小日期 java.lang.String
     * @param date2 大日期 java.lang.String
     * @return 天数差
     * @author YSL
     * 2019-01-10 19:03
     */
    public static Integer differentDays(String date1, String date2, SimpleDateFormat sdf){
        Integer diff = differentDays(toLocalDate(strToDate(date1, sdf)), toLocalDate(strToDate(date2, sdf)));
        return diff;
    }

    /**
     * 天数差
     * @param date1 小日期, java.lang.String
     * @param date2 大日期 java.util.Date
     * @return 天数差
     * @author YSL
     * 2019-01-10 19:03
     */
    public static Integer differentDays(String date1, Date date2, SimpleDateFormat sdf){
        Integer diff = differentDays(toLocalDate(strToDate(date1, sdf)), toLocalDate(date2));
        return diff;
    }

    /**
     * 天数差
     * @param date1 小日期, java.lang.String
     * @param date2 大日期 java.util.Date
     * @return 天数差
     * @author YSL
     * 2019-01-10 19:03
     */
    public static Integer differentDays(Date date1, String date2, SimpleDateFormat sdf){
        Integer diff = differentDays(toLocalDate(date1), toLocalDate(strToDate(date2, sdf)));
        return diff;
    }

    /**
     * 判断当前日期是星期几
     * <p>星期一--------1</p>
     * <p>星期天--------7</p>
     * @param date
     * @return 出错返回0
     * @author YSL
     * 2018-11-08 17:38
     */
    public static int dayOfWeek(String date, SimpleDateFormat sdf){
        if(date == null || sdf == null){
            return 0;
        }
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        int dayForWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayForWeek = 7;
        } else {
            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayForWeek;
    }

    /**
     * 日期转 时间戳
     * @param s
     * @return
     * @throws ParseException
     */
    public static String dateToStamp(String s) throws ParseException {
        String res;
        Date date = YMDHMS.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;

    }

    /**
     * 时间戳转日期
     * @param s
     * @return
     */
    public static String stampToDate(String s){
        long lt = new Long(s);
        Date date = new Date(lt);
        return YMDHMS.format(date);
    }

    public static String stampToDate(Long l, SimpleDateFormat sdf){
        Date date = new Date(l);
        return sdf.format(date);
    }

    /**
     * 时间格式转换
     * @param date 时间
     * @param date 原来格式
     * @param date 目标格式
     * @return 字符串格式时间
     * @author YSL
     * 2/17/19 7:56 PM
     */
    public static String changeFormat(String date,SimpleDateFormat src,SimpleDateFormat dest){
        try {
            Date parse = src.parse(date);
            String format = dest.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    /**
     * 将传入的日期反转，比如20181025反转成52018102
     */
    public static String reverseDate(String s) {
        char[] array = s.toCharArray();
        String reverse = "";
        //倒叙拿出
        for (int i = array.length - 1; i >= 0; i--)
            reverse += array[i];
        return reverse;
    }

    /**
     * 毫秒转分钟数
     * @param millisecond 毫秒
     * @return
     */
    public static long getMin(long millisecond){
        return millisecond / 1000 * 60;
    }

    /**
     * 分转小时
     * @param min 分钟
     * @return
     */
    public static long getHours(long min){
        return min / 60;
    }

    /**
     * 毫秒转换为 xxx h(小时)  xxx m(分钟)
     * @param millisecond 毫秒
     */
    public static String getHourAndMin(long millisecond) {
        long min = getMin(millisecond);
        long hours = getHours(min);
        min = min - hours * 60;
        return String.format(formatTime, hours, min);
    }


}
