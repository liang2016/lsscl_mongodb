/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.rt.event.detectors;

import java.util.Date;

import com.serotonin.mango.Common;
import com.serotonin.mango.util.timeout.TimeoutClient;
import com.serotonin.mango.util.timeout.TimeoutTask;
import com.serotonin.timer.TimerTask;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * This class is a base class for detectors that need to schedule timeouts for their operation. Subclasses may use
 * schedules for timeouts that make them active, or that make them inactive.
 * 
 *  
 */
abstract public class TimeoutDetectorRT extends PointEventDetectorRT implements TimeoutClient {
    /**
     * Internal configuration field. The millisecond version of the duration fields.
     */
    private long durationMS;

    /**
     * Internal configuration field. The human-readable description of the duration fields.
     */
    private LocalizableMessage durationDescription;

    /**
     * Internal configuration field. The unique name for this event producer to be used in the scheduler (if required).
     */
    private TimerTask task;

    @Override
    public void initialize() {
        durationMS = Common.getMillis(vo.getDurationType(), vo.getDuration());
        durationDescription = vo.getDurationDescription();

        super.initialize();
    }

    @Override
    public void terminate() {
        super.terminate();
        cancelTask();
    }

    protected LocalizableMessage getDurationDescription() {
        return durationDescription;
    }

    protected long getDurationMS() {
        return durationMS;
    }

    protected void scheduleJob(long timeout) {
        if (task != null)
            cancelTask();
        task = new TimeoutTask(new Date(timeout), this);
    }

    protected void unscheduleJob() {
        cancelTask();
    }

    @Override
    synchronized public final void scheduleTimeout(long fireTime) {
        scheduleTimeoutImpl(fireTime);
        task = null;
    }

    abstract protected void scheduleTimeoutImpl(long fireTime);
    public void resumeScheduleTimeout(long fireTime) {
    	
    }
    synchronized private void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
