package com.myth.taskdispatcher;

/**
 * Created by mitesh on 17/08/15.
 */
public interface ITask extends Runnable {
    public String getName();
    public int getPriority();
    public void onComplete();
}
