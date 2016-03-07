package com.myth.taskdispatcher;

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

    public TaskDispatcher() {
        if (messageQueue == null) {
            messageQueue = new PriorityBlockingQueue<ITask>(10, new TaskComparator());
        }
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(createWrapper());
    }

    private class TaskComparator implements Comparator<ITask> {
        @Override
        public int compare(ITask lhs, ITask rhs) {
            return (lhs.getPriority() > rhs.getPriority()) ? 1 : -1;
        }
    }

    public void execute(ITask task) {
        synchronized (messageQueue) {
            messageQueue.add(task);
            messageQueue.notify();
        }
    }

    private Runnable createWrapper(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ITask activeTask ;
                while(true) {
                    activeTask = null ;
                    synchronized (messageQueue) {
                        if(messageQueue.size() > 0) {
                            activeTask = messageQueue.peek();
                        }
                        // First sleep before we can add any further request and wake up
                        if(activeTask == null) {
                            try {
                                messageQueue.wait() ;
                            } catch(InterruptedException e) {
                                e.printStackTrace() ;
                            }
                        }
                    }

                    if(activeTask != null) {
                        try{
                            activeTask.run() ;
                        }catch(Throwable e) {
                            e.printStackTrace();
                        }finally{
                            activeTask.onComplete();
                            synchronized (activeTask) {
                                if(messageQueue.size() > 0)
                                    messageQueue.poll() ;
                            }
                        }
                    }
                }
            }
        } ;

        return runnable ;
    }
}