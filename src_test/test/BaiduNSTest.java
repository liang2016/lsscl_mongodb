package test;

import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;

public class BaiduNSTest {
    public static void main(String[]args) {

        /*
         * @brief 向Android端设备推送单播消息
         * message_type = 1
         * device_type = 3
         */

        // 1. 设置developer平台的ApiKey/SecretKey
        String apiKey = "x8jSKUepDGUe9kFDlt0FPoBQ";
        String secretKey = "ODPiO37j90ZlTK4kYKu3NHHn55ksbEdl";
        ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);

        // 2. 创建BaiduChannelClient对象实例
        BaiduChannelClient channelClient = new BaiduChannelClient(pair);

        // 3. 若要了解交互细节，请注册YunLogHandler类
        channelClient.setChannelLogHandler(new YunLogHandler() {
            @Override
            public void onHandle(YunLogEvent event) {
                System.out.println(event.getMessage());
            }
        });

        try {

            // 4. 创建请求类对象
            PushUnicastMessageRequest request = new PushUnicastMessageRequest();
            request.setDeviceType(3);
            //通过Android客户端获取channelId、UserId
            request.setChannelId(3858916015566064571L);
            request.setUserId("607069001780124845");
            request.setMessageType(1);
            request.setMessage("{\"title\":\"Notify_title_danbo\",\"description\":\"Notify_description_content\",\"notification_basic_style\":2}");

            // 5. 调用pushMessage接口
            PushUnicastMessageResponse response = channelClient
                    .pushUnicastMessage(request);

//            Assert.assertEquals(1, response.getSuccessAmount());

        } catch (ChannelClientException e) {
            // 处理客户端错误异常
            e.printStackTrace();
        } catch (ChannelServerException e) {
            // 处理服务端错误异常
            System.out.println(String.format(
                    "request_id: %d, error_code: %d, error_message: %s",
                    e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
        }

    }
}
