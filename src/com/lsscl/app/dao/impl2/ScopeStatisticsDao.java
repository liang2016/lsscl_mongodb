package com.lsscl.app.dao.impl2;

import java.util.HashMap;
import java.util.Map;

import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.bean2.ScopeStatisticsMsgBody;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.util.StringUtil;
import com.serotonin.mango.vo.ResultData;

public class ScopeStatisticsDao extends AppDao{
	private static final String BASE_SQL = " select a.id " +
       		"from appAcps a left join ScopeTree s on s.id = a.scopeId where s.scopetype=3";
	public static final String ACP_IDS_BY_SCOPEID_SQL =
	           SCOPE_TREE_BY_PARENTID_SQL+BASE_SQL;
	public static final String ACP_IDS_BY_PHONE_SQL = SCOPE_TREE_BY_PHONENO + BASE_SQL;
	
	public static final String sql1 = "select power,openCount,closeCount from scopeStatistics where scopeId = ?";
	public static final String sql2 = "select sum(power) power,sum(openCount) openCount,sum(closeCount)closeCount " +
											"from scopeStatistics ss left join scope s on ss.scopeId = s.id " +
											"where ss.scopeId in (select us.scopeid from user_scope us left join users u on us.uid = u.id where us.isHomeScope = 0 and u.phone = ?)";
	@Override
	public RSP getRSP(QC qc) {
		RSP rsp = new RSP(qc.getMsgId());
		String scopeId = qc.getMsgBody().get("SCOPEID");
		String phone = qc.getMsgBody().get("PHONENO");
			Map<String,Object> result;
			if(!(isRootScope(phone, scopeId) && isNotAdmin(phone))){
				result = queryForObject(sql1,new Object[]{scopeId},new ResultData(),new HashMap<String,Object>());
			}else{
				result = queryForObject(sql2,new Object[]{phone},new ResultData(),new HashMap<String,Object>());
			}
			ScopeStatisticsMsgBody msg = new ScopeStatisticsMsgBody();
			Integer open = 0;
			Integer close = 0;
			Double power = 0.0;
			if(result!=null){
				open = (Integer) result.get("openCount");
				close = (Integer)result.get("closeCount");
				power = (Double)result.get("power");
				open = open!=null?open:0;
				close = close!=null?close:0;
				power = power!=null?power:0.0;
			}
			
			msg.setOpen(open);
			msg.setClose(close);
			msg.setPower(StringUtil.formatNumber(power/ 1000, "0.0"));
			rsp.setMsgBody(msg);
		return rsp;
	}

}
