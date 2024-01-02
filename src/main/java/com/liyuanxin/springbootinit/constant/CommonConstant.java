package com.liyuanxin.springbootinit.constant;

/**
 * 通用常量

 */
public interface CommonConstant {

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";

    /*
    * python代码
    * */
    String Python_code ="import http.client\n" +
            "import json\n" +
            "conn = http.client.HTTPSConnection(\"api.binjie.fun\")\n" +
            "headers ={\"User-Agent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36\",\"Referer\":\"https://chat18.aichatos.xyz/\",\"Origin\":\"https://chat18.aichatos.xyz\",\"Sec-Fetch-Dest\":\"empty\",\"Sec-Fetch-Mode\":\"cors\",\"Sec-Fetch-Site\":\"cross-site\",\"Content-Type\":\"application/json\"}\n" +
            "payload = {\"prompt\": \"你好\",\"userId\": \"#/chat/1700312908088\",\"network\": True,\"system\": \"\",\"withoutContext\": False}\n" +
            "payload_str = json.dumps(payload)\n" +
            "conn.request(\"POST\", \"/api/generateStream?refer__1360=n4%2Bh0KGKYveUOrDuDBqDqpBjFgjdgYxWwD\", body=payload_str, headers=headers)\n" +
            "response = conn.getresponse()\n" +
            "print(response.status)\n" +
            "print(response.read().decode())\n" +
            "conn.close()";
}
