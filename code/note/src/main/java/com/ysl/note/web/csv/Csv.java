package com.ysl.note.web.csv;

import com.csvreader.CsvWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 创建csv
 * @author YSL
 * 2019-02-13 15:30
 */
public class Csv {

    public static void main(String[] args) {
        csv();
    }

    /**
     * java代码生成csv
     */
    public static void csv(){
        String filePath = "C:\\Users\\ysl\\Desktop\\test.csv";

        try {
            // 创建CSV写对象
            CsvWriter csvWriter = new CsvWriter(filePath, ',', Charset.forName("GBK"));
            //CsvWriter csvWriter = new CsvWriter(filePath);
            // 表头
            String[] headers = {"pk","c1","\n"};
            csvWriter.writeRecord(headers);
            csvWriter.writeRecord(new String[]{"test1" , "test2"+ "\n"});
            csvWriter.writeRecord(new String[]{"test3" , "test4"+ "\n"});
            csvWriter.endRecord();
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 网页中返回csv
     * @param response
     */
    public static void responseCsv(HttpServletResponse response){
        CsvUtil.writeCsv("文件名称", response, new CsvUtil.WriteCsv(){
            @Override
            public void write(CsvWriter csvWriter) throws IOException{
                csvWriter.writeRecord(new String[]{"表头1","表头2"});// csv表头

                csvWriter.write("数据");
                csvWriter.write("数据2");
                csvWriter.endRecord();
            }
        });
    }


}
