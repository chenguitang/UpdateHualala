package com.posin.updatehualala.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TelnetSuProc {

	private Process mProc;
	private volatile boolean mExitPending = false;
	private volatile boolean mRunning = false;
	private ArrayList<String> mLogs = new ArrayList<String>();
	
	public TelnetSuProc(int port) throws IOException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		//pb.command("/system/bin/busybox", "telnet", "127.0.0.1", String.valueOf(port));
		pb.command("/system/xbin/ru");
		mProc = pb.start();
		mRunning = true;
		(new Thread(mRun)).start();
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public void cleanLogs() {
		synchronized(this) {
			mLogs.clear();
		}
	}
	
	public void getOutputs(ArrayList<String> out) {
		synchronized(this) {
			out.addAll(mLogs);
			mLogs.clear();
		}
	}
	
	public void execute(String cmd) throws UnsupportedEncodingException, IOException {
		mProc.getOutputStream().write(cmd.getBytes("UTF-8"));
	}
	
	public void stop() {
		mExitPending = true;
	}
	
	private final Runnable mRun = new Runnable() {
		@Override
		public void run() {
			BufferedReader br = null;
			
			try {
				br = new BufferedReader(new InputStreamReader(mProc.getInputStream(), "UTF-8"));
				String s;
				while(!mExitPending && (s=br.readLine()) != null) {
					if(s.isEmpty())
						continue;
					synchronized(TelnetSuProc.this) {
						mLogs.add(s);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mRunning = false;
				if(br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mProc.destroy();
			}
			
		}
	};
}
