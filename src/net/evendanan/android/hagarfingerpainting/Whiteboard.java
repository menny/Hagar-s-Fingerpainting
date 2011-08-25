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
package net.evendanan.android.hagarfingerpainting;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class Whiteboard extends View {
	
    /**
	 * 
	 */
	private final HagarFingerpaintingActivity mFingerpaintingActivity;

    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Map<Integer, PathDrawing>  mPaths;
    private Stack<Integer> mColorsStack = new Stack<Integer>();
    private Paint   mBitmapPaint;

    public Whiteboard(HagarFingerpaintingActivity hagarFingerpaintingActivity, Context c) {
        super(c);
		mFingerpaintingActivity = hagarFingerpaintingActivity;

        mPaths = new HashMap<Integer, PathDrawing>();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFAAAAAA);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        for(PathDrawing path : mPaths.values())
        {
        	if (path != null)
        	{
        		mFingerpaintingActivity.mPaint.setColor(path.pointerColor);
        		canvas.drawPath(path.path, mFingerpaintingActivity.mPaint);
        	}
        }
    }

	private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y, int pointerId) {
    	PathDrawing path = getPath(pointerId);
    	path.path.reset();
    	path.path.moveTo(x, y);
    	path.mX = x;
    	path.mY = y;
    }

	private PathDrawing getPath(int pointerId) {
		PathDrawing path = mPaths.get(pointerId);
    	if (path == null)
    	{
    		path = new PathDrawing(popColorForPointer());
    		mPaths.put(pointerId, path);
    	}
		return path;
	}
	
    private int popColorForPointer() {
		if (mColorsStack.isEmpty())
		{
			for(int i=mFingerpaintingActivity.mColors.length-1; i>=0; i--)
				mColorsStack.push(mFingerpaintingActivity.mColors[i]);
			
			return popColorForPointer();
		}
		
		Integer color = mColorsStack.pop();
		
		return color.intValue();
	}
    
    private void pushColorForPointer(int pointerColor)
    {
    	mColorsStack.push(pointerColor);
    }

	private boolean touch_move(float x, float y, int pointerId) {
    	PathDrawing path = getPath(pointerId);
        float dx = Math.abs(x - path.mX);
        float dy = Math.abs(y - path.mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        	
        	path.path.quadTo(path.mX, path.mY, (x + path.mX)/2, (y + path.mY)/2);
        	path.mX = x;
        	path.mY = y;
        	return true;
        }
        else
        {
        	return false;
        }
    }
    private void touch_up(int pointerId) {
    	PathDrawing path = getPath(pointerId);
    	path.path.lineTo(path.mX, path.mY);
        // commit the path to our offscreen
    	mFingerpaintingActivity.mPaint.setColor(path.pointerColor);
        mCanvas.drawPath(path.path, mFingerpaintingActivity.mPaint);
        // kill this so we don't double draw
        path.path.reset();
        mPaths.remove(pointerId);
        pushColorForPointer(path.pointerColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
        	touch_start(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()), event.getPointerId(event.getActionIndex()));
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
        	boolean invalidateRequired = false;
        	for(int pointerIndex=0; pointerIndex<event.getPointerCount(); pointerIndex++)
        	{
        		final int pointerId = event.getPointerId(pointerIndex);
        		float pointerX = event.getX(pointerIndex);
                float pointerY = event.getY(pointerIndex);
                
                invalidateRequired |= touch_move(pointerX, pointerY, pointerId);
        	}
        	
        	if (invalidateRequired)
        		invalidate();
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            touch_up(event.getPointerId(event.getActionIndex()));
            invalidate();
            if (event.getPointerCount() == 1)
            {
            	//Now that there are no fingers down, I can clear the stack, and reset the colors sequence. 
            	mColorsStack.clear();
            }
            break;
    	}
        
        return true;
    }
}