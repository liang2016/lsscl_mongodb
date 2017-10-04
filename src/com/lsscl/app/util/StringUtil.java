package com.lsscl.app.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringUtil {
	
	 /***
	  * 压缩GZip
	  * 
	  * @param data
	  * @return
	  */
	 public static byte[] gZip(byte[] data) {
	  byte[] b = null;
	  try {
	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	   GZIPOutputStream gzip = new GZIPOutputStream(bos);
	   gzip.write(data);
	   gzip.finish();
	   gzip.close();
	   b = bos.toByteArray();
	   bos.close();
	  } catch (Exception ex) {
	   ex.printStackTrace();
	  }
	  return b;
	 }
	 /***
	  * 解压GZip
	  * 
	  * @param data
	  * @return
	  */
	 public static byte[] unGZip(byte[] data) {
	  byte[] b = null;
	  try {
	   ByteArrayInputStream bis = new ByteArrayInputStream(data);
	   GZIPInputStream gzip = new GZIPInputStream(bis);
	   byte[] buf = new byte[1024];
	   int num = -1;
	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   while ((num = gzip.read(buf, 0, buf.length)) != -1) {
	    baos.write(buf, 0, num);
	   }
	   b = baos.toByteArray();
	   baos.flush();
	   baos.close();
	   gzip.close();
	   bis.close();
	  } catch (Exception ex) {
	   ex.printStackTrace();
	  }
	  return b;
	 }
	
	/**
	 * 格式化时间
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date,String pattern){
		String ret = null;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		ret = sdf.format(date);
		return ret;
	}
	
	/**
	 * 获取当前时间
	 * @return
	 */
	public static String getCurrentDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	/**
	 * 格式化数字
	 * @param d
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(String d,String pattern){
		String ret = null;
		if(d==null)return null;
		try{
		Double d1 = Double.parseDouble(d);
		DecimalFormat df = new DecimalFormat(pattern);
		ret = df.format(d1);
		}catch(NumberFormatException e){
		}
		return ret;
	}
	
	/**
	 * 格式化数字
	 * @param d
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(Double d,String pattern){
		String ret = null;
		if(d==null)return null;
		try{
		DecimalFormat df = new DecimalFormat(pattern);
		ret = df.format(d);
		}catch(NumberFormatException e){
		}
		return ret;
	}
	/**
	 * 格式化手机号码
	 * @param phoneno
	 * @return
	 */
	public static String formatPhoneNO(String phoneno){
		return "<PHONENO>"+phoneno+"</PHONENO>";
	}

	
	/**
	 * 获取随机字符串
	 * @return
	 */
	public static String getRandomString() {
		
		return new Date().getTime()+"_"+getRandomString(4);
	}
	public static String getRandomString(int length) { 
	    StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); 
	    StringBuffer sb = new StringBuffer(); 
	    Random r = new Random(); 
	    int range = buffer.length(); 
	    for (int i = 0; i < length; i ++) { 
	        sb.append(buffer.charAt(r.nextInt(range))); 
	    } 
	    return sb.toString(); 
	}
	
	public static void trimMap(Map<String,Object>m){
		for(String key:m.keySet()){
			Object obj = m.get(key);
			if("".equals(obj)){
				m.put(key, null);
			}
		}
	}
}
