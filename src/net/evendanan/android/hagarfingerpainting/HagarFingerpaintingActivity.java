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

import com.google.ads.AdView;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

public class HagarFingerpaintingActivity extends Activity implements OnSharedPreferenceChangeListener {

    public static final String TAG = "HagarFingerpaintingActivity";

    private static final int DIALOG_NEW_PAPER = 12398;
    
	private Whiteboard mWhiteboard;
	private AdView mAdView;
	private boolean mBlurFilterApplied = false;
	private boolean mEraseMode = false;
	private TextView mPainterName;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
    	
    	onNewPaperRequested();
    }

	void onNewPaperRequested() {
		showDialog(DIALOG_NEW_PAPER);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case DIALOG_NEW_PAPER:
			final Dialog newPaper = new Dialog(this);
			newPaper.setTitle(R.string.new_paper_dialog_title);
			newPaper.setContentView(R.layout.new_paper_dialog);
			newPaper.setCancelable(true);
			final EditText painterName = (EditText)newPaper.findViewById(R.id.painter_name_input_text);
			Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/schoolbell.ttf");
	        painterName.setTypeface(tf);
	        
	        final Gallery colors = (Gallery)newPaper.findViewById(R.id.colors_list);
			colors.setAdapter(new PaperColorListAdapter(getApplicationContext()));
			colors.setOnItemSelectedListener(new OnItemSelectedListener() {
				private View mSelectedItem = null;
		    	@Override
		    	public void onItemSelected(AdapterView<?> adapter, View v,
		    			int position, long id) {
		    		if (mSelectedItem != null)
	    				mSelectedItem.setBackgroundDrawable(null);
		    		
		    		mSelectedItem = v;
	    			if (mSelectedItem != null)
	    				mSelectedItem.setBackgroundResource(R.drawable.selected_color_background);
		    	}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					if (mSelectedItem != null)
	    				mSelectedItem.setBackgroundDrawable(null);
				}
		    });
			
			View createButton = newPaper.findViewById(R.id.new_paper_create_button);
			createButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					Editor e = sp.edit();
					
					final CharSequence painterNameFromUI = painterName.getText().toString();
					final String newPainterName = TextUtils.isEmpty(painterNameFromUI)?
							getString(R.string.settings_key_painter_name_default_value) : painterNameFromUI.toString();
					e.putString(getString(R.string.settings_key_painter_name), newPainterName);
					
					int selectedColor = colors.getSelectedItemPosition() == 0? Color.BLACK : ((Integer)colors.getSelectedItem()).intValue();
					e.putInt(getString(R.string.settings_key_paper_color), selectedColor);
					
					e.commit();
			    	
					newPaper.dismiss();
					
					createNewPaper();
				}
			});
			View cancelButton = newPaper.findViewById(R.id.new_paper_cancel_button);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					newPaper.dismiss();
					if (mWhiteboard == null)
						HagarFingerpaintingActivity.this.finish();
				}
			});
			newPaper.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (mWhiteboard == null)
						HagarFingerpaintingActivity.this.finish();
				}
			});
			
			
			return newPaper;
		default:
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id)
		{
		case DIALOG_NEW_PAPER:
			EditText painterName = (EditText)dialog.findViewById(R.id.painter_name_input_text);
			painterName.setText(getPainterName());
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	void createNewPaper() {
		setContentView(R.layout.main);
        mWhiteboard = (Whiteboard)findViewById(R.id.whiteboard);
        mPainterName = (TextView)findViewById(R.id.painter_name_text);
        mAdView = (AdView)findViewById(R.id.adView);
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/schoolbell.ttf");
        mPainterName.setTypeface(tf);
        mBlur = new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL);
        
        mWhiteboard.setMaskFilter(mBlur);
        mBlurFilterApplied = true;
        mWhiteboard.setEraserMode(false);
        mWhiteboard.setBackgroundColor(getPaperColor());
        mEraseMode = false;
        mPainterName.setText(getPainterName());
        mAdView.setVisibility(getShowAds()? View.VISIBLE : View.GONE);
	}

	private MaskFilter  mBlur;
    
	private static final int COLOR_MENU_ID = Menu.FIRST;
    //private static final int BLUR_MENU_ID = Menu.FIRST + 1;
    private static final int ERASE_MENU_ID = Menu.FIRST + 2;
    private static final int SAVE_MENU_ID = Menu.FIRST + 3;
    private static final int SHARE_MENU_ID = Menu.FIRST + 4;
    private static final int NEW_PAPER_MENU_ID = Menu.FIRST + 5;
    private static final int SETTINGS_MENU_ID = Menu.FIRST + 6;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, R.string.menu_color_title);
        //menu.add(0, BLUR_MENU_ID, 0, R.string.menu_blur_title);
        menu.add(0, ERASE_MENU_ID, 0, R.string.menu_eraser_title);
        menu.add(0, NEW_PAPER_MENU_ID, 0, R.string.menu_new_title).setIcon(R.drawable.paper_menu);
        menu.add(0, SAVE_MENU_ID, 0, R.string.menu_save_title).setIcon(R.drawable.save_menu);
        menu.add(0, SHARE_MENU_ID, 0, R.string.menu_share_title).setIcon(R.drawable.share_menu);
        menu.add(0, SETTINGS_MENU_ID, 0, R.string.menu_settings_title).setIcon(android.R.drawable.ic_menu_preferences);
        
        return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	String painterName = getPainterName();
    	setTitle(getString(R.string.app_title, painterName));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    		String key) {
    	if (key.equals(getString(R.string.settings_key_painter_name)))
    	{
    		if (mPainterName != null)
    			mPainterName.setText(getPainterName());
    	}
    	else if (key.equals(getString(R.string.settings_key_admob_enabled)))
    	{
    		if (mAdView != null)
    			mAdView.setVisibility(getShowAds()? View.VISIBLE : View.GONE);
    	}
    }
    
    boolean getShowAds()
    {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean admobEnabled = sp.getBoolean(getString(R.string.settings_key_admob_enabled), getResources().getBoolean(R.bool.settings_key_admob_enabled_default_value));
		return admobEnabled;
    }
    
	String getPainterName() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	String painterName = sp.getString(getString(R.string.settings_key_painter_name), getString(R.string.settings_key_painter_name_default_value));
		return painterName;
	}
    
	int getPaperColor()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	int paperColor = sp.getInt(getString(R.string.settings_key_paper_color), getResources().getInteger(R.integer.settings_key_paper_color_default_value));
		return paperColor;
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, mWhiteboard, 0).show();
                return true;
            /*case BLUR_MENU_ID:
                if (!mBlurFilterApplied) {
                    mWhiteboard.setMaskFilter(mBlur);
                } else {
                    mWhiteboard.setMaskFilter(null);                    
                }
                mBlurFilterApplied = !mBlurFilterApplied;
                return true;*/
            case ERASE_MENU_ID:
            	mEraseMode = !mEraseMode;
            	mWhiteboard.setEraserMode(mEraseMode);
                return true;
            case SAVE_MENU_ID:
                takeScreenshot(true);
                return true;
            case SHARE_MENU_ID:
                File screenshotPath = takeScreenshot(false);
                Intent intent = new Intent();
        		intent.setAction(Intent.ACTION_SEND);
        		intent.setType("image/png");
        		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title_template, getPainterName()));
        		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_template, getPainterName()));
    			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenshotPath));
    			try 
    			{
    				startActivity(Intent.createChooser(intent, getString(R.string.menu_share_title)));
    			} catch (android.content.ActivityNotFoundException ex) {
    				Toast.makeText(this.getApplicationContext(), R.string.no_way_to_share, Toast.LENGTH_LONG).show();
    			}
                return true;
            case NEW_PAPER_MENU_ID:
            	onNewPaperRequested();
            	return true;
            case SETTINGS_MENU_ID:
            	startActivity(new Intent(getApplicationContext(), FingerpaintSettingsActivity.class));
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private File takeScreenshot(boolean showToast) {
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
			file = null;
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
		}

		if (file != null)
		{
			if (showToast) Toast.makeText(getApplicationContext(), "Save fingerpainting to: "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			return file;
		}
		else
		{
			return null;
		}
	}
}
