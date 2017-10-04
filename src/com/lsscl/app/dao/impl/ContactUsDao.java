package com.lsscl.app.dao.impl;

import java.util.Date;

import com.lsscl.app.bean.MailSenderInfo;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.util.AcpConfig;
import com.lsscl.app.util.SimpleMailSender;

public class ContactUsDao extends AppDao {
	/**
	 * 反馈信息
	 */
	private static final String contactInfo_insert = "insert into contactInfo (simno,contactInfo,createtime) values"
			+ "                                                                 (?,?,?)";
	/**
	 * 我的反馈
	 * 
	 * @param qc
	 * @return
	 */
	public RSP getRSP(QC qc) {
		String phone = qc.getMsgBody().get("PHONENO");
		String contactText = qc.getMsgBody().get("CONTACTTEXT");
		RSP rsp = new RSP(qc.getMsgId());
		// 服务器
		/**
		 * 将反馈信息存入数据库
		 */
		int i = doInsert(contactInfo_insert, new Object[] { phone, contactText,
				new Date() });
		if (i > 0) {
			sendMail(phone, contactText);
			rsp.setError("");
			rsp.setResult(0);
		} else {
			rsp.setError("反馈失败");
			rsp.setResult(1);
		}
		return rsp;
	}

	public void sendMail(String phone, String contactText) {
		// 这个类主要是设置邮件
		/*
			MailServerHost=www.lsscl.com
			MailServerPort=25
			MailUserName=lsscl@lsscl.com
			MailUserPassword=123
			MailFromAddress=lsscl@lsscl.com
			MailToAddress=service@lsscl.com
		 */
		MailSenderInfo mailInfo = new MailSenderInfo();
		mailInfo.setMailServerHost(AcpConfig.env.getProperty("MailServerHost"));
		mailInfo.setMailServerPort(AcpConfig.env.getProperty("MailServerPort"));
		mailInfo.setValidate(true);
		mailInfo.setUserName(AcpConfig.env.getProperty("MailUserName"));
		mailInfo.setPassword(AcpConfig.env.getProperty("MailUserPassword"));// 您的邮箱密码
		mailInfo.setFromAddress(AcpConfig.env.getProperty("MailFromAddress"));
		mailInfo.setToAddress(AcpConfig.env.getProperty("MailToAddress"));
		mailInfo.setSubject("App反馈(" + phone + ")");
		mailInfo.setContent("反馈内容：<br>" + contactText);
		// 这个类主要来发送邮件
		SimpleMailSender sms = new SimpleMailSender();
		sms.sendHtmlMail(mailInfo);// 发送html格式
	}
}
