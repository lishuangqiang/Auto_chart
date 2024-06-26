package com.liyuanxin.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyuanxin.springbootinit.mapper.ChartMapper;
import com.liyuanxin.springbootinit.model.entity.Chart;
import com.liyuanxin.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-12-16 23:23:59
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




