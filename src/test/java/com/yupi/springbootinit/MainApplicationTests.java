package com.yupi.springbootinit;

import com.yupi.springbootinit.config.WxOpenConfig;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * 主类测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class MainApplicationTests {
    @Resource
    private YuCongMingClient client;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }
    @Test
    public void test1()  {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1737488339726462978L);
        devChatRequest.setMessage("你好");
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        System.out.println(response.getData().getContent());
    }
}