package com.corner23.android.logcatlivewallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.service.wallpaper.WallpaperService;
// import android.text.Layout;
// import android.text.StaticLayout;
// import android.text.TextPaint;
import android.util.Log;
// import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LogcatLiveWallpaper extends WallpaperService {

	public static final String SHARED_PREFS_NAME = "llw_settings";
	private static final String TAG = "LogcatLiveWallpaper";

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
	    
	@Override
	public Engine onCreateEngine() {
		Log.d(TAG, "onCreateEngine");
		return new LogcatEngine();
	}
	
	class LogcatMsg {
		int color;
		String msg;
		
		public LogcatMsg(int c, String m) {
			color = c;
			msg = m;
		}
	}
	
	class LogcatEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final int MAX_LOG_MSG = 1024;
		private static final int MSG_NEWLINE = 1;
		private static final int THEME_DDMS = 0;
		private static final int THEME_OLD = 1;
		
		private final Paint mPaint = new Paint();
		private boolean bVisible;
		private int nXOffset = 0;        

		private LogcatThread mLogcatThread;
		private LogcatMsg[] logmsg = new LogcatMsg[MAX_LOG_MSG];
		private int index = 0;
		
		private SharedPreferences mPrefs;
		private boolean bWrapText = false;
		private int nFontsize = 16;
		private int nTheme = THEME_DDMS;		
		private int ColorV, ColorD, ColorI, ColorW, ColorE, ColorF, ColorFront, ColorBack;
		private Typeface mTypeface = null;
		private boolean bFullscreen;
		int head_height;
		int tail_height;
		int step;
		int NUM_MSG_TO_SHOW;
		
	    private final Handler mHandler = new Handler() {
	    	@Override
	    	public void handleMessage(Message msg) {
	    		switch (msg.what) {
	    		case MSG_NEWLINE:
	    			handleMessageNewline(msg);
	    			break;
	    		default:
	    			super.handleMessage(msg);
	    		}
	    	}
	    };
		
	    private void handleMessageNewline(Message msg) {
	    	String line = (String) msg.obj;
	    	if (index == MAX_LOG_MSG) {
	    		index = 0;
	    	}

	    	int color = ColorFront;
	    	int level = line.charAt(0);
			switch (level) {
			case 'V': color = ColorV; break;
			case 'D': color = ColorD; break;
			case 'I': color = ColorI; break;
			case 'W': color = ColorW; break;
			case 'E': color = ColorE; break;
			case 'F': color = ColorF; break;
			}
			
			logmsg[index] = new LogcatMsg(color, line);
	    	index++;
	    }
	    
	    LogcatEngine() {
			final Paint paint = mPaint;
			paint.setColor(Color.GREEN);
			paint.setAntiAlias(true);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStyle(Paint.Style.STROKE);
			
	    	mPrefs = LogcatLiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
	    	mPrefs.registerOnSharedPreferenceChangeListener(this);
	    	onSharedPreferenceChanged(mPrefs, null);
		}
		
		private final Runnable drawPaper = new Runnable() {
			public void run() {
				draw();
			}
		};

		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			// bWrapText = prefs.getBoolean("pref_wrap_text", false);
			bFullscreen = prefs.getBoolean("pref_full_screen", false);
			nFontsize = Integer.parseInt(prefs.getString("pref_font_size", "16"));
			mPaint.setTextSize(nFontsize);

			nTheme = Integer.parseInt(prefs.getString("pref_theme", "0"));
			if (nTheme == THEME_DDMS) {
				ColorFront = Color.parseColor("#121212");
				ColorBack = Color.WHITE;
				ColorV = Color.parseColor("#121212");
				ColorD = Color.parseColor("#00006C");
				ColorI = Color.parseColor("#20831B");
				ColorW = Color.parseColor("#FD7916");
				ColorE = Color.parseColor("#FD0010");
				ColorF = Color.parseColor("#ff0066");
			} else if (nTheme == THEME_OLD) {
				ColorFront = Color.parseColor("#AA00FF00");
				ColorBack = Color.BLACK;
				ColorV = Color.parseColor("#AA00FF00");
				ColorD = Color.parseColor("#AAFFFF00");
				ColorI = Color.parseColor("#AA00FF00");
				ColorW = Color.parseColor("#AAFFFF00");
				ColorE = Color.parseColor("#AAFF0000");
				ColorF = Color.parseColor("#AAFF0000");
			}
			
			String font = prefs.getString("pref_font", "arcade.ttf");
			mTypeface = Typeface.createFromAsset(getAssets(), font);
			mPaint.setTypeface(mTypeface);

			head_height = bFullscreen ? 0 : 60;
			tail_height = bFullscreen ? 0 : 60;
			step = nFontsize + 2;
			NUM_MSG_TO_SHOW = (LogcatLiveWallpaper.this.getResources().getDisplayMetrics().heightPixels 
					- head_height - tail_height) / step;
		}

/*		
		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "onSurfaceCreated");
			super.onSurfaceCreated(holder);
		}

		@Override
		public int getDesiredMinimumHeight() {
			Log.d(TAG, "getDesiredMinimumHeight");
			return super.getDesiredMinimumHeight();
		}

		@Override
		public int getDesiredMinimumWidth() {
			Log.d(TAG, "getDesiredMinimumWidth");
			return super.getDesiredMinimumWidth();
		}

		@Override
		public SurfaceHolder getSurfaceHolder() {
			// Log.d(TAG, "getSurfaceHolder");
			return super.getSurfaceHolder();
		}

		@Override
		public boolean isPreview() {
			Log.d(TAG, "isPreview");
			return super.isPreview();
		}

		@Override
		public boolean isVisible() {
			Log.d(TAG, "isVisible");
			return super.isVisible();
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			Log.d(TAG, "onCommand");
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			Log.d(TAG, "onTouchEvent");
			super.onTouchEvent(event);
		}

		@Override
		public void setTouchEventsEnabled(boolean enabled) {
			Log.d(TAG, "setTouchEventsEnabled:" + enabled);
			super.setTouchEventsEnabled(enabled);
		}


		@Override
		public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
			Log.d(TAG, "onDesiredSizeChanged");
			super.onDesiredSizeChanged(desiredWidth, desiredHeight);
		}
*/
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			// Log.d(TAG, "onCreate (engine)");
			super.onCreate(surfaceHolder);

			setTouchEventsEnabled(false);

	        mLogcatThread = new LogcatThread() {
	        	public void onError(final String msg, Throwable e) {
	        	}

	        	public void onNewline(String line) {
	        		Message msg = mHandler.obtainMessage(MSG_NEWLINE);
	        		msg.obj = line;
	        		mHandler.sendMessage(msg);
	        	}
	        };

	        mLogcatThread.start();
		}

		@Override
		public void onDestroy() {
			// Log.d(TAG, "onDestroy (engine)");
			super.onDestroy();

			if (mLogcatThread != null) {
	            mLogcatThread.stopLogcat();
	            mLogcatThread = null;
			}

            mHandler.removeCallbacks(drawPaper);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			// Log.d(TAG, "onOffsetsChanged");
			if (bWrapText) {
				nXOffset = 0;
			} else {
				nXOffset = xPixelOffset;
			}
			draw();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			// Log.d(TAG, "onSurfaceChanged:" + width + "," + height);
			draw();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			// Log.d(TAG, "onSurfaceDestroyed");
			super.onSurfaceDestroyed(holder);
			bVisible = false;
			mHandler.removeCallbacks(drawPaper);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			// Log.d(TAG, "onVisibilityChanged:" + visible);
			bVisible = visible;
			if (visible) {
				draw();
			} else {
				mHandler.removeCallbacks(drawPaper);
			}
		}

		void draw() {
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					drawText(c);
				}
			} finally {
				if (c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}
			
			mHandler.removeCallbacks(drawPaper);
			if (bVisible) {
				mHandler.postDelayed(drawPaper, 1000 / 25);
			}
		}
		
		void drawText(Canvas c) {
			// clean screen
			c.drawColor(ColorBack);

			// determine first log to print
			int idx_msg_to_show = 0;			
			if (index < NUM_MSG_TO_SHOW) {
				idx_msg_to_show = MAX_LOG_MSG - (NUM_MSG_TO_SHOW - index);
			} else {
				idx_msg_to_show = index - NUM_MSG_TO_SHOW;
			}
			
			// loop to show message
			for (int yPos = head_height, count = 0; count < NUM_MSG_TO_SHOW ; yPos += step, idx_msg_to_show++, count++) {
				if (idx_msg_to_show == MAX_LOG_MSG) {
					idx_msg_to_show = 0;
				}
				
				if (logmsg[idx_msg_to_show] != null) {
					/*
					if (bWrapText) {
						TextPaint tp = new TextPaint();
						tp.setColor(logmsg[idx_msg_to_show].color);
						tp.setTextSize(nFontsize);
						c.save();
						c.translate(10 + nXOffset, yPos);
						StaticLayout sl = new StaticLayout(logmsg[idx_msg_to_show].msg, tp, 480, Layout.Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
						sl.draw(c);
						c.restore();
					} 
					else 
					*/
					{
						mPaint.setColor(logmsg[idx_msg_to_show].color);
						c.drawText(// "I:" + index + ",N:" + idx_msg_to_show + ":" + 
								logmsg[idx_msg_to_show].msg, 10 + nXOffset, yPos, mPaint);
					}
				}
			}
		}
	}
}