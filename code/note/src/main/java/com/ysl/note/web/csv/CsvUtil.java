package com.ysl.note.web.csv;

import com.csvreader.CsvWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * @author YSL
 * 2019-02-13 15:46
 */
public class CsvUtil {

    /*
     * 自定义write csv 接口
     */
    public interface WriteCsv {
        void write(CsvWriter csvWriter) throws Exception;
    }

    /**
     * 导出csv(对外统一接口)
     * @param  fileName 文件名称(不包括.csv后缀)
     * @param  response
     * @param  writeCsv 自定义接口，需实现write方法
     * @return 直接将csv返回给前端
     * @author YSL
     * 2018-11-01 20:56
     */
    public static void writeCsv(String fileName, HttpServletResponse response, WriteCsv writeCsv){

        try {
            // 创建临时文件
            File tempfile = File.createTempFile(fileName, ".csv");

            // 创建 CsvWriter 对象
            CsvWriter csvWriter = new CsvWriter(tempfile.getCanonicalPath(),',', Charset.forName("GBK"));

            // 接口需要实现的方法
            writeCsv.write(csvWriter);

            // 关闭 CsvWriter 对象
            csvWriter.close();

            // 获取写入数据的临时文件
            File fileLoad=new File(tempfile.getCanonicalPath());

            // 返回给前端
            CsvUtil.downLoadCsv(fileName+".csv", fileLoad, response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 导出csv文件
     * @param fileName 文件名称，含后缀
     * @param fileLoad 要输出的文件
     * @param response
     * @author YSL
     * 2018-09-25 11:07
     */
    public static void downLoadCsv(String fileName, File fileLoad, HttpServletResponse response) {

        try {
            byte[] b=new byte[1024 * 1024];
            OutputStream out=response.getOutputStream();
            response.reset();
            response.setContentType("application/csv");
            //re.setContentType("application/x-msdownload;");
            response.setHeader("content-disposition", "attachment; filename="+ URLEncoder.encode(fileName,"UTF-8"));
            Long filelength=fileLoad.length();
            response.setHeader("Content_Length",String.valueOf(filelength));
            FileInputStream fileInputStream=new FileInputStream(fileLoad);
            int n;
            while ((n = fileInputStream.read(b)) != -1) {
                out.write(b, 0, n); //每次写入out1024字节
            }
            // System.out.println(tempfile.getCanonicalPath());
            fileInputStream.close();
            out.flush();
            out.close();
            System.out.println("export"+fileName+"success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
