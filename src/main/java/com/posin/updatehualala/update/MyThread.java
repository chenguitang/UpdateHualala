package com.posin.updatehualala.update;

import android.util.Log;

import java.io.IOException;

/**
 * FileName: MyThread
 * Author: Greetty
 * Time: 2018/6/1 19:47
 * Desc: TODO
 */
public abstract class MyThread implements Runnable {

	private static final String TAG = "MyThread";

	protected final String mName;
	protected boolean mRunning = false;
	protected boolean mExitPending = false;
	
	public static interface OnStop {
		public void onStop(MyThread t);
	}
	
	private OnStop mOnStop;
	
	public MyThread(String name, OnStop onStop) {
		mName = name;
		mOnStop = onStop;
	}
	
	public String getName() {
		return mName;
	}
	
	public boolean exitPending() {
		return mExitPending;
	}
	
	public synchronized boolean isRunning() {
		return mRunning;
	}
	
	public synchronized void start() {
		if(mRunning) {
			return;
		}
		
		mExitPending = false;
		(new Thread(this)).start();
	}
	
	public synchronized void stop() {
		if(!mRunning)
			return;
		
		mExitPending = true;
		onExitRequest();
	}

	@Override
	public void run() {
		Log.d(TAG, mName + " started.");

		mRunning = true;
		
		try {
			try {
				onRun();
			} catch (Throwable e) {
				e.printStackTrace();
			}

		} finally {
			mRunning = false;
			Log.d(TAG, mName + " stoped.");

			try {
				if(mOnStop!=null)
					mOnStop.onStop(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract void onRun() throws IOException;
	protected abstract void onExitRequest();

	public static void sleepMs(int ms) {
		long end = System.currentTimeMillis()+ms;
		int loop = ms/10+1;
		for(int i=0; i<loop; i++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			if(System.currentTimeMillis()>=end)
				return;
		}
	}
	
	public static void sleepSecond(int second) {
		long tick = System.currentTimeMillis() + ((long)second)*1000;
		int loop = second*2;
		for(int i=0; i<loop; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			if(System.currentTimeMillis() >= tick)
				break;
		}
	}

	public static void sleepMiniute(int minute) {
		long tick = System.currentTimeMillis() + ((long)minute)*(60*1000);
		int loop = minute*60;
		for(int i=0; i<loop; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if(System.currentTimeMillis() >= tick)
				break;
		} 
	}
}
