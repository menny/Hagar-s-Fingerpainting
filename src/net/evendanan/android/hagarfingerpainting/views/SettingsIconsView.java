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
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SettingsIconsView extends View {
	private static final String TAG = "SettingsIconsView";
    
    private final DisplayMetrics mDisplayMetrics;

    private int mWidth;
    private int mHeight;
    private float mDragIconX;
    private float mDragIconY;
    private final Bitmap mDragIcon;
    private final float mDragIconRadius;
    
    private final Paint mPaint;

	private boolean mTouched;
    
	public SettingsIconsView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(1);
        mDisplayMetrics = c.getResources().getDisplayMetrics();
        mHeight = mDisplayMetrics.heightPixels;
        
        mDragIcon = BitmapFactory.decodeResource(c.getResources(), android.R.drawable.ic_menu_preferences);
        mDragIconRadius = mDragIcon.getWidth()/2;
        
        resetIcons();
    }

    private void resetIcons() {
    	mTouched = false;
    	
    	mWidth = mDragIcon.getWidth();
    	
    	mDragIconX = 0;
    	mDragIconY = 0;
    	
    	invalidate();
	}

	@Override
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.translate(0, 0);
		
		mPaint.setColor(mTouched? 0x900505A0 : 0x300505A0);
		canvas.drawCircle(mDragIconX + mDragIconRadius, mDragIconY + mDragIconRadius, mDragIconRadius, mPaint);
		canvas.drawBitmap(mDragIcon, mDragIconX, mDragIconY, mPaint);
    }
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	setMeasuredDimension(mWidth, mHeight);
    }
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	mTouched = true;
        case MotionEvent.ACTION_MOVE:
        	mDragIconX = event.getX() - mDragIconRadius;
        	mDragIconY = event.getY() - mDragIconRadius;
        	
        	int newWidth = (int) Math.max(mDragIcon.getWidth(), event.getX() + mDragIconRadius);
        	if (newWidth != mWidth)
        	{
        		//resize this view is required
        	}
        	mWidth = newWidth;
        	
        	invalidate();
        	break;
        case MotionEvent.ACTION_UP:
        	mTouched = false;
        	resetIcons();
        	break;
		}
		
		return true;
	}
    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float angle = (float)java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle/(2*PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    mCenterPaint.setColor(interpColor(mColors, unit));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (inCenter) {
                        mListener.colorChanged(mCenterPaint.getColor(), -1);
                    }
                    mTrackingCenter = false;    // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }
    */
}