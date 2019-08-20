package com.wdg.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseRowModel implements Serializable {
    @ExcelProperty(value = "货号",index = 0)
    private String proNum;
    @ExcelProperty(value = "LOGO",index = 1)
    private String logo;
    @ExcelProperty(value = "品类",index = 2)
    private String proType;
    @ExcelProperty(value = "色号",index = 3)
    private String proColorEn;
    @ExcelProperty(value = "颜色",index = 4)
    private String proColorCn;
    @ExcelProperty(value = "款号",index = 5)
    private String proStyleNum;
    @ExcelProperty(value = "品名",index = 6)
    private String proName;
    @ExcelProperty(value = "专柜价",index = 7)
    private String proPrice;
}
