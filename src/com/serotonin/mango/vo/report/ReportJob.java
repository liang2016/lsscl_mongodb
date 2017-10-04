/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.vo.report;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.maint.work.ReportWorkItem;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.timer.TimerTrigger;

/**
 *  
 */
public class ReportJob extends TimerTask {
    private static final Map<Integer, ReportJob> JOB_REGISTRY = new HashMap<Integer, ReportJob>();

    public static void scheduleReportJob(ReportVO report) {
        synchronized (JOB_REGISTRY) {
            // Ensure that there is no existing job.
            unscheduleReportJob(report);

            if (report.isSchedule()) {
                CronTimerTrigger trigger;
                if (report.getSchedulePeriod() == ReportVO.SCHEDULE_CRON) {
                    try {
                        trigger = new CronTimerTrigger(report.getScheduleCron());
                    }
                    catch (ParseException e) {
                        throw new ShouldNeverHappenException(e);
                    }
                }
                else
                    trigger = Common.getCronTrigger(report.getSchedulePeriod(), report.getRunDelayMinutes() * 60);

                ReportJob reportJob = new ReportJob(trigger, report);
                JOB_REGISTRY.put(report.getId(), reportJob);
                Common.timer.schedule(reportJob);
            }
        }
    }

    public static void unscheduleReportJob(ReportVO report) {
        synchronized (JOB_REGISTRY) {
            ReportJob reportJob = JOB_REGISTRY.remove(report.getId());
            if (reportJob != null)
                reportJob.cancel();
        }
    }

    private final ReportVO report;

    private ReportJob(TimerTrigger trigger, ReportVO report) {
        super(trigger);
        this.report = report;
    }

    @Override
    public void run(long runtime) {
        ReportWorkItem.queueReport(report);
    }
}
