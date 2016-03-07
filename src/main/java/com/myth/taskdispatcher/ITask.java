package com.myth.taskdispatcher;

/**
 * Created by mitesh on 17/08/15.
 */
public interface ITask extends Runnable {
    // Specify name of a task.
    public String getName();
    // Specify priority of task.
    public int getPriority();
    // Called from non-ui thread.
    public void onComplete();
    // Called on ui thread.
    public void onUiThreadComplete();
}
