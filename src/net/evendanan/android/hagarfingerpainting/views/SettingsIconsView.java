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

    private final int mWidth;
    private final int mHeight;
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
        mWidth = mDisplayMetrics.widthPixels;
        
        mDragIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.settings_drag_icon);
        mDragIconRadius = Math.max(mDragIcon.getWidth(), mDragIcon.getHeight()) /2;
        
        Log.d(TAG, "Drag icon radius: "+mDragIconRadius);
        
        resetIcons();
    }

    private void resetIcons() {
    	mTouched = false;
    	
    	mDragIconX = mWidth - (mDragIconRadius*1.5f);
    	mDragIconY = 0 - (mDragIconRadius*0.5f);
    	
    	invalidate();
	}

	@Override
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.translate(0, 0);
		
		mPaint.setColor(mTouched? 0x900505A0 : 0x300505A0);
		canvas.drawCircle(mDragIconX + mDragIconRadius, mDragIconY + mDragIconRadius, mDragIconRadius*1.25f, mPaint);
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
        	break;
		}
		
		return true;
	}

	private boolean isTouchInsideSettingsDragIcon(MotionEvent event) {
		final float touchX = event.getX();
		final float touchY = event.getY();
		
		Log.d(TAG, "Touch @ "+touchX+","+touchY+" icon at "+mDragIconX+","+mDragIconY+" with radius "+mDragIconRadius);
		
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
}