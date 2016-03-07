package com.myth.taskdispatcher;

import android.os.Handler;
import android.os.Looper;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by mitesh on 17/08/15.
 */
public class TaskDispatcher {
    protected PriorityBlockingQueue<ITask> messageQueue = null;
    ExecutorService executorService;
    Handler mainHandler;

    public TaskDispatcher() {
    }

    private Handler getHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private void runOnCompleteInUiThread(final ITask task) {
        this.getHandler().post(new Runnable() {
            @Override
            public void run() {
                task.onUiThreadComplete();
            }
        });
    }

    private PriorityBlockingQueue getMessageQueue() {
        if (messageQueue == null) {
            messageQueue = new PriorityBlockingQueue<ITask>(10, new TaskComparator());
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(createWrapper());
        }
        return messageQueue;
    }

    private static class TaskComparator implements Comparator<ITask> {
        @Override
        public int compare(ITask lhs, ITask rhs) {
            return (lhs.getPriority() > rhs.getPriority()) ? 1 : -1;
        }
    }

    // BlockingQueue implementations are thread-safe.
    // so no need to synchronize.
    public void execute(ITask task) {
        this.getMessageQueue().add(task);
    }

    private Runnable createWrapper(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ITask activeTask = null ;
                while(true) {
                    try {
                        /**
                         * Retrieves and removes the head of this queue, waiting
                         * if necessary until an element becomes available.
                         */
                        activeTask = messageQueue.take() ;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(activeTask != null) {
                        try{
                            activeTask.run() ;
                        }catch(Throwable e) {
                            e.printStackTrace();
                        }finally{
                            // Runs on same thread.
                            activeTask.onComplete();
                            // Runs on main thread.
                            runOnCompleteInUiThread(activeTask);
                        }
                    }
                }
            }
        } ;

        return runnable ;
    }
}