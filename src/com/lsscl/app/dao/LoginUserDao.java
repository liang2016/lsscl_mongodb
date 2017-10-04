package com.lsscl.app.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.lsscl.app.bean.LoginUser;
import com.serotonin.db.spring.GenericRowMapper;
import com.serotonin.mango.db.dao.BaseDao;

public class LoginUserDao extends BaseDao {

	public void saveOrUpdate(LoginUser user){
		if(user==null||user.getUserId()==0)return;
		int count = ejt.queryForInt("select count(userId) from appUsers where phoneno = ? ",new Object[]{user.getPhoneno()},0);
		String insertSql = "insert into appUsers(notificationType,token2,userName,phoneno,email,scopeId,scopeName,login,online,token,deviceType,deviceVersion,userId)" +
				                       " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String updateSql = "update appUsers set notificationType =?,token2 =?,lastLogin = login,userName = ?," +
				                               "phoneno = ?," +
				                               "email = ?," +
				                               "scopeId = ?," +
				                               "scopeName = ?," +
				                               "login = ?," +
				                               "online = ?," +
				                               "token = ?,"+
				                               "deviceType = ?,"+
				                               "deviceVersion = ?"+
				                               " where userId = ?";
		Object[] params = new Object[]{
				user.getNotificationType(),
				user.getToken2(),
				user.getUserName(),
				user.getPhoneno(),
				user.getEmail(),
				user.getScopeId(),
				user.getScopeName(),
				user.getLoginTime(),
				user.isOnline()?1:0,
				user.getToken(),
				user.getDeviceType(),
				user.getDeviceVersion(),
				user.getUserId()
		};
		if(count==0){
			//save
			ejt.update(insertSql, params);
		}else{
			ejt.update(updateSql,params);
		}
	}
	
	public LoginUser getById(int userId){
		String sql = "select * from appUsers where userId = ?";
		LoginUser user = queryForObject(sql,new Object[]{userId},new LoginUserRowMapper(),new LoginUser());
		return user;
	}
	
	public List<LoginUser> getAll(){
		String sql = "select * from appUsers order by online desc";
		List<LoginUser> users = ejt.query(sql,new Object[]{},new LoginUserRowMapper());
		return users;
	}
	/**
	 * 获取在线的用户
	 * @return
	 */
	public List<LoginUser> getOnlineUsers() {
		String sql = "select * from appUsers where online = 1";
		return ejt.query(sql,new Object[]{},new LoginUserRowMapper());
	}
	
	private class LoginUserRowMapper implements GenericRowMapper<LoginUser>{

		@Override
		public LoginUser mapRow(ResultSet rs, int i) throws SQLException {
			LoginUser user = new LoginUser();
			user.setUserId(rs.getInt("userId"));
			user.setUserName(rs.getString("userName"));
			user.setPhoneno(rs.getString("phoneno"));
			user.setEmail(rs.getString("email"));
			user.setLastLogin(rs.getLong("lastLogin"));
			user.setLogin(rs.getLong("login"));
			user.setOnline(rs.getInt("online")==1);
			user.setScopeId(rs.getInt("scopeId"));
			user.setScopeName(rs.getString("scopeName"));
			user.setToken(rs.getString("token"));
			user.setDeviceType(rs.getInt("deviceType"));
			user.setDeviceVersion(rs.getString("deviceVersion"));
			user.setToken2(rs.getString("token2"));
			user.setNotificationType(rs.getString("notificationType"));
			user.setLastRspTime(rs.getLong("lastRspTime"));
			return user;
		}
		
	}

	public void logout(String phone) {
		String sql = "update appUsers set lastLogin = login,online = 0 where phoneno = ?";
		ejt.update(sql,new Object[]{phone});
	}

	public void logoutAll() {
		String sql = "update appUsers set lastLogin = login,online = 0";
		ejt.update(sql,new Object[]{});
	}

	public boolean isOffine(String phoneno) {
		String sql = "select online from appUsers where phoneno = ?";
		int online = ejt.queryForInt(sql, new Object[]{phoneno},0);
		return online != 1;
	}

	public boolean isLogined(String phoneno, String newToken) {
		String sql = "select token from appUsers where phoneno = ?";
		String token = ejt.queryForObject(sql,new Object[]{phoneno},String.class,"");
		return !token.equals(newToken);
	}

	public void updateRspTime(String phone) {
		String sql = "update appUsers set lastRspTime = ? where phoneno = ?";
		ejt.update(sql, new Object[]{new Date().getTime(),phone});
	}
	
	


}
