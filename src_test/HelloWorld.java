import java.util.Arrays;

import com.lsscl.app.util.StringUtil;

public class HelloWorld {
	public static void main(String[] args) {
		// new Timer().schedule(new TimerTask(){
		// @Override
		// public void run() {
		// System.out.println("run..");
		// throw new RuntimeException();
		// }
		// }, 1000, 2000);
		String s = "{\"RSP\":{\"MSGID\":\"AcpPoints\",\"RESULT\":0,\"RSPTIME\":\"2015-08-03 15:21:52\",\"ERROR\":\"\",\"MSGBODY\":{\"CID\":40639,\"CVALUE\":\"32896.0\",\"TID\":40640,\"TVALUE\":\"6682.0\",\"PID\":40635,\"PVALUE\":\"591.1\",\"POINTS\":[{\"PID\":40629,\"PNAME\":\"ModulationÄ£Ê½ÔËÐÐ\",\"VALUE\":\"ModulationÄ£Ê½\",\"STATE\":1},{\"PID\":40633,\"PNAME\":\"On/OffÄ£Ê½\",\"VALUE\":\"²»ÔÚOn/Off lineÄ£Ê½\",\"STATE\":1},{\"PID\":40631,\"PNAME\":\"±¨¾¯\",\"VALUE\":\"±¨¾¯\",\"STATE\":1},{\"PID\":40648,\"PNAME\":\"±¨¾¯´úÂë\",\"VALUE\":\"38036\",\"STATE\":1},{\"PID\":40626,\"PNAME\":\"±¾µØ/Ô¶³Ì\",\"VALUE\":\"±¾µØ\",\"STATE\":1},{\"PID\":40634,\"PNAME\":\"´óÆøÁ¿¿ØÖÆ/×Ô¶¯¿ØÖÆ\",\"VALUE\":\"ÔÚ´óÆøÁ¿¿ØÖÆ/×Ô¶¯¿ØÖÆ\",\"STATE\":1},{\"PID\":40641,\"PNAME\":\"µÍÎÂ£\u00ADÀäÈ´ÓÍÎÂ¶È£¨Ñ¡Ïî£©\",\"VALUE\":\"8224¡æ\",\"STATE\":1},{\"PID\":40628,\"PNAME\":\"¼ÓÔØ/Ð¶ÔØ\",\"VALUE\":\"Ð¶ÔØ\",\"STATE\":1},{\"PID\":40644,\"PNAME\":\"¼ÓÔØÊ±¼ä\",\"VALUE\":\"36237hours\",\"STATE\":1},{\"PID\":40646,\"PNAME\":\"¼ÓÔØÊ±¼ä£¨ÍòÐ¡Ê±£©\",\"VALUE\":\"398350000hours\",\"STATE\":1},{\"PID\":40637,\"PNAME\":\"½øÆø¸ºÑ¹\",\"VALUE\":\"4857.30bar\",\"STATE\":1},{\"PID\":40632,\"PNAME\":\"¾¯¸æ\",\"VALUE\":\"Õý³£\",\"STATE\":1},{\"PID\":40647,\"PNAME\":\"¾¯¸æ´úÂë\",\"VALUE\":\"30326\",\"STATE\":1},{\"PID\":40636,\"PNAME\":\"ÓÍ·ÖÀëÍ²ÌåÑ¹Á¦\",\"VALUE\":\"976.60bar\",\"STATE\":1},{\"PID\":40642,\"PNAME\":\"ÓÍ·ÖÀëÑ¹²î\",\"VALUE\":\"-2955.60bar\",\"STATE\":1},{\"PID\":40627,\"PNAME\":\"ÔËÐÐ/Í£Ö¹\",\"VALUE\":\"ÔËÐÐ\",\"STATE\":1},{\"PID\":40625,\"PNAME\":\"ÔËÐÐ¿ØÖÆ×´Ì¬\",\"VALUE\":\"23130\",\"STATE\":1},{\"PID\":40643,\"PNAME\":\"ÔËÐÐÊ±¼ä\",\"VALUE\":\"0hours\",\"STATE\":1},{\"PID\":40645,\"PNAME\":\"ÔËÐÐÊ±¼ä£¨ÍòÐ¡Ê±£©\",\"VALUE\":\"0hours\",\"STATE\":1},{\"PID\":40638,\"PNAME\":\"Ö÷»úÅçÓÍÎÂ¶È\",\"VALUE\":\"15934¡æ\",\"STATE\":1},{\"PID\":40630,\"PNAME\":\"×Ô¶¯ÖØÆô·½Ê½ÏÂÍ£Ö¹\",\"VALUE\":\"²»ÄÜ×Ô¶¯ÖØÆô·½Ê½ÏÂÍ£Ö¹\",\"STATE\":1}]}}}";
		byte[]bytes1 = s.getBytes();
		byte[]bytes2 = StringUtil.gZip(bytes1);
		System.out.println(bytes1.length);
		System.out.println(bytes2.length);
		System.out.println(StringUtil.unGZip(bytes2).length);
		String [] arr = new String[20];
		arr[3] = "100";
		System.out.println(Arrays.toString(arr));
		
	}
}
