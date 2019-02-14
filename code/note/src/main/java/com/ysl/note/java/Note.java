package com.ysl.note.java;

/**
 * java 相关的一些错误或坑
 * @author YSL
 * 2019-02-01 20:29
 */
public class Note {

    public static void main(String[] args) {
        // 两种空格
        diffSpace();
    }

    /**
     * 两种不同的空格
     * @author YSL
     * 2019-02-01 20:36
     */
    public static void diffSpace(){
        //按下【键盘Alt+数字键‘3’+数字键‘2’ 然后松开】，发现打出了空格，没错打出来了，这就是空格，ASCII中数字：32.
        //按下【键盘Alt+数字键‘1’+数字键‘6’+数字键‘0’ 然后松开】，发现打出了空格，没错打出来了，这就是空格，ASCII中数字：160.
        char a=' ';  // ASCII 32。trim()能去掉。
        char b=' '; //  ASCII 160。trim()不能去掉。网页由 &nbsp; 生成的空格就是这种。non-breaking space的缩写正是nbsp。这中空格的作用就是在页面换行时不被打断
        System.out.println((int)a);//32
        System.out.println((int)b);// 160

        String s32 = " string ";
        String s160 = " string ";
        System.out.println("原字符串长度："+s32.length());// 8
        System.out.println("原字符串长度："+s160.length());// 8

        System.out.println("trim()后字符串长度："+s32.trim().length());// 6
        System.out.println("trim()后字符串长度："+s160.trim().length());// 8

        // 替换掉160空格
        String rps = s160.replaceAll("[\\s\\u00A0]+"," ");
        System.out.println("替换后的字符串trim()后长度："+rps.trim().length());// 6

    }
}
