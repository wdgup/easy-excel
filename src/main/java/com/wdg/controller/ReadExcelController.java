package com.wdg.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.wdg.entity.Product;
import com.wdg.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.ContextLoader;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/excel")
@Slf4j
public class ReadExcelController {

    @Autowired
    private RedisTemplate redisTemplate;
    private String filePath = "C:\\Users\\jm005227\\Desktop\\ck测试\\1.xlsx";
    private String outputPath = "C:\\Users\\jm005227\\Desktop\\ck测试\\2.xlsx";
    private String outputPath1 = "C:\\Users\\jm005227\\Desktop\\ck测试\\3.xlsx";
    private String[] outPutFields = {"货号","LOGO","品类","色号","颜色","款号","品名","专柜价"};
    @Autowired
    private ServletContext servletContext;
    @GetMapping("/import")
    @ResponseBody
    private Object importProduct()  {
        try {
            InputStream inputStream = new FileInputStream(new File(filePath));
            List<Object> read = EasyExcelFactory.read(inputStream, new Sheet(1, 1));

            List<Product> products = new ArrayList<>();
            for (int i = 0; i < read.size(); i++) {
                products.add(new Product());
                ReflectUtil.setPropertiesToObj(read.get(i),products.get(i));
            }
            //将数据写入Redis
            writeProductInfoToRedis(products);
            return products;
        }catch (Exception e){
            e.printStackTrace();
            return "failed";
        }
    }

    private String writeProductInfoToRedis(List<Product> products){
        Optional.ofNullable(products).ifPresent(pro ->redisTemplate.boundValueOps("productInfo").set(pro));
        return "ok";
    }

    @GetMapping("/getObjeFromRedis/{key}")
    @ResponseBody
    private Object getObjeFromRedis(@PathVariable(name = "key") String key){
        if(StringUtils.isEmpty(key)){
            throw new RuntimeException("key must be not null");
        }
        return redisTemplate.boundValueOps(key).get();
    }

    @GetMapping("/write")
    @ResponseBody
    private Object writeModelToExcel(){
        try {
            List<Product> products= (List<Product>) redisTemplate.boundValueOps("productInfo").get();
            OutputStream outputStream = new FileOutputStream(outputPath);
            ExcelWriter writer = EasyExcelFactory.getWriter(outputStream);
            Sheet sheet = new Sheet(1,0,Product.class);
            sheet.setSheetName("商品文案");
            writer.write(products,sheet);
            writer.finish();
            outputStream.close();
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return "faied";
        }
    }


    @GetMapping("/writeExcel")
    private void writeExcel(HttpServletRequest request, HttpServletResponse response){
        FileOutputStream outputStream = null;
        XSSFWorkbook workbook = null;
        List<Product> products = (List<Product>) redisTemplate.boundValueOps("productInfo").get();
        //将集合转化为二维集合
        List<List> collect = products.stream().map(product -> {
            try {
                Class<? extends Product> productClass = product.getClass();
                Field[] declaredFields = productClass.getDeclaredFields();
                List list = new ArrayList();
                for (Field field : declaredFields) {
                    String name = field.getName();
                    list.add(productClass.getDeclaredMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1)).invoke(product));
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
        log.info("productInfo:{}",JSONObject.toJSONString(collect));
        try {
            //1.在内存中创建一个excel文件
            outputStream = new FileOutputStream(outputPath1);
            workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("商品信息");
            XSSFRow title = sheet.createRow(0);
            //创建标题行
            for (int i = 0; i < outPutFields.length; i++) {
                title.createCell(i).setCellValue(outPutFields[i]);
            }
            //写入数据
            for (List list : collect) {
                String[] objects = (String[]) list.toArray(new String[list.size()]);
                //一行 一行写入
                XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                for (int i = 0; i < objects.length; i++) {
                    row.createCell(i).setCellValue(objects[i]);
                }
            }
            //10.写出文件,关闭流
           /* workbook.write(outputStream);
            workbook.close();*/
            //设置信息头
            String fileName = "商品信息.xlsx";
            // 设置浏览器默认打开的时候采用的字符集
            response.setHeader("Content-Type", "text/html;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename="+fileName);
            ServletOutputStream out = response.getOutputStream();
            /*InputStream ins = new FileInputStream(outputPath);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len= ins.read(buffer))!= 0){
                out.write(buffer,0,len);
            }*/
            workbook.write(outputStream);
            workbook.write(out);
            workbook.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
