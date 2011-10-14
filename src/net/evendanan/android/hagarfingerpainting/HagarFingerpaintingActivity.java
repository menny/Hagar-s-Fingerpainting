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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.BEVEL);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStrokeWidth(4);

        mBlur = new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL);
        //starting with blur
        mPaint.setMaskFilter(mBlur);
        
        setContentView(new Whiteboard(this, getApplicationContext()));
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	int[] mColors = new int[]{ 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF0FF000, 0xFF000FF0}; 
	
    Paint       mPaint;
    private MaskFilter  mBlur;
    
    public void colorChanged(int color, int index) {
    	mColors[index] = color;
    }

	private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int BLUR_MENU_ID = Menu.FIRST + 1;
    private static final int ERASE_MENU_ID = Menu.FIRST + 2;
    private static final int SAVE_MENU_ID = Menu.FIRST + 3;
    private static final int CLEAR_MENU_ID = Menu.FIRST + 4;
    private static final int SETTINGS_MENU_ID = Menu.FIRST + 5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color");
        menu.add(0, BLUR_MENU_ID, 0, "Blur");
        menu.add(0, ERASE_MENU_ID, 0, "Eraser");
        menu.add(0, CLEAR_MENU_ID, 0, "Clear").setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, SAVE_MENU_ID, 0, "Save").setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, SETTINGS_MENU_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        
        return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	String painterName = getPainterName();
    	setTitle(getString(R.string.app_title, painterName));
    }

	String getPainterName() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	String painterName = sp.getString(getString(R.string.settings_key_painter_name), getString(R.string.settings_key_painter_name_default_value));
		return painterName;
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
                new ColorPickerDialog(this, this, mPaint.getColor(), 0).show();
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                return true;
            case SAVE_MENU_ID:
                takeScreenshot();
                return true;
            case CLEAR_MENU_ID:
            	setContentView(new Whiteboard(this, this));
            	return true;
            case SETTINGS_MENU_ID:
            	startActivity(new Intent(getApplicationContext(), FingerpaintSettingsActivity.class));
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
			Calendar cal = Calendar.getInstance();
			
			file = new File(path, 
					getPainterName()+"_"+
					cal.get(Calendar.YEAR)+"_"+(1+cal.get(Calendar.MONTH))+"_"+cal.get(Calendar.DAY_OF_MONTH)+"_"+
					cal.get(Calendar.HOUR_OF_DAY)+"_"+cal.get(Calendar.MINUTE)+"_"+cal.get(Calendar.SECOND)+
					".png");
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
