package com.lsscl.app.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lsscl.app.bean.LoginMsgBody;
import com.lsscl.app.bean.LoginUser;
import com.lsscl.app.bean.MsgBody;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.dao.LoginUserDao;
import com.serotonin.mango.Common;

public class MobileLoginDao extends AppDao {
	private static final String MobileLogin = "select count(*) from users where (phone = ? or username = ?) and password = ?";
	/**
	 * 登录验证
	 * 
	 * @param qc
	 * @return
	 */
	public RSP getRSP(QC qc) {
		String phone = qc.getMsgBody().get("PHONENO");
		String password = qc.getMsgBody().get("PASSWORD");
		String enPwd = Common.encrypt(password);
		RSP rsp = new RSP(qc.getMsgId());
		int count = queryForObject(MobileLogin, new Object[] { phone,phone, enPwd },
				Integer.class, 0);
		if (count == 1) {//
			String sql = "select s.scopeType,u.username,u.phone,s.id,s.scopename,us.uid,u.email,u.phone from scope s "
					+ "left join user_scope us on s.id = us.scopeId "
					+ "left join users u on us.uid = u.id "
					+ "where (u.phone = ? or u.username = ?) and u.password = ?";
			List<Map<String, String>> maps = ejt.query(sql, new Object[] {phone,
					phone, enPwd }, new MapResultData());
			if (maps.size() > 0) {
				Map<String,String> m = maps.get(0);
				String scopeType = maps.get(0).get("scopeType");
				String scopeId = maps.get(0).get("id");
				String username = maps.get(0).get("username");
				String scopename = maps.get(0).get("scopename");
				String phoneno = maps.get(0).get("phone");
				String uid = m.get("uid");
				String email = m.get("email");
				rsp.setResult(0);
				LoginMsgBody msgBody = new LoginMsgBody();
				//设置app登录信息
				LoginUser user = new LoginUser();
				long login = new Date().getTime();
				user.setLogin(login);
				user.setEmail(email);
				user.setUserId(Integer.valueOf(uid));
				user.setOnline(true);
				user.setScopeId(Integer.valueOf(scopeId));
				user.setUserName(username);
				user.setPhoneno(phoneno);
				qc.getMsgBody().put("PHONENO", phoneno);
				user.setScopeName(scopename);
				String deviceType = qc.getMsgBody().get("DEVICETYPE");
				if(deviceType!=null){
					user.setDeviceType(Integer.valueOf(deviceType));
				}
				user.setToken(qc.getImsi());
				String deviceVersion = qc.getMsgBody().get("DEVICEVERSION");
				user.setDeviceVersion(deviceVersion);
				msgBody.setUser(user);
				msgBody.setScopename(scopename);
				
				if ("3".equals(scopeType)) {
					msgBody.setUserflag(1);// 工厂用户
					msgBody.setDefaultScopeId(0);
				} else {// 非工厂用户
					msgBody.setUserflag(2);// 渠道用户
					msgBody.setDefaultScopeId(getDefaultScopeId(scopeId));
				}
				if (scopeId != null)
					msgBody.setScopeId(Integer.parseInt(scopeId));
				msgBody.setUsername(username);
				rsp.setMsgBody(msgBody);
			} else {
				rsp.setResult(1);
				rsp.setError("帐号异常，请联系管理员");
				rsp.setMsgBody(new MsgBody());
			}

		} else if (count == 0) {
			rsp = new RSP(qc.getMsgId());
			rsp.setResult(1);
			rsp.setError("用户名或密码不正确");
			rsp.setMsgBody(new MsgBody());
		} else if (count > 1) {
			rsp = new RSP(qc.getMsgId());
			rsp.setResult(1);
			rsp.setError("帐号异常，请联系管理员");
			rsp.setMsgBody(new MsgBody());
		}
		return rsp;
	}
}
