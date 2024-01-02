package com.liyuanxin.springbootinit;

import com.liyuanxin.springbootinit.config.WxOpenConfig;
import com.liyuanxin.springbootinit.mapper.ChartMapper;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * 主类测试

 */
@SpringBootTest
class MainApplicationTests {
    @Resource
    private YuCongMingClient client;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private ChartMapper chartMapper;


    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }

    @Test
    public void test1() {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1737488339726462978L);
        devChatRequest.setMessage("你好");
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        System.out.println(response.getData().getContent());
    }

    @Test
    void query() {
        String chartId = "123456";
        String querySql = String.format("select * from chart_%s", chartId);
        List<Map<String, Object>> resultData = chartMapper.queryChartData(querySql);
        System.out.println(resultData);
    }
}