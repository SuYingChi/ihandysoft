package com.ihs.keyboardutils.threadpool;

import com.ihs.keyboardutils.threadpool.runnables.HSRunnable;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ihandysoft on 16/11/21.
 */

public class HSThreadPool {

    private ThreadPoolExecutor threadPoolExecutor;

    private HSThreadPool(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void post(HSRunnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    public final static class Builder {
        private int coreThreadSize = 6;

        public Builder(int coreThreadSize) {
            this.coreThreadSize = coreThreadSize;
        }

        public HSThreadPool build() {
            return new HSThreadPool(new ThreadPoolExecutor(coreThreadSize, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue()) {
                @Override
                protected void beforeExecute(Thread t, Runnable r) {
                    super.beforeExecute(t, r);
                    HSRunnable hsRunnable = (HSRunnable) r;
                    t.setPriority(hsRunnable.getThreadPriority());
                }
            });
        }
    }


}
