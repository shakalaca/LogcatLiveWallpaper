package com.corner23.android.logcatlivewallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class LogcatThread extends Thread {
	private static final String[] LOGCAT_CMD = new String[] { "logcat" };
	private static final int BUFFER_SIZE = 1024;
	private int mLines = 0;
	private Process mLogcatProc = null;
	private boolean mRunning = false;
	
	public void run() {
		mRunning = true;
		
		try	{
			mLogcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);
		} catch (IOException e) {
			onError("Can't start " + LOGCAT_CMD[0], e);
			return;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()), BUFFER_SIZE);

			String line;
			while (mRunning && (line = reader.readLine()) != null) {
				if (!mRunning) {
					break;
				}
				
				if (line.length() == 0) {
					continue;
				}
				
				onNewline(line);
				mLines++;
			}
		} catch (IOException e) {
			onError("Error reading from process " + LOGCAT_CMD[0], e);
		} finally {
			if (reader != null) {
				try { 
					reader.close(); 
				} catch (IOException e) {
				}
			}
			stopLogcat();
		}
	}

	public void stopLogcat() {
		if (mLogcatProc == null)
			return;
	        
		mLogcatProc.destroy();
		mLogcatProc = null;
		mRunning = false;
	}
	
	public int getLineCount() {
		return mLines;
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public abstract void onError(String msg, Throwable e);
	public abstract void onNewline(String line);
}
