package com.yupi.springbootinit.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AImanager {
    @Resource
    private YuCongMingClient yuCongMingClient;
    public String doChat(String message)
    {
        // 构造请求
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1737488339726462978L);
        devChatRequest.setMessage(message);
        //处理响应内容
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        return (response.getData().getContent());
    }
}
