package com.lsscl.app.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.lsscl.app.bean.ScopeEvent;
import com.serotonin.db.spring.GenericRowMapper;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.web.i18n.LocalizableMessageParseException;

public class ScopeEventMapper implements GenericRowMapper<ScopeEvent> {
	private static final Locale defaultLocale = Locale.getDefault();
	static LocalizableMessage lm;
	static final ResourceBundle bundle = ResourceBundle.getBundle("mobile", defaultLocale);
	@Override
	public ScopeEvent mapRow(ResultSet rs, int i) throws SQLException {
		ScopeEvent s = new ScopeEvent();
		s.setfName(rs.getString("scopename"));
		String message = rs.getString("message");
		s.setScopeId(rs.getString("scopeId"));
		s.setId(rs.getInt("id"));
		s.setPointId(rs.getInt("typeRef1"));
		s.setPointName(rs.getString("pointName"));
		s.setAcpId(rs.getInt("acpId"));
		s.setAcpName(rs.getString("acpName"));
		s.setAckUserId(rs.getInt("ackUserId"));
		s.setAckTs(rs.getLong("ackTs"));
		s.setRtnTs(rs.getLong("rtnTs"));
		s.setRtnCause(rs.getInt("rtnCause"));
		s.setAlarmLevel(rs.getInt("alarmLevel"));
		s.setTypeRef2(rs.getInt("typeRef2"));
		try {
			 lm = LocalizableMessage.deserialize(message);
			s.setMessage(lm.getLocalizedMessage(bundle));
		} catch (LocalizableMessageParseException e) {
			e.printStackTrace();
		}
		s.setcTime(rs.getLong("activeTs"));
		return s;
	}

}