package com.lsscl.app.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.lsscl.app.bean.LoginMsgBody;
import com.lsscl.app.bean.LoginUser;
import com.lsscl.app.bean.MsgBody;
import com.lsscl.app.bean.PushEvents;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.dao.AppDaoBuilder;
import com.lsscl.app.dao.AppDaoImpl;
import com.lsscl.app.dao.LoginUserDao;
import com.lsscl.app.util.AcpConfig;
import com.lsscl.app.util.ApnsUtil;
import com.lsscl.app.util.AndroidNS;
import com.lsscl.app.util.StringUtil;

public class AppService {

	/**
	 * session过期
	 */
	public static final String SESSION_TIMEOUT = AcpConfig.cfg
			.getProperty("session.timeout");

	/**
	 * 帐号被其他用户登录
	 */
	public static final String USER_ISLOGINED = AcpConfig.cfg
			.getProperty("user.isLogined");
	/**
	 * 用户未登录
	 */
	public static final String UNLOGIN = AcpConfig.cfg.getProperty("unLogin");
	private AppDao appDao = new AppDaoImpl();
	private LoginUserDao loginUserDao = new LoginUserDao();

	public RSP getRSP(QC qc, HttpServletRequest req) {
		if (qc == null)
			return null;
		String msgId = qc.getMsgId();
		appDao = AppDaoBuilder.create(msgId);
		RSP rsp = new RSP(msgId);
		if(appDao!=null){
			if(!(AppDaoBuilder.ChangePwdApi.equals(msgId)
			   ||AppDaoBuilder.AckEvents.equals(msgId))){//先检查是否登录，在进行数据库更新操作
				checkLogin(qc, req, rsp);
				if(rsp.getResult()!=0){
					return rsp;
				}
			}
			rsp = appDao.getRSP(qc);
			checkLogin(qc, req, rsp);
		}else{
			rsp.setError("错误的请求");
			rsp.setResult(1);
		}
		// 验证登录
		return rsp;
	}

	private void checkLogin(QC qc, HttpServletRequest req, RSP rsp) {

		String msgId = qc.getMsgId();
		String phone = qc.getMsgBody().get("PHONENO");
		if (AppDaoBuilder.LoginApi.equals(msgId)) {// 登录
			// 保存区域id
			if (rsp.getResult() != 0)
				return;
			MsgBody body = rsp.getMsgBody();
			if (body != null && body instanceof LoginMsgBody) {
				LoginMsgBody loginMsgBody = (LoginMsgBody) body;
				LoginUser user = loginMsgBody.getUser();
				user.setOnline(true);
				if(!user.isAndroidDevice()){
					user.setNotificationType(qc.getImei());
				}
				loginUserDao.saveOrUpdate(user);
				qc.getMsgBody().put("SCOPEID", loginMsgBody.getScopeId() + "");
				qc.getMsgBody().put("SCOPENAME", loginMsgBody.getScopename());
				qc.getMsgBody().put("USERNAME", loginMsgBody.getUsername());
				qc.getMsgBody().put("LOGIN_TIME", StringUtil.getCurrentDate());
			}
		} else if (AppDaoBuilder.LogoutApi.equals(msgId)) {// 登出
			loginUserDao.logout(phone);
		} else {
			String imsi = qc.getImsi();
			String phoneno = qc.getMsgBody().get("PHONENO");
			if(loginUserDao.isOffine(phoneno)){
				rsp.setError("用户未登录");
				rsp.setResult(1);
				return;
			}
			if(loginUserDao.isLogined(phoneno,imsi)){
				rsp.setResult(1);
				rsp.setError("帐号被其他用户登录");
				return;
			}
		}
		loginUserDao.updateRspTime(phone);
	}

	/**
	 * 查看报警，若新报警，远程推送消息
	 * 
	 * @param context
	 */
	public void checkAlarms(ServletContext context) {
		System.out.println("checkAlarms........................");
		long now = new Date().getTime();
		long duration = 1 * 45 * 1000;
		long after = now - duration;
//		after = 1423539060000L;

		List<PushEvents> pushEventses = getScopeAlarmPhones(after);
		System.out.println("pushEvents:" + pushEventses.size());
		for (PushEvents pe : pushEventses) {
			if (pe.getDeviceType() == 1) {// Android
				System.out.println("Android push start...");
				AndroidNS.notification(pe.getUserId(), "报警", pe.message());
			} else {
				if (pe.getDeviceToken().length() == 71) {
					System.out.println("ios push start..."
							+ pe.getDeviceToken() + ","
							+ pe.getDeviceToken().length());
					ApnsUtil.pushNotification(pe);
				}
			}
		}
	}

