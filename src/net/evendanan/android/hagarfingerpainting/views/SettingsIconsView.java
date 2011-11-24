/*
 * Copyright (C) 2011 Menny Even Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
http://blog.evendanan.net/2011/08/Fingerpainting-app-for-Hagar-OR-Multitouch-sample-code
*/
package net.evendanan.android.hagarfingerpainting.views;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class SettingsIconsView extends View {
	
	public static interface SettingsIconsTouchedListener
	{
		void onToolboxIconTouched();
		void onSettingsIconTouched();
	}
	
	private static final String TAG = "SettingsIconsView";
    
//	private static int msCurrentColorIndex = 0;
//	private static final int[] msHintTextColor = new int[]
//	                                                    {
//		Color.BLACK,
//		Color.BLUE,
//		Color.CYAN,
//		Color.DKGRAY,
//		Color.GRAY,
//		Color.GREEN,
//		Color.LTGRAY,
//		Color.MAGENTA,
//		Color.RED,
//		Color.WHITE,
//		Color.YELLOW
//		
//	                                                    };
    private final DisplayMetrics mDisplayMetrics;

    private final int mWidth;
    private final int mHeight;
    private float mDragIconX;
    private float mDragIconY;
    private final Bitmap mDragIcon;
    private final float mDragIconRadius;
    
    private final Bitmap mTargetToolsIcon;
    private final float mTargetToolsIconX;
    private final float mTargetToolsIconY;
    private final float mTargetToolsIconRadius;
    private final String mDragToToolBoxText;
    private final float mDragToToolBoxTextX;
    private final Bitmap mTargetSettingsIcon;
    private final float mTargetSettingsIconX;
    private final float mTargetSettingsIconY;
    private final float mTargetSettingsIconDiameter;
    private final String mDragToSettingsText;
    private final float mDragToSettingsTextX;
    
    private final Paint mPaint;
    
//    private static final int CHANGE_COLOR_DELAY = 250;
//    private final Runnable mChangeHintTextColor = new Runnable() {
//		@Override
//		public void run() {
//			if (mTouched)
//			{
//				msCurrentColorIndex++;
//				invalidate();
//			}
//		}
//	};
//	
//	private final Handler mHandler;
	private boolean mTouched;
	
	private SettingsIconsTouchedListener mListener;
    
	public SettingsIconsView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(1);
        mDisplayMetrics = c.getResources().getDisplayMetrics();
        mHeight = mDisplayMetrics.heightPixels;
        mWidth = mDisplayMetrics.widthPixels;
        
        mDragIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.settings_drag_icon);
        mDragIconRadius = Math.max(mDragIcon.getWidth(), mDragIcon.getHeight()) /2;
        mTargetToolsIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.toolbox_target_icon);
        mTargetToolsIconRadius = Math.max(mTargetToolsIcon.getWidth(), mTargetToolsIcon.getHeight())/2;
        mTargetToolsIconX = mWidth-mTargetToolsIconRadius*2;
        mTargetToolsIconY = (mHeight - mTargetToolsIconRadius)/2;
        
        mTargetSettingsIcon = BitmapFactory.decodeResource(c.getResources(), android.R.drawable.ic_menu_preferences);
        mTargetSettingsIconDiameter = Math.max(mTargetSettingsIcon.getWidth(), mTargetSettingsIcon.getHeight());
        mTargetSettingsIconX = mWidth-mTargetSettingsIconDiameter;
        mTargetSettingsIconY = mHeight - mTargetSettingsIconDiameter;
        
        Typeface tf = Typeface.createFromAsset(c.getAssets(), "fonts/schoolbell.ttf");
        mPaint.setTypeface(tf);
        mPaint.setTextSize(c.getResources().getDimensionPixelSize(R.dimen.painter_name_text_size));
        mDragToToolBoxText = c.getString(R.string.drag_to_toolbox);
        mDragToSettingsText = c.getString(R.string.drag_to_settings);
        mDragToToolBoxTextX = mTargetToolsIconX-mPaint.measureText(mDragToToolBoxText);
        mDragToSettingsTextX = mTargetSettingsIconX-mPaint.measureText(mDragToSettingsText);
        
