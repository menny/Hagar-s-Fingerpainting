/*
 * Copyright (C) 2007 The Android Open Source Project
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

package net.evendanan.android.hagarfingerpainting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class HagarFingerpaintingActivity extends Activity
        implements ColorPickerDialog.OnColorChangedListener {

    public static final String TAG = "HagarFingerpaintingActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(new MyView(this));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);

        mBlur = new BlurMaskFilter(4, BlurMaskFilter.Blur.NORMAL);
        //starting with blur
        mPaint.setMaskFilter(mBlur);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	private int[] mColors = new int[]{ 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF0FF000, 0xFF000FF0}; 
	
    private Paint       mPaint;
    private MaskFilter  mBlur;
    
    public void colorChanged(int color, int index) {
    	mColors[index] = color;
    }

	private static class PathDrawing
	{
		final Path path = new Path();
		final int pointerIndex;
        float mX, mY;
        
        PathDrawing(int index)
        {
        	pointerIndex = index;
        }
	}

    public class MyView extends View {
    	
        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;

        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Map<Integer, PathDrawing>  mPaths;
        private Paint   mBitmapPaint;

        public MyView(Context c) {
            super(c);

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
            		int color = mColors[path.pointerIndex % mColors.length];
                    mPaint.setColor(color);
            		canvas.drawPath(path.path, mPaint);
            	}
            }
        }

		private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y, int pointerIndex) {
        	PathDrawing path = getPath(pointerIndex);
        	path.path.reset();
        	path.path.moveTo(x, y);
        	path.mX = x;
        	path.mY = y;
        }

		private PathDrawing getPath(int pointerIndex) {
			PathDrawing path = mPaths.get(pointerIndex);
        	if (path == null)
        	{
        		path = new PathDrawing(pointerIndex);
        		mPaths.put(pointerIndex, path);
        	}
			return path;
		}
        private void touch_move(float x, float y, int pointerIndex) {
        	PathDrawing path = getPath(pointerIndex);
            float dx = Math.abs(x - path.mX);
            float dy = Math.abs(y - path.mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            	
            	path.path.quadTo(path.mX, path.mY, (x + path.mX)/2, (y + path.mY)/2);
            	path.mX = x;
            	path.mY = y;
            }
        }
        private void touch_up(int pointerIndex) {
        	PathDrawing path = getPath(pointerIndex);
        	path.path.lineTo(path.mX, path.mY);
            // commit the path to our offscreen
            mCanvas.drawPath(path.path, mPaint);
            // kill this so we don't double draw
            path.path.reset();
            mPaths.remove(pointerIndex);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
        	float x = event.getX(event.getActionIndex());
            float y = event.getY(event.getActionIndex());
            
        	switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            	touch_start(x, y, event.getActionIndex());
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
            	for(int pointerIndex=0; pointerIndex<event.getPointerCount(); pointerIndex++)
            	{
            		float pointerX = event.getX(pointerIndex);
                    float pointerY = event.getY(pointerIndex);
                    touch_move(pointerX, pointerY, pointerIndex);
                    invalidate();
            	}
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                touch_up(event.getActionIndex());
                invalidate();
                break;
        	}
            
            return true;
        }
    }

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int BLUR_MENU_ID = Menu.FIRST + 1;
    private static final int ERASE_MENU_ID = Menu.FIRST + 2;
    private static final int SAVE_MENU_ID = Menu.FIRST + 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color");
        menu.add(0, BLUR_MENU_ID, 0, "Blur");
        menu.add(0, ERASE_MENU_ID, 0, "Erase");
        menu.add(0, SAVE_MENU_ID, 0, "Save");

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                        PorterDuff.Mode.CLEAR));
                return true;
            case SAVE_MENU_ID:
                takeScreenshot();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void takeScreenshot() {
		View v = getWindow().getDecorView();
		v.setDrawingCacheEnabled(true);
		Bitmap cachedBitmap = v.getDrawingCache();
		Bitmap copyBitmap = cachedBitmap.copy(Bitmap.Config.RGB_565, true);
		FileOutputStream output = null;
		File file = null;
		try
		{
			File path = new File(Environment.getExternalStorageDirectory(), "/Fingerpainting/");
			path.mkdirs();
			file = new File(path, ""+System.currentTimeMillis()+".png");
			output = new FileOutputStream(file);
			copyBitmap.compress(CompressFormat.PNG, 100, output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally
		{
			if (output != null)
			{
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (file != null)
			{
				Toast.makeText(getApplicationContext(), "Save fingerpainting to: "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