	/**
	 * 获取区域下的报警的手机号码
	 * 
	 * @param context
	 * @param after
	 * @return
	 */
	private List<PushEvents> getScopeAlarmPhones(ServletContext context,
			long after) {
		List<PushEvents> phones = new ArrayList<PushEvents>();
		// 查看某时间点后的区域报警的区域id（工厂为三级报警、其他为一二三级报警）
		Map<String, Set<String>> scopeEvents = appDao.getScopeEvents(after);
		// 更新报警统计
		appDao.updateEventStatistics();
		// 遍历查询application中的手机号码手否在区域内
		Map<String, QC> qcs = (Map<String, QC>) context.getAttribute("AppQCS");
		System.out.println("qcs:"+qcs);
		if (qcs == null)
			qcs = new HashMap<String, QC>();
		for (String key : qcs.keySet()) {
			Object obj = qcs.get(key);
			if (obj != null && obj instanceof QC) {
				QC qc = (QC) obj;
				String scopeId = qc.getMsgBody().get("SCOPEID");
				if (scopeEvents.containsKey(scopeId))
					phones.add(new PushEvents(scopeEvents.get(scopeId), qc));
			}
		}
		return phones;
	}


	/**
	 * 获取区域下的报警的手机号码
	 * 
	 * @param after
	 * @return
	 */
	private List<PushEvents> getScopeAlarmPhones(long after) {
		List<PushEvents> phones = new ArrayList<PushEvents>();
		// 查看某时间点后的区域报警的区域id（工厂为三级报警、其他为一二三级报警）
		Map<String, Set<String>> scopeEvents = appDao.getScopeEvents(after);
		// 更新报警统计
		appDao.updateEventStatistics();
		// 遍历查询application中的手机号码手否在区域内
		List<LoginUser> loginUsers = loginUserDao.getOnlineUsers();
		for(LoginUser user:loginUsers){
			String scopeId = user.getScopeId()+"";
			if(scopeEvents.containsKey(scopeId)){
				phones.add(new PushEvents(scopeEvents.get(scopeId),user));
			}
		}
//		for (String key : qcs.keySet()) {
//			Object obj = qcs.get(key);
//			if (obj != null && obj instanceof QC) {
//				QC qc = (QC) obj;
//				String scopeId = qc.getMsgBody().get("SCOPEID");
//				if (scopeEvents.containsKey(scopeId))
//					phones.add(new PushEvents(scopeEvents.get(scopeId), qc));
//			}
//		}
		return phones;
	}
	
	private Set<String> getFactoryAlarmPhones(ServletContext context, long after) {
		// 查看某个时间点后的工厂属性点报警
		List<Map<String, Object>> results = appDao.getMobileEvents(after);
		// 2：遍历查询点的新值
		// 存储号码
		Set<String> tokens = new HashSet<String>();
		for (Map<String, Object> m : results) {
			Long time = (Long) m.get("cTime");
			Integer pid = (Integer) m.get("id");
			// 报警时间在10分钟内，产生推送消息
			// 1:获取改点对应的手机号
			List<String> phones = appDao.getPhonesByPid(pid);
			// 获取报警点对应手机的令牌
			for (String phoneNO : phones) {
				String key = StringUtil.formatPhoneNO(phoneNO);
				QC qc = (QC) context.getAttribute(key);
				if (qc == null)
					continue;
				String imsi = qc.getImsi();
				if (imsi != null && imsi.length() == 71) {
					tokens.add(key);
				}
			}
			// 更新报警
			appDao.updateMobileEvent(pid, time);
		}
		return tokens;
	}

	/**
	 * 初始化数据库
	 */
	public void initDataBase() {
//		loginUserDao.logoutAll();
	}

	/**
	 * 更新统计表
	 */
	public void updateStatistics() {
		appDao.updateStatistics();
	}
}
