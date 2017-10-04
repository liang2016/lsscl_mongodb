package test2000;

import java.io.UnsupportedEncodingException;

import com.lsscl.app.util.HttpUtil;

/**
 * 2000客户端测试
 */
public class App2000Test {
	private static String path = "http://192.168.1.117:8080/servlet/AppServlet";

	public static void main(String[] args) throws Exception {
		testLogin();
//		testQC();
	}

	public static void testLogin() throws Exception {
		final String msgId = "MobileLogin";
		for (int i = 0; i < 2000; i++) {
			final long phone = 15067110000L + i;
			new Thread() {
				public void run() {
					testJson(msgId, phone + "", "123", phone + "");
				}
			}.start();
		}
	}

	public static void testQC() throws Exception{
		final String msgId = "ScopeIndex";
//		while (true) {
			for (int i = 0; i < 1; i++) {
				final long phone = 15067110000L + i;
				new Thread() {
					public void run() {
						testJson(msgId, phone + "", "123", phone + "");
					}
				}.start();
			}
//		}
	}

	public static void testJson(String msgId, String phoneNo, String pwd,
			String imsi) {
		// msgId = "CompressorList";
		// msgId = "CompressorDetails";
		// msgId = "MobileIndex";
		// msgId = "AlarmHistory";
		// msgId = "CompressorAttrNames";
		// msgId = "ContactUs";
		// msgId = "ScopeList";
		// msgId = "ScopeAlarmList";
		// msgId = "ScopeIndex";
		String aid = "16";
		String imei = "10";// 有声音
		String json = "{QC:{" + "MSGID:'" + msgId + "'," + "SIMNO:'通信卡号',"
				+ "IMEI:'" + imei + "'," + "IMSI:'" + imsi + "'," + "MSGBODY:{"
				+ "PHONENO:'" + phoneNo + "'," + "PASSWORD:'" + pwd + "',"
				+ "CONTACTTEXT:'邮件测试'," + "COMPRESSORID:" + aid
				+ ",SCOPEID:181,LEVEL:0,STARTINDEX:1,PAGESIZE:500}}}";
		try {
			System.out.println(json);
			byte[] data = HttpUtil.postXml(path, json, "utf-8");
			String s = new String(data, "GBK");
			System.out.println(s);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
