package test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.junit.Test;

import com.lsscl.app.bean.PushEvents;
import com.lsscl.app.util.ApnsUtil;
import com.lsscl.app.util.HttpUtil;

public class HttpTest {
	
	@Test
	public void testArray(){
		System.out.println(new ArrayList<String>().remove(0));
	}

	@Test
	public void testJson(){
		String path = "http://www.lsscl.com:8080/servlet/AppServlet";
		path = "http://localhost:8080/servlet/AppServlet";
		String msgId = "MobileLogin";
//		msgId = "CompressorList";
//		msgId = "CompressorDetails";
//		msgId = "MobileIndex";
//		msgId = "AlarmHistory";
//		msgId = "CompressorAttrNames";
//		msgId = "ContactUs";
//		msgId = "ScopeList";
//		msgId = "ScopeAlarmList";
//		msgId = "ScopeIndex";
//		msgId = "MobileLogout";
		msgId = "AcpList";
//		msgId = "AcpPoints";
//		msgId = "ScopeStatistics";
		String phoneNo = "15067118176";
//		phoneNo = "18753098391";
		String pwd = "123";
		String aid = "18";
		String imsi  = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";
		imsi = "123a";
		String imei = "10";//有声音
		int scopeId = 1;
		int page = 1;
		int pageSize = 10;
		String json = "{QC:{" + "MSGID:'"+msgId+"',"
				+ "SIMNO:'通信卡号'," + "IMEI:'"+imei+"'," + "IMSI:'"+imsi+"',"
				+ "MSGBODY:{" + "PHONENO:'"+phoneNo+"'," + "PASSWORD:'"+pwd+"',"
				+ "CONTACTTEXT:'邮件测试'," + "COMPRESSORID:"+aid+",SCOPEID:"+scopeId
				+",LEVEL:0,STARTINDEX:1,PAGESIZE:"+pageSize+",PAGE:"+page
				+",AID:"+aid+",MSGTYPE:1,VERSION:2}}}";
		System.out.println(json);
//		String json2 = "{QC:{MSGID:'CompressorDetails',SIMNO:'15555215554',IMSI:'607069001780124845',IMEI:'3858916015566064571',MSGBODY:{DEVICETYPE:'1',VERSION:'1',PASSWORD:'123',PHONENO:'15067118176',COMPRESSORID:'136',SCOPEID:161}}}";
		try {
			byte []data = HttpUtil.postXml(path, json, "utf-8");
			String s = new String(data,"GBK");
			System.out.println(data.length);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testApns(){
		PushEvents pe = new PushEvents();
		pe.setDeviceToken("6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de");
		pe.setFlag("10");
		pe.setDeviceType(2);
		ApnsUtil.pushNotification(pe);
	}
}