//        mHandler = new Handler();
        
        resetIcons();
    }
	
	public void setOnSettingsIconsTouchedListener(SettingsIconsTouchedListener listener)
	{
		mListener = listener;
	}

    private void resetIcons() {
    	mTouched = false;
    	
    	mDragIconX = mWidth - (mDragIconRadius*1.5f);
    	mDragIconY = 0 - (mDragIconRadius*0.5f);
    	
    	invalidate();
    	
    	//mHandler.removeCallbacks(mChangeHintTextColor);
	}

	@Override
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.translate(0, 0);
		
		mPaint.setColor(mTouched? 0x900505A0 : 0x300505A0);
		canvas.drawCircle(mDragIconX + mDragIconRadius, mDragIconY + mDragIconRadius, mDragIconRadius*1.25f, mPaint);
		canvas.drawBitmap(mDragIcon, mDragIconX, mDragIconY, mPaint);
		if (mTouched)
		{
			mPaint.setShadowLayer(3, 3, 3, Color.BLACK);
			mPaint.setColor(Color.LTGRAY/*msHintTextColor[msCurrentColorIndex % msHintTextColor.length]*/);
			canvas.drawText(mDragToToolBoxText, mDragToToolBoxTextX, mTargetToolsIconY + mTargetToolsIconRadius, mPaint);
			canvas.drawBitmap(mTargetToolsIcon, mTargetToolsIconX, mTargetToolsIconY, mPaint);
			canvas.drawText(mDragToSettingsText, mDragToSettingsTextX, mTargetSettingsIconY - mTargetSettingsIconDiameter/2, mPaint);
			canvas.drawBitmap(mTargetSettingsIcon, mTargetSettingsIconX, mTargetSettingsIconY, mPaint);
			mPaint.setShadowLayer(0, 0, 0, Color.BLACK);
			//mHandler.postDelayed(mChangeHintTextColor, CHANGE_COLOR_DELAY);
		}
    }
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	setMeasuredDimension(mWidth, mHeight);
    }
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	mTouched = isTouchInsideSettingsDragIcon(event);
        case MotionEvent.ACTION_MOVE:
        	if (!mTouched) return false;
        	
        	mDragIconX = event.getX() - mDragIconRadius;
        	mDragIconY = event.getY() - mDragIconRadius;
        	        		
        	invalidate();
        	break;
        case MotionEvent.ACTION_UP:
        	if (!mTouched) return false;
        	
        	mTouched = false;
        	resetIcons();
        	
        	if (isTouchInsideToolboxTargetIcon(event))
        	{
        		if (mListener != null) mListener.onToolboxIconTouched();
        	}
        	else if (isTouchInsideSettingsTargetIcon(event))
        	{
        		if (mListener != null) mListener.onSettingsIconTouched();
        	}
        	break;
		}
		
		return true;
	}

	private boolean isTouchInsideSettingsDragIcon(MotionEvent event) {
		final float touchX = event.getX();
		final float touchY = event.getY();
		
		//Log.d(TAG, "Touch @ "+touchX+","+touchY+" icon at "+mDragIconX+","+mDragIconY+" with radius "+mDragIconRadius);
		

		if (	(touchX >= mDragIconX) && (touchX < (mDragIconX + mDragIconRadius*2)) &&
				(touchY >= mDragIconY) && (touchY < (mDragIconY + mDragIconRadius*2)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean isTouchInsideToolboxTargetIcon(MotionEvent event)
	{
		final float touchX = event.getX();
		final float touchY = event.getY();
		
		//Log.d(TAG, "Touch @ "+touchX+","+touchY+" icon at "+mDragIconX+","+mDragIconY+" with radius "+mDragIconRadius);
		
		if (	(touchX >= mTargetToolsIconX) && (touchX < (mTargetToolsIconX + mTargetToolsIconRadius*2)) &&
				(touchY >= mTargetToolsIconY) && (touchY < (mTargetToolsIconY + mTargetToolsIconRadius*2)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean isTouchInsideSettingsTargetIcon(MotionEvent event)
	{
		final float touchX = event.getX();
		final float touchY = event.getY();
		
		//Log.d(TAG, "Touch @ "+touchX+","+touchY+" icon at "+mDragIconX+","+mDragIconY+" with radius "+mDragIconRadius);
		if (	(touchX >= mTargetSettingsIconX) && (touchX < (mTargetSettingsIconX + mTargetSettingsIconDiameter)) &&
				(touchY >= mTargetSettingsIconY) && (touchY < (mTargetSettingsIconY + mTargetSettingsIconDiameter)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}