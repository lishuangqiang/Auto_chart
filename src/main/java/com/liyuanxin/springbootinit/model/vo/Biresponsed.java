package com.liyuanxin.springbootinit.model.vo;

import lombok.Data;

/*
 * Bi返回接轨
 * */
@Data
public class Biresponsed {
    /*
     * 图标代码
     * */
    private String genChart;

    /*
     * 分析结果
     * */
    private String genResult;

    /*
     * 图表ID
     * */
    private String genChartId;
    /*
     * 图表状态
     * */
    private String genChartStatus;

}
