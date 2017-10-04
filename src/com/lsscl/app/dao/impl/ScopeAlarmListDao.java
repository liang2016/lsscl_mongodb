package com.lsscl.app.dao.impl;

import java.util.List;

import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.bean.ScopeEvent;
import com.lsscl.app.bean.ScopeEventsMsgBody;
import com.lsscl.app.dao.AppDao;

public class ScopeAlarmListDao extends AppDao {
	//公共条件
	private static final String CONDITION_SQL = "and (a.acpId is not null and acp.Id is not null) ";
	
	//字段名
	private static final String FIELDS_SQL = "e.id,s.id as scopeId,s.scopename,e.message,e.activeTs,e.typeRef1,e.typeRef2,e.rtnTs,e.rtnCause,e.alarmLevel," +
											"a.name as pointName,a.pointId,acp.id as acpId,acp.name as acpName,e.ackUserId,e.ackTs";
	private static final String ORDERBY_SQL = "order by e.activeTs desc";
	
	private static final String BASE_SQL = "select * from ( "
			+ "select row_number()over("+ORDERBY_SQL+")rn , "
			+ FIELDS_SQL+" from events e "
			+ "left join ScopeTree s on e.scopeId = s.id "
			+ "left join appPoints a on e.typeRef1 = a.pointId "
			+ "left join appAcps acp on a.acpId = acp.id "
			+ "where e.ackUserId is null "+CONDITION_SQL
			+ "and s.scopetype = 3 and e.alarmLevel = ? and e.activeTs > ? )tt "
			+ "where rn>=? and rn<? ";
	
	
	private static final String BASE_SQL_ALL = "select * from ( "
			+ "select row_number()over("+ORDERBY_SQL+")rn , "
			+ FIELDS_SQL+" from events e "
			+ "left join ScopeTree s on e.scopeId = s.id "
			+ "left join appPoints a on e.typeRef1 = a.pointId "
			+ "left join appAcps acp on a.acpId = acp.id "
			+ "where e.rtnApplicable='Y' "+CONDITION_SQL
			+ "and s.scopetype = 3 and e.typeId!=3 and e.typeId!=4 and e.activeTs > ?)tt "
			+ "where rn>=? and rn<? ";

	
	public static void main(String[] args) {
		System.out.println(SCOPE_TREE_BY_PARENTID_SQL+ScopeAlarmListDao.BASE_SQL_ALL);
		System.out.println(SCOPE_TREE_BY_PARENTID_SQL+ScopeAlarmListDao.BASE_SQL);
	}
	public RSP getRSP(QC qc) {
		RSP rsp = new RSP(qc.getMsgId());
		String level = qc.getMsgBody().get("LEVEL");
		String scopeId = qc.getMsgBody().get("SCOPEID");
		String startIndex = qc.getMsgBody().get("STARTINDEX");
		String pageSize = qc.getMsgBody().get("PAGESIZE");
		String time = qc.getMsgBody().get("STIME");
		String phoneno = qc.getMsgBody().get("PHONENO");
		time = time != null ? time : "0";
		int start = 1;
		int end = 10;
		if (startIndex != null && pageSize != null) {
			start = Integer.parseInt(startIndex);
			end = start + Integer.parseInt(pageSize);
		}

		List<ScopeEvent> events = null;
		boolean b = isRootScope(phoneno, scopeId) && isNotAdmin(phoneno);
		String sql = b?SCOPE_TREE_BY_PHONENO:SCOPE_TREE_BY_PARENTID_SQL; 
//		if(!"0".equals(level)){
			int l = level!=null?Integer.valueOf(level)+3:3;
			sql += BASE_SQL;
			System.out.println(sql);
			events = ejt.query(sql, new Object[] { b?phoneno:scopeId,
					l, time, start, end }, new ScopeEventMapper());
//		}
		/*else{
			sql += BASE_SQL_ALL;
			events = ejt.query(sql, new Object[] { b?phoneno:scopeId,
					 time, start, end }, new ScopeEventMapper());
		}*/
		ScopeEventsMsgBody msgBody = new ScopeEventsMsgBody();
		msgBody.setEvents(events);
		msgBody.setLevel(level);
		msgBody.setStartIndex(start + "");
		msgBody.setPageSize(pageSize);
		rsp.setMsgBody(msgBody);
		return rsp;
	}

}
