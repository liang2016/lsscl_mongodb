/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.rt.dataSource.modbus;

import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.DataPointVO.LoggingTypes;
import com.serotonin.mango.vo.dataSource.modbus.ModbusDataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusPointLocatorVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.messaging.MessagingExceptionHandler;
import com.serotonin.messaging.TimeoutException;
import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ExceptionResult;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.modbus4j.base.KeyedModbusLocator;
import com.serotonin.modbus4j.base.ReadFunctionGroup;
import com.serotonin.modbus4j.code.RegisterRange;
abstract public class ModbusDataSource extends PollingDataSource implements MessagingExceptionHandler {
    private final Log LOG = LogFactory.getLog(ModbusDataSource.class);

    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    private ModbusMaster modbusMaster;
    private BatchRead<ModbusPointLocatorRT> batchRead;
    private boolean lostData=false;
    private boolean changeType=false;
    private final ModbusDataSourceVO<?> vo;
    private final Map<Integer, DataPointRT> slaveMonitors = new HashMap<Integer, DataPointRT>();
//更新batchRead中的时间
    private void updateBatchRead(BatchRead<ModbusPointLocatorRT> batchRead,long timestamp) {
    	for (ReadFunctionGroup<ModbusPointLocatorRT> RF: batchRead.getReadFunctionGroups()) {
    		RF.getSlaveAndTime().setTime(timestamp);
		}
	}
    
