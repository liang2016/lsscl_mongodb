/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.rt.event.detectors;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * The PositiveCusumDetector is used to detect occurances of point values exceeding the given CUSUM limit for a given
 * duration. For example, a user may need to have an event raised when a temperature CUSUM exceeds some value for 10
 * minutes or more.
 * 
 *  
 */
public class PositiveCusumDetectorRT extends TimeDelayedEventDetectorRT {
    /**
     * State field. The current positive CUSUM for the point.
     */
    private double cusum;

    /**
     * State field. Whether the positive CUSUM is currently active or not. This field is used to prevent multiple events
     * being raised during the duration of a single positive CUSUM exceed.
     */
    private boolean positiveCusumActive;

    private long positiveCusumActiveTime;
    private long positiveCusumInactiveTime;

    /**
     * State field. Whether the event is currently active or not. This field is used to prevent multiple events being
     * raised during the duration of a single positive CUSUM exceed.
     */
    private boolean eventActive;

    public PositiveCusumDetectorRT(PointEventDetectorVO vo) {
        this.vo = vo;
    }

    @Override
    public LocalizableMessage getMessage() {
        String name = vo.njbGetDataPoint().getName();
        String prettyLimit = vo.njbGetDataPoint().getTextRenderer().getText(vo.getLimit(), TextRenderer.HINT_SPECIFIC);
        LocalizableMessage durationDescription = getDurationDescription();
        if (durationDescription == null)
            return new LocalizableMessage("event.detector.posCusum", name, prettyLimit);
        return new LocalizableMessage("event.detector.posCusumPeriod", name, prettyLimit, durationDescription);
    }

    public boolean isEventActive() {
        return eventActive;
    }

    /**
     * This method is only called when the positive CUSUM changes between being active or not, i.e. if the point's CUSUM
     * is currently above the limit, then it should never be called with a value of true.
     * 
     * @param b
     */
    private void changePositiveCusumActive() {
        positiveCusumActive = !positiveCusumActive;

        if (positiveCusumActive)
            // Schedule a job that will call the event active if it runs.
            scheduleJob(positiveCusumActiveTime);
        else
            unscheduleJob();
    }

    @Override
    synchronized public void pointUpdated(PointValueTime newValue) {
        double newDouble = newValue.getDoubleValue();

        cusum += newDouble - vo.getWeight();
        if (cusum < 0)
            cusum = 0;

        if (cusum > vo.getLimit()) {
            if (!positiveCusumActive) {
                positiveCusumActiveTime = newValue.getTime();
                changePositiveCusumActive();
            }
        }
        else {
            if (positiveCusumActive) {
                positiveCusumInactiveTime = newValue.getTime();
                changePositiveCusumActive();
            }
        }
    }

    /**
     * This method is only called when the event changes between being active or not, i.e. if the event currently is
     * active, then it should never be called with a value of true. That said, provision is made to ensure that the
     * postive CUSUM is active before allowing the event to go active.
     * 
     * @param b
     */
    @Override
    synchronized public void setEventActive(boolean b) {
        eventActive = b;
        if (eventActive) {
            // Just for the fun of it, make sure that the positive CUSUM is active.
            if (positiveCusumActive)
                // Ok, things are good. Carry on...
                // Raise the event.
                raiseEvent(positiveCusumActiveTime + getDurationMS(), createEventContext());
            else
                eventActive = false;
        }
        else
            // Deactive the event.
            returnToNormal(positiveCusumInactiveTime);
    }
}
