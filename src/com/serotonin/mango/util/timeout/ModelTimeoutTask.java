package com.serotonin.mango.util.timeout;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.timer.OneTimeTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.timer.TimerTrigger;

public class ModelTimeoutTask<T> extends TimerTask {
    private final ModelTimeoutClient<T> client;
    private final T model;
    private static final Log LOG = LogFactory.getLog(ModelTimeoutTask.class);
    public ModelTimeoutTask(long delay, ModelTimeoutClient<T> client, T model) {
        this(new OneTimeTrigger(delay), client, model);
    }

    public ModelTimeoutTask(Date date, ModelTimeoutClient<T> client, T model) {
        this(new OneTimeTrigger(date), client, model);
    }

    public ModelTimeoutTask(TimerTrigger trigger, ModelTimeoutClient<T> client, T model) {
        super(trigger);
        this.client = client;
        this.model = model;
        Common.timer.schedule(this);
    }

    @Override
    protected void run(long runtime) {
    	try{
        client.scheduleTimeout(model, runtime);
    	}catch(Exception e){
    		LOG.error(e);
    	}
    }
}
