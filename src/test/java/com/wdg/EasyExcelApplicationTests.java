package com.wdg;


import com.alibaba.excel.metadata.Sheet;
import com.wdg.entity.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.excel.EasyExcelFactory.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EasyExcelApplicationTests {

    private String filePath = "C:\\Users\\jm005227\\Desktop\\ck测试\\1.xlsx";
    @Test
    public void contextLoads() throws Exception {

        InputStream inputStream = new FileInputStream(new File(filePath));

        List<Object> read =  read(inputStream, new Sheet(1, 1));
        read.forEach(obj-> System.out.println(obj+"\n"));
       // castListClassInfo(read,Product.class);
       List<Product> products = new ArrayList<>();
        for (int i = 0; i < read.size() ; i++) {
            products.add(new Product());
            setPropertiesToObj(read.get(i), products.get(i));
        }
        System.out.println(products);
    }

    private static Object setPropertiesToObj(Object souce,Object target) throws Exception {
            Field[] declaredFields = target.getClass().getDeclaredFields();
            List<String> values = (List<String>) souce;
            for (int i = 0; i < declaredFields.length; i++) {
                String sourceName = declaredFields[i].getName();
                Class<?> type = declaredFields[i].getType();
                Method method = null;
                method = target.getClass().getDeclaredMethod("set" + sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1), type);
                method.invoke(target,values.get(i));
            }
            return target;
    }
    private static  void castListClassInfo(Object source,Class<?> target) throws NoSuchMethodException {
        Field[] sourceFields = source.getClass().getDeclaredFields();
        Field[] targetFields = target.getDeclaredFields();
        if(sourceFields.length != targetFields.length){
            throw new RuntimeException("source field length target properties length");
        }
        for (int i = 0; i < sourceFields.length; i++) {

        }
    }


}
