package test;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.lsscl.app.util.ApnsUtil;


public class ApnsTest {
	/*@Test
	public void test(){
		try
        {
            //从客户端获取的deviceToken，在此为了测试简单，写固定的一个测试设备标识。
          // String deviceToken = "2a467f33 0ab21821 0a86bd35 87c40046 3a1c6eda 97c64984 5e9fe36a 7c69aed8";
			String deviceToken = "627e4193 3cf59c55 cdefe11e b3948e47 43688851 0aa3e75f 3cd5d2e4 a46537b5";
			deviceToken = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";//线上
			deviceToken = "ac787497 015938ad ae436d43 1894c327 1f077f35 4e7a2106 6c152c11 163a1f31";//测试：赵远博
			deviceToken = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";//yxx 线上
			//ibox-dev yxx-ipad 
//			deviceToken = "e895628c edc0b69a a462c019 13a15bb2 d135d217 693b318f 475bff38 17083c33";
//			//ibox-pro yxx-ipad
//			deviceToken = "928eb1a7 f715ca4a 61211955 64936542 3a1a833e 75ea0d06 f083371f fe873c85";
            System.out.println("Push Start deviceToken:" + deviceToken);
            //定义消息模式
            PayLoad payLoad = new PayLoad();
            payLoad.addAlert("测试报警");
            payLoad.addBadge(1);//消息推送标记数，小红圈中显示的数字。
            payLoad.addSound("default");
            //注册deviceToken
            PushNotificationManager pushManager = PushNotificationManager.getInstance();
            pushManager.addDevice("iPhone", deviceToken);
            //连接APNS
            String host = "gateway.sandbox.push.apple.com";
            host = "gateway.push.apple.com";
            
            int port = 2195;
            String certificatePath = "E:\\share\\keys\\ibox_aps_production.p12";//前面生成的用于JAVA后台连接APNS服务的*.p12文件位置
            String certificatePassword = "admin_2014";//p12文件密码。
          
            //开发版
//            host = "gateway.push.apple.com";
            certificatePath = "E:\\share\\keys\\aps_production.p12";
            pushManager.initializeConnection(host, port, certificatePath, certificatePassword, SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
            //发送推送
            Device client = pushManager.getDevice("iPhone");
            System.out.println("推送消息: " + client.getToken()+"\n"+payLoad.toString() +" ");
            pushManager.sendNotification(client, payLoad);
            //停止连接APNS
            pushManager.stopConnection();
            //删除deviceToken
            pushManager.removeDevice("iPhone");
            System.out.println("Push End");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
	}*/
	
	@Test
	public void testTime(){
		System.out.println(new Date(1430708461000L));
		System.out.println(new Date(1430708406000L));
		System.out.println(new Date(1430708346000L));
	}

	@Test
	public void apnsTest(){
		
		
		
	}
	public static void main(String[] args) {
		String deviceToken = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";
		deviceToken = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";
		deviceToken = "c79c91bd f5c59532 46b5ae65 21467f3d 2761ccc2 b34fddc3 df047812 c491412c";
		String sound = "default";
		String message = "alert";
		ApnsUtil.pushDefaultNotification(deviceToken,"test");	
//		ApnsUtil.pushNoficationWithSound(deviceToken, null, message);
	}
}
