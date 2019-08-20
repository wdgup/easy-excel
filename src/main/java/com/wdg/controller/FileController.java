package com.wdg.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.fastjson.JSONObject;
import com.wdg.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: wangdaogang
 * Date: 2019/8/20
 * Description: No Description
 */
@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

    private String[] parttern = {"xlsx","xls"};

    private String writePath = "D:\\test\\";
    private String copyPath = "D:\\copyPath\\";
    @PostMapping("/import")
    @ResponseBody
    public Map<String,Object> importFile(MultipartFile file) throws Exception{
        Map<String,Object> map = new HashMap();
        //判断文件的格式
        String filename = file.getOriginalFilename();
        String[] split = filename.split("\\.");
        String suffix = split[split.length-1];
        List<String> collect = Stream.of(parttern).filter(str -> str.indexOf(suffix) >= 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)){
            map.put("message","failed");
            map.put("code",500);
            return map;
        }
        String newFileName = UUID.randomUUID().toString()+"."+suffix;
        String url = writePath+newFileName;
        file.transferTo(new File(url));
        FileInputStream fis = new FileInputStream(new File(writePath+newFileName));
        FileOutputStream fos = new FileOutputStream(new File(copyPath+newFileName));
        //字节流
        byte[] bytes = new byte[1024];
        int legth;
        while ((legth=fis.read(bytes))!=-1){
            fos.write(legth);
        }
        fos.close();
        fis.close();
        //下面开始解析 excel
        resolutExcel(new File(url));
        map.put("message","success");
        map.put("code",200);
        map.put("url",url);
        return map;
    }

    private void resolutExcel(File file) throws Exception {
        InputStream inputStream = new FileInputStream(file);
        List<Object> read = EasyExcelFactory.read(inputStream, new Sheet(1, 1, Product.class));
        List<Product> products = read.stream().map(obj-> (Product)obj).collect(Collectors.toList());
        log.info("products:{}", JSONObject.toJSONString(products));
    }

    @RequestMapping("")
    public String toImportHtml(){
        return "index";
    }
}
