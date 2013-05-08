package com.xengine.android.full.utils;

import android.os.AsyncTask;

import java.util.concurrent.RejectedExecutionException;


public abstract class XSafeAsyncTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

    /**
     * 保证执行。优先尝试异步执行，如果失败则同步执行
     * @param params
     */
	public void safeExecute(Params... params) {
		try {
			execute(params);
		} catch(RejectedExecutionException e) {
            // 查看ThreadPoolExecutor的源码得知，RejectedExecutionException是由于
            // 同时并发的线程超过上限所致。在AysncTask就是同时并发超过128个

			// Failed to start in the background, so do it in the foreground
			onPreExecute();
			if (isCancelled()) {
				onCancelled();
			} else {
				onPostExecute(doInBackground(params));
			}
		}
	}
}