    public ModbusDataSource(ModbusDataSourceVO<?> vo) {
        super(vo);
        this.vo = vo;
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), vo.isQuantize());
    }

    private long getLatestTime(){
    	PointValueDao pointValueDao = new PointValueDao();
    	 return pointValueDao.getEndDataTime(getPointIds());
    }
    private void checkChangeType(boolean lastType,boolean nowType){
    	if(lastType==nowType){
    		changeType=false;//未变化请求方式
    	}
    	else{
    		changeType=true;
    	}
    }
    private void setNextPollingPeriod(long [] timestamps,long pollTime){
    	long timestamp=timestamps[0];
    	long nowTimestamp=timestamps[1];
    	PointValueDao pointValueDao = new PointValueDao();
    	if(timestamp<=nowTimestamp-20){
    		if(nowTimestamp<=pollTime){
    			//查询nowTimestamp点是否有数据
    			if(pointValueDao.getEndDataTime(getPointIds(),nowTimestamp*1000)){
    				//不做处理,以便于之间调用polltime
    				checkChangeType(lostData,false);
    				lostData=false;
    				setNextTime(20);
    			}
    			else{
    				checkChangeType(lostData,true);
    				pollTime=timestamp;
    				lostData=true;
    			}
    		}
    		else{
    			checkChangeType(lostData,true);
    			pollTime=timestamp;
        		lostData=true;
    		}
    	}
    	else if(nowTimestamp>=(20+timestamp)||nowTimestamp==timestamp){
    		setJobThreadStartTime(timestamp);
    		checkChangeType(lostData,false);
    		lostData=false;
			//setNextTime(20);
    	}
    	else if(timestamp>pollTime){//需要立即执行
    		checkChangeType(lostData,true);
    		pollTime=timestamp;
    		lostData=true;
    	}
    	else if(timestamp==pollTime){//20秒以后执行,不做改变
    		checkChangeType(lostData,false);
    		setNextTime(20);
    		lostData=false;
    	}
    	else{
    		if(nowTimestamp>pollTime){
    			if(pointValueDao.getEndDataTime(getPointIds(),nowTimestamp*1000)){
    				pollTime=nowTimestamp;
    			}
    			else{
    				pollTime=timestamp;
    			}
    			checkChangeType(lostData,true);
    			lostData=true;
    		}else if(nowTimestamp<pollTime){
    			//查询nowTimestamp点是否有数据
    			if(pointValueDao.getEndDataTime(getPointIds(),nowTimestamp*1000)){
    				//不做处理,以便于之间调用polltime
    				setNextTime(20);
    				checkChangeType(lostData,false);
    				lostData=false;
    			}
    			else{
    				pollTime=nowTimestamp;
    				checkChangeType(lostData,true);
    				lostData=true;
    			}
    		}
    		else{//相等
    			setNextTime(20);
    			checkChangeType(lostData,false);
    			lostData=false;
    		}
    	}
    	canlceTimerTask2();
    	if(lostData){
    		canlceTimerTask();
    		batchRead.setForceRead(true);
    		setJobThreadStartTime(pollTime);
    		setNextTime(20);
    		setNextPoll();//新的请求立即发送
    	}
    	else{
    		batchRead.setForceRead(false);
    		beginPolling();
    	}
    	//System.out.println("next"+batchRead.isForceRead());
    }	
    //获得数据点
    private List<Integer>  getPointIds() {
    	List<Integer> ids=new ArrayList<Integer>();; 
    	ModbusPointLocatorRT locator;
    	for (DataPointRT dataPoint : dataPoints) {
             locator = dataPoint.getPointLocator();
             if (locator.getVO().isSlaveMonitor()||locator.getVO().getRange()!=RegisterRange.HOLDING_REGISTER_88)
                 continue;
             ids.add(dataPoint.getId());
    	 }
    	return ids;
	}
    
    @Override
    public void addDataPoint(DataPointRT dataPoint) {
        super.addDataPoint(dataPoint);

        // Mark the point as unreliable.
        ModbusPointLocatorVO locatorVO = dataPoint.getVO().getPointLocator();
        if (!locatorVO.isSlaveMonitor())
            dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, true);

        // Slave monitor points.
        if (vo.isCreateSlaveMonitorPoints()) {
            int slaveId = locatorVO.getSlaveId();

            if (locatorVO.isSlaveMonitor())
                // The monitor for this slave. Set it in the map.
                slaveMonitors.put(slaveId, dataPoint);
            else if (!slaveMonitors.containsKey(slaveId)) {
                // A new slave. Add null to the map to ensure we don't do this check again.
                slaveMonitors.put(slaveId, null);

                // Check if a monitor point already exists.检查是否已经存在一个监控点。
                DataPointDao dataPointDao = new DataPointDao();
                boolean found = false;

                List<DataPointVO> points = dataPointDao.getDataPoints(vo.getId(), null);
                for (DataPointVO dp : points) {
                    ModbusPointLocatorVO loc = dp.getPointLocator();
                    if (loc.getSlaveId() == slaveId && loc.isSlaveMonitor()) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // A monitor was not found, so create one
                    DataPointVO dp = new DataPointVO();
                    dp.setXid(dataPointDao.generateUniqueXid());
                    dp.setName(Common.getMessage("dsEdit.modbus.monitorPointName", slaveId));
                    dp.setDataSourceId(vo.getId());
                    dp.setEnabled(true);
                    dp.setLoggingType(LoggingTypes.ON_CHANGE);
                    dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());

                    ModbusPointLocatorVO locator = new ModbusPointLocatorVO();
                    locator.setSlaveId(slaveId);
                    locator.setSlaveMonitor(true);
                    dp.setPointLocator(locator);

                    Common.ctx.getRuntimeManager().saveDataPoint(dp);
                    LOG.info("Monitor point added: " + dp.getXid());
                }
            }
        }
    }

    @Override
    public void removeDataPoint(DataPointRT dataPoint) {
        synchronized (pointListChangeLock) {
            super.removeDataPoint(dataPoint);

            // If this is a slave monitor point being removed, also remove it from the map.
            ModbusPointLocatorVO locatorVO = dataPoint.getVO().getPointLocator();
            if (locatorVO.isSlaveMonitor())
                slaveMonitors.put(locatorVO.getSlaveId(), null);
        }
    }

    @Override
    protected void doPoll(long time) {
    	long time_88=setNextTime(0l);
        if (!modbusMaster.isInitialized()) {
            if (vo.isCreateSlaveMonitorPoints()) {
                // Set the slave monitors to offline
                for (DataPointRT monitor : slaveMonitors.values()) {
                    if (monitor != null) {
                        PointValueTime oldValue = monitor.getPointValue();
                        if (oldValue == null || oldValue.getBooleanValue())
                            monitor.setPointValue(new PointValueTime(false, time), null);
                    }
                }
            }

            return;
        }
        ModbusPointLocatorRT locator;
        BaseLocator<?> modbusLocator;
        Object result;
        //batchRead批量读取
        if (batchRead == null || pointListChanged||changeType) {
        	boolean temp=false;
        	if(batchRead!=null){
        		temp=batchRead.isForceRead();
        	}
            pointListChanged = false;
            batchRead = new BatchRead<ModbusPointLocatorRT>();
            batchRead.setContiguousRequests(vo.isContiguousBatches());//设置连续请求
            batchRead.setErrorsInResults(true);
            batchRead.setExceptionsInResults(true);
            batchRead.setForceRead(temp);
            for (DataPointRT dataPoint : dataPoints) {
                locator = dataPoint.getPointLocator();
                if (!locator.getVO().isSlaveMonitor()) {
                	if(locator.getVO().getRange()==RegisterRange.HOLDING_REGISTER_88){
                		if(checkStart()){
                    		time_88=setNextTime(getLatestTime()/1000+20);
                    	}
                		locator.getVO().setTimestamp(time_88);
                	}
                	 if(batchRead.isForceRead()){
                     	if(locator.getVO().getRange()!=RegisterRange.HOLDING_REGISTER_88)
                     		continue;
                     }
                    modbusLocator = createModbusLocator(locator.getVO());
                    batchRead.addLocator(locator, modbusLocator);
                }
            }
        }
       // System.out.println("time_88"+time_88);
        try {
        	//System.out.println("time_88:"+time_88);
        	if(batchRead.getReadFunctionGroups()!=null)
        		updateBatchRead(batchRead,time_88);
        	//System.out.println("send"+batchRead.isForceRead());
        	//FIXME 发送数据（1~3秒）
            BatchResults<ModbusPointLocatorRT> results = modbusMaster.send(batchRead);
            Map<Integer, Boolean> slaveStatuses = new HashMap<Integer, Boolean>();
            boolean dataSourceExceptions = false;
            boolean havaData = false;
            long[] resultTimes={};
            for (DataPointRT dataPoint : dataPoints) {
            	//System.out.println("kuailaile"+dataPoint.getId());
                locator = dataPoint.getPointLocator();
                if (locator.getVO().isSlaveMonitor())//如果正在补数据,就不写入3#功能点的值,直接跳过
                    continue;
                //System.out.println("jinlaile1111"+dataPoint.getId());
                //System.out.println("jinlaile11114444"+batchRead.isForceRead());
                //System.out.println("jinlaile11115555"+dataPoint.getId());
                if(batchRead.isForceRead()){
                	if(locator.getVO().getRange()!=RegisterRange.HOLDING_REGISTER_88)
                		continue;
                }
                //System.out.println("jinlaile2222"+dataPoint.getId());
                result = results.getValue(locator);
                if (result instanceof ExceptionResult) {
                    ExceptionResult exceptionResult = (ExceptionResult) result;

                    // Raise an event.
                	if(locator.getVO().getRange()==RegisterRange.HOLDING_REGISTER_88)
                		raiseEvent(POINT_READ_EXCEPTION_EVENT, time_88*1000, true, new LocalizableMessage("event.exception2",
                            dataPoint.getVO().getName(), exceptionResult.getExceptionMessage()));
                	else
                		raiseEvent(POINT_READ_EXCEPTION_EVENT, time, true, new LocalizableMessage("event.exception2",
                                dataPoint.getVO().getName(), exceptionResult.getExceptionMessage()));

                    dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, true);

                    // A response, albeit an undesirable one, was received from the slave, so it is online.
                    slaveStatuses.put(locator.getVO().getSlaveId(), true);
                }
                else if (result instanceof ModbusTransportException) {
                    ModbusTransportException e = (ModbusTransportException) result;

                    // Update the slave status. Only set to false if it is not true already.
                    if (!slaveStatuses.containsKey(locator.getVO().getSlaveId()))
                        slaveStatuses.put(locator.getVO().getSlaveId(), false);

                    // Raise an event.
                  //  raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, time, true, getLocalExceptionMessage(e));
                    dataSourceExceptions = true;

                    dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, true);
                }
                else {//区分88号功能和3号功能
                	//System.out.println("point Id: "+dataPoint.getId());
                	//System.out.println("rang  : "+locator.getVO().getRange());
                	if(locator.getVO().getRange()==RegisterRange.HOLDING_REGISTER_88){
                		havaData=true;
                		resultTimes=(long [])results.getTimes(locator);
                	//	Date date=new Date();
                	//	date.setTime(resultTimes[0]*1000);
                		//System.out.println("time1:"+resultTimes[0]*1000);
                		//System.out.println("time2:"+resultTimes[1]*1000);
                	//	System.out.println(date);
                		returnToNormal(POINT_READ_EXCEPTION_EVENT,resultTimes[0]*1000);
                        dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, false);
                        updatePointValue(dataPoint, locator, result, resultTimes[0]*1000);
                        slaveStatuses.put(locator.getVO().getSlaveId(), true);
                	}
                	else{
                		returnToNormal(POINT_READ_EXCEPTION_EVENT,time);
                        dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, false);
                        updatePointValue(dataPoint, locator, result, time);
                        slaveStatuses.put(locator.getVO().getSlaveId(), true);
                	}
                }
            }
            if(havaData){
            	setNextPollingPeriod(resultTimes,time_88);
            }
            if (vo.isCreateSlaveMonitorPoints()) {
                for (Map.Entry<Integer, Boolean> status : slaveStatuses.entrySet()) {
                    DataPointRT monitor = slaveMonitors.get(status.getKey());
                    if (monitor != null) {
                        boolean oldOnline = false;
                        boolean newOnline = status.getValue();

                        PointValueTime oldValue = monitor.getPointValue();
                        if (oldValue != null)
                            oldOnline = oldValue.getBooleanValue();
                        else
                            // Make sure it gets set.
                            oldOnline = !newOnline;

                        if (oldOnline != newOnline){
                        	 locator = monitor.getPointLocator();
                        	if(locator.getVO().getRange()==RegisterRange.HOLDING_REGISTER_88)
                        		monitor.setPointValue(new PointValueTime(newOnline, time_88*1000), null);
                        	else
                        		monitor.setPointValue(new PointValueTime(newOnline, time), null);
                        }
                    }
                }
            }

            if (!dataSourceExceptions){
                // Deactivate any existing event.
               // returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, time);
            }
        }
        catch (ErrorResponseException e) {
            // Should never happen because we set "errorsInResults" to true.
            throw new ShouldNeverHappenException(e);
        }
        catch (ModbusTransportException e) {
            // Should never happen because we set "exceptionsInResults" to true.
            throw new ShouldNeverHappenException(e);
        }
    }

    protected void initialize(ModbusMaster modbusMaster) {
        this.modbusMaster = modbusMaster;
        modbusMaster.setTimeout(vo.getTimeout());
        modbusMaster.setRetries(vo.getRetries());
        modbusMaster.setMaxReadBitCount(vo.getMaxReadBitCount());
        modbusMaster.setMaxReadRegisterCount(vo.getMaxReadRegisterCount());
        modbusMaster.setMaxWriteRegisterCount(vo.getMaxWriteRegisterCount());

        // Add this as a listener to exceptions that occur in the implementation.
        //将此作为一个监听器在执行中发生的异常。
        modbusMaster.setExceptionHandler(this);//设置异常处理程序

        try {
            modbusMaster.init();

            // Deactivate any existing event.停用任何现有的事件。
           // returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
        }
        catch (Exception e) {
           // raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, getLocalExceptionMessage(e));
            LOG.debug("Error while initializing data source", e);
            return;
        }

        super.initialize();
    }
    
    @Override
    public void forceSourceRead(@SuppressWarnings("unused") DataSourceRT ds,long timeStart, long timeEnd) {
        Common.ctx.getRuntimeManager().stopDataSourceForce(ds.getId());
        Common.ctx.getRuntimeManager().startDataSource(ds);
        beginPolling();
        setForceSourceRead(true);
    	setJobThreadStartTime(timeStart/1000);
    	setEndTime(timeEnd/1000);
    	lostData=true;
    }
    
    @Override
    public void forcePointRead(DataPointRT dataPoint) {
        ModbusPointLocatorRT pl = dataPoint.getPointLocator();
        if (pl.getVO().isSlaveMonitor())
            // Nothing to do
            return;

        BaseLocator<?> ml = createModbusLocator(pl.getVO());
        long time = System.currentTimeMillis();

        synchronized (pointListChangeLock) {
            try {
                Object value = modbusMaster.getValue(ml);

                returnToNormal(POINT_READ_EXCEPTION_EVENT, time);
                dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, false);

                updatePointValue(dataPoint, pl, value, time);
            }
            catch (ErrorResponseException e) {
                raiseEvent(POINT_READ_EXCEPTION_EVENT, time, true, new LocalizableMessage("event.exception2", dataPoint
                        .getVO().getName(), e.getMessage()));
                dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, true);
            }
            catch (ModbusTransportException e) {
                // Don't raise a data source exception. Polling should do that.
                LOG.warn("Error during forcePointRead", e);
                dataPoint.setAttribute(ATTR_UNRELIABLE_KEY, true);
            }
        }
    }

    private void updatePointValue(DataPointRT dataPoint, ModbusPointLocatorRT pl, Object value, long time) {
        if (pl.getVO().getDataTypeId() == DataTypes.BINARY)
            dataPoint.updatePointValue(new PointValueTime((Boolean) value, time));
        else if (pl.getVO().getDataTypeId() == DataTypes.ALPHANUMERIC)
            dataPoint.updatePointValue(new PointValueTime((String) value, time));
        else {
            // Apply arithmetic conversions.
            double newValue = ((Number) value).doubleValue();
            newValue *= pl.getVO().getMultiplier();
            newValue += pl.getVO().getAdditive();
            dataPoint.updatePointValue(new PointValueTime(newValue, time));
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        modbusMaster.destroy();
    }

    //
    //
    // Data source interface
    //
    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        ModbusPointLocatorRT pl = dataPoint.getPointLocator();
        BaseLocator<?> ml = createModbusLocator(pl.getVO());

        try {
            // See if this is a numeric value that needs to be converted.
            if (dataPoint.getDataTypeId() == DataTypes.NUMERIC) {
                double convertedValue = valueTime.getDoubleValue();
                convertedValue -= pl.getVO().getAdditive();
                convertedValue /= pl.getVO().getMultiplier();
                modbusMaster.setValue(ml, convertedValue);
            }
            else if (dataPoint.getDataTypeId() == DataTypes.ALPHANUMERIC)
                modbusMaster.setValue(ml, valueTime.getStringValue());
            else
                modbusMaster.setValue(ml, valueTime.getBooleanValue());
            dataPoint.setPointValue(valueTime, source);

            // Deactivate any existing event.
            returnToNormal(POINT_WRITE_EXCEPTION_EVENT, valueTime.getTime());
        }
        catch (ModbusTransportException e) {
            // Raise an event.
            raiseEvent(POINT_WRITE_EXCEPTION_EVENT, valueTime.getTime(), true, new LocalizableMessage(
                    "event.exception2", dataPoint.getVO().getName(), e.getMessage()));
            LOG.info("Error setting point value", e);
        }
        catch (ErrorResponseException e) {
            raiseEvent(POINT_WRITE_EXCEPTION_EVENT, valueTime.getTime(), true, new LocalizableMessage(
                    "event.exception2", dataPoint.getVO().getName(), e.getErrorResponse().getExceptionMessage()));
            LOG.info("Error setting point value", e);
        }
    }

    public static BaseLocator<?> createModbusLocator(ModbusPointLocatorVO vo) {
        return BaseLocator.createLocator(vo.getSlaveId(), vo.getRange(), vo.getOffset(),vo.getTimestamp(), vo.getModbusDataType(),
                vo.getBit(), vo.getRegisterCount(), Charset.forName(vo.getCharset()));
    }

    public static LocalizableMessage localExceptionMessage(Exception e) {
        if (e instanceof ModbusTransportException) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException)
                return new LocalizableMessage("event.modbus.noResponse", ((ModbusTransportException) e).getSlaveId());
            if (cause instanceof ConnectException)
                return new LocalizableMessage("common.default", e.getMessage());
        }

        return DataSourceRT.getExceptionMessage(e);
    }

    protected LocalizableMessage getLocalExceptionMessage(Exception e) {
        return localExceptionMessage(e);
    }

    //
    //
    // MessagingConnectionListener interface
    //
    public void receivedException(Exception e) {
        LOG.warn("Modbus exception", e);
      //  raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessage(
     //           "event.modbus.master", e.getMessage()));
    }
}
