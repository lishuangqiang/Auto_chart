package com.liyuanxin.springbootinit.model.dto.chart;

import com.liyuanxin.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {


    /**
     * 图表名称
     */
    private String name;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;


    /**
     * 图表类型
     */
    private String chartType;


    /**
     * 用户ID
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}