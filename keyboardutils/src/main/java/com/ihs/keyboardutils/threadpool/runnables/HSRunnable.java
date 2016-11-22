package com.ihs.keyboardutils.threadpool.runnables;

/**
 * Created by ihandysoft on 16/11/21.
 */

public abstract class HSRunnable implements Runnable {

    private int priority;
    private boolean isCancelled;

    protected HSRunnable(int priority) {
        this.priority = priority;
    }

    public void cancel() {
        isCancelled = true;
    }

    protected boolean isCanncelled() {
        return isCancelled;
    }

    public int getThreadPriority() {
        return priority;
    }

}
