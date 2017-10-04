package com.lsscl.app.util;

import java.util.Date;
import java.util.List;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

import com.lsscl.app.bean.PushEvents;

public class ApnsUtil {

	public static void pushDefaultNotification(String deviceToken,
			String message) {
		pushNoficationWithSound(deviceToken, "default", message);
	}

	public static void pushNoficationWithSound(String deviceToken,
			String sound, String message) {
		
		deviceToken = deviceToken.replace(" ", "");
		System.out.println("ios push:"+deviceToken);
		try {
			PushNotificationPayload payload = PushNotificationPayload.complex();
			payload.addAlert(message);
			payload.addBadge(1);
			if(sound!=null){
				payload.addSound(sound);
			}
			System.out.println(payload.toString());
			String path = AcpConfig.env.getProperty("ApnsFilePath");
			String password = AcpConfig.env.getProperty("ApnsPassword");
			System.out.println(path+"\n"+password);
			List<PushedNotification> NOTIFICATIONS = Push
					.payload(payload, path, password, true,
							deviceToken);
			for (PushedNotification alert : NOTIFICATIONS) {
				if (alert.isSuccessful()) {
					/* APPLE ACCEPTED THE alert AND SHOULD DELIVER IT */
					System.out.println("PUSH alert SENT SUCCESSFULLY TO: "
							+ alert.getDevice().getToken());
					/* STILL require TO QUERY THE FEEDBACK SERVICE REGULARLY */
				} else {
					String INVALIDTOKEN = alert.getDevice().getToken();
					/* ADD code snippet HERE TO delete INVALIDTOKEN FROM YOUR DB */
					/* FIND OUT MORE ABOUT WHAT THE issue WAS */
					Exception THEPROBLEM = alert.getException();
					THEPROBLEM.printStackTrace();
					/*
					 * IF THE issue WAS AN ERROR-RESPONSE PACKET RETURNED BY
					 * APPLE GET IT
					 */
					ResponsePacket THEERRORRESPONSE = alert.getResponse();
					if (THEERRORRESPONSE != null) {
						System.out.println(THEERRORRESPONSE.getMessage());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void pushNotification(String deviceToken, String imei,
			String message) {
		
		if (imei == null) {
			pushDefaultNotification(deviceToken, message);
			return;
		}
		int flag = Integer.valueOf(imei);
		if (flag == 10) {// 有声音
			pushDefaultNotification(deviceToken, message);
			return;
		} else if (flag == 11) {// 22:00~8:00静音
			int h = new Date().getHours();
			if (h > 8 && h < 22) {
				pushDefaultNotification(deviceToken, message);
			} else {
				pushNoficationWithSound(deviceToken, null, message);
			}
			return;
		} else if (flag == 1 || flag == 0) {// 没有声音
			pushNoficationWithSound(deviceToken, null, message);
		}
	}

	public static void pushNotification(PushEvents pe) {
		String message = pe.message();
		String deviceToken = pe.getDeviceToken();
		String flag = pe.getFlag();
		System.out.println("start..."+deviceToken+","+flag+","+message);
		pushNotification(deviceToken, flag, message);
	}
}
