import java.io.UnsupportedEncodingException;

import com.lsscl.app.util.HttpUtil;


public class ApiTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "http://www.lsscl.com:9080/servlet/AppServlet";
		String msgId = "MobileLogin";
//		msgId = "CompressorList";
//		msgId = "CompressorDetails";
//		msgId = "MobileIndex";
//		msgId = "AlarmHistory";
//		msgId = "CompressorAttrNames";
//		msgId = "ContactUs";
//		msgId = "ScopeList";
//		msgId = "ScopeAlarmList";
		msgId = "ScopeIndex";
		String phoneNo = "13100000001";
		phoneNo = "15067118176";
		String pwd = "123";
		String aid = "11";
		String imsi  = "948d3a7b ba699b9c de5b35ea cc34b238 0f653ba7 cb85bc0a 299fc123 0bcbe631";
		String imei = "10";//有声音
		String json = "{QC:{" + "MSGID:'"+msgId+"',"
				+ "SIMNO:'通信卡号'," + "IMEI:'"+imei+"'," + "IMSI:'"+imsi+"',"
				+ "MSGBODY:{" + "PHONENO:'"+phoneNo+"'," + "PASSWORD:'"+pwd+"',"
				+ "CONTACTTEXT:'邮件测试'," + "COMPRESSORID:"+aid+",SCOPEID:110,LEVEL:0,STARTINDEX:1,PAGESIZE:500}}}";
		String json2 = "{QC:{MSGID:'ScopeIndex',SIMNO:'15767118176',IMSI:'926587477475628719',IMEI:'4153470738761065185',MSGBODY:{PASSWORD:'123',PHONENO:'15067118176',DEVICETYPE:'1',SCOPEID:1}}}";
		try {
			byte []data = HttpUtil.postXml(path, json2, "utf-8");
			String s = new String(data,"GBK");
			System.out.println(s);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
