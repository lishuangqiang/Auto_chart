package com.liyuanxin.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liyuanxin.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【chart(图表信息表)】的数据库操作Mapper

*/
public interface ChartMapper extends BaseMapper<Chart> {
   List<Map<String,Object>> queryChartData(String querySql);
}




