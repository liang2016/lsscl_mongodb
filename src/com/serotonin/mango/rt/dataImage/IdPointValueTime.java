/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.rt.dataImage;

import com.serotonin.mango.rt.dataImage.types.MangoValue;

public class IdPointValueTime extends PointValueTime implements Comparable<IdPointValueTime>{
    private static final long serialVersionUID = 1L;

    private final int dataPointId;
    private long time;

    public IdPointValueTime(int dataPointId, MangoValue value, long time) {
        super(value, time);
        this.dataPointId = dataPointId;
        this.time = time;
    }

    public int getDataPointId() {
        return dataPointId;
    }

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int compareTo(IdPointValueTime pvt) {
		return (int) (this.time-pvt.getTime());
	}
    
}
