package com.lsscl.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lsscl.app.bean.QC;

public class AndroidNS {
	
	private static final String appKey =AcpConfig.env.getProperty("appKey");
	private static final String masterSecret = AcpConfig.env.getProperty("masterSecret");
	private static final Log LOG = LogFactory.getLog(AndroidNS.class);
	/**
	 * 云推送
	 * @param userid Android登录用户设备id
	 * @param title 标题 
	 * @param content 通知内容
	 */
	public static void notification(String userid,String title,String content){
		// HttpProxy proxy = new HttpProxy("localhost", 3128);
		// Can use this https proxy: https://github.com/Exa-Networks/exaproxy
		JPushClient jpushClient = new JPushClient(masterSecret, appKey, 3);

		// For push, all you need do is to build PushPayload object.
		PushPayload payload = PushPayload.alertAll(content,userid);

		try {
			PushResult result = jpushClient.sendPush(payload);
			LOG.info("Got result - " + result);

		} catch (APIConnectionException e) {
			LOG.error("Connection error. Should retry later. ", e);

		} catch (APIRequestException e) {
			LOG.error("Error response from JPush server. Should review and fix it. ", e);
			LOG.info("HTTP Status: " + e.getStatus());
			LOG.info("Error Code: " + e.getErrorCode());
			LOG.info("Error Message: " + e.getErrorMessage());
			LOG.info("Msg ID: " + e.getMsgId());
		}
	}

	/**
	 * 根据loginQC的msgType获取百度云基本推送类型（响铃 震动 通知可以被清除）
	 * @param flag “声音"（1）, "振动"（2）, "22:00-8:00 静音”（3）
	 * @return 响铃 震动 通知可以被清除（二进制转10进制（1选中、 0未选中））
	 */
	public static int getBasicType(String flag){
		SimpleDateFormat df = new SimpleDateFormat("HH");
		int hour = Integer.parseInt(df.format(new Date()));
		boolean isDuringNight = hour<8 || hour>22;
		if("0".equals(flag)){ //对应123（三项全有）指定时间内静音
			return isDuringNight?3:7;
		}else if("1".equals(flag)){// 对应12
			return 7;
		}else if("2".equals(flag)){// 对应13
			return isDuringNight?1:5;
		}else if("3".equals(flag)){// 对应23 指定时间振动
			return isDuringNight?1:3;
		}else if("4".equals(flag)){// 对应1 只有声音
			return 5;
		}else if("5".equals(flag)){// 对应2 只有振动
			return 3;
		}else if("6".equals(flag)){// 对应3（相当于是全静音模式）
			return 1;
		}
		return -1;
	}
	/**
	 * 发送通知
	 * @param loginQC
	 */
	public static void notification(QC loginQC){
    	String userId = loginQC.getImsi();
    	String channel = loginQC.getImei();
    	
    }
}
