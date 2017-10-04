package com.lsscl.app.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.lsscl.app.bean.CompressorDetailsMsgBody;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.util.AcpConfig;
import com.lsscl.app.util.StringUtil;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;

public class CompressorDetailsDao extends AppDao {

	/**
	 * 空压机详细(根据手机号码和空压机id查询)
	 * 
	 * @param qc
	 * @return
	 */
	public RSP getRSP(QC qc) {
		String compressorId = qc.getMsgBody().get("COMPRESSORID");
		String versionStr = qc.getMsgBody().get("VERSION");
		RSP rsp = null;
		rsp = new RSP(qc.getMsgId());

		Map<String, String> acpInfo = getAcpInfoById(compressorId);
		List<Map<String, Object>> list = ejt.query(acp_attr2,
				new Object[] { compressorId },
				new com.serotonin.mango.vo.ResultData());
		LinkedHashMap<String, Map<String,String>> dataPoints = new LinkedHashMap<String, Map<String,String>>();
		CompressorDetailsMsgBody msg = new CompressorDetailsMsgBody();
		msg.setCompressorId(compressorId);
		msg.setCompressorName(getAcpName(acpInfo));
		msg.setTime(StringUtil.getCurrentDate());
		if(versionStr!=null)msg.setVersion(Integer.parseInt(versionStr));
		long now = new Date().getTime();
		/**
		 * 读取点值
		 */
		for (Map<String, Object> m : list) {
			String name = (String) m.get("name");
			if (isBasicPointName(name))
				continue;
			Integer dpid = (Integer) m.get("pointId");
			if (dpid != null) {
				DataPointVO pv = new DataPointDao().getDataPoint(dpid);
				String sql = "select top 1 * from pointValues_" + dpid
						+ " order by ts desc";
				Map<String, Object> point = queryForObject(sql, null,
						new com.serotonin.mango.vo.ResultData(),
						new HashMap<String, Object>());
				Integer type = (Integer) point.get("dataType");
				if (type != null) {// 不为空且不为二进制数据
					String tValue;
					Object obj = point.get("pointValue");
					if (type != DataTypes.BINARY) {
						if (obj instanceof Double) {
							Double d = (Double) obj;
							tValue = pv.getTextRenderer().getText(d, 2);
						} else {
							tValue = pv.getTextRenderer().getText(
									point.get("pointValue") + "", 2);
						}
					} else {
						Double d = (Double) obj;
						Boolean b = (d > 0);
						tValue = pv.getTextRenderer().getText(b, 2);
					}
					String aid = "T" + dpid;
					Map<String,String>pMap = new HashMap<String,String>();
					pMap.put("value", tValue);
					String state = "0";
					Long ts = (Long) point.get("ts");
					if(ts!=null){
						if((now-ts)<60*1000)state="1";
					}
					pMap.put("state", state);
					dataPoints.put(aid, pMap);
				}else{
					String aid = "T" + dpid;
					Map<String,String>pMap = new HashMap<String,String>();
					pMap.put("value", "");
					String state = "0";
					Long ts = (Long) point.get("ts");
					if(ts!=null){
						if((now-ts)<60*1000)state="1";
					}
					pMap.put("state", state);
					dataPoints.put(aid, pMap);
				}
			}
		}
		// 基本属性点：电流、压力、温度
				String data1 = getPointValueByAcpIdAndName(compressorId,
						AcpConfig.EXHAUSTEMPERATURE);
				msg.setExhausTemperature(data1 == null ? " " : data1);
				// 排气压力
				data1 = getPointValueByAcpIdAndName(compressorId,
						AcpConfig.EXHAUSPRESSURE);
				msg.setExhausPressure(data1 == null ? " " : data1);

				// 电流
				String current = getPointValueByAcpIdAndName(compressorId,
						AcpConfig.CURRENT);
				data1 = current;
				msg.setCurrent(data1 == null ? "   " : data1);

				msg.setDataPoints(dataPoints);
				msg.setCurrentId(getPidByAcpIdAndName(compressorId, AcpConfig.CURRENT));
				msg.setExhausPressureId(getPidByAcpIdAndName(compressorId, AcpConfig.EXHAUSPRESSURE));
				msg.setExhausTemperatureId(getPidByAcpIdAndName(compressorId, AcpConfig.EXHAUSTEMPERATURE));
				rsp.setMsgBody(msg);
				return rsp;
			}

}
