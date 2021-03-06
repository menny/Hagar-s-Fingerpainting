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

import net.evendanan.android.hagarfingerpainting.newpaper.IntentDrivenPaperBackground;
import net.evendanan.android.hagarfingerpainting.newpaper.PaperBackground;
import net.evendanan.android.hagarfingerpainting.newpaper.PaperColorListAdapter;
import net.evendanan.android.hagarfingerpainting.views.ColorPickerDialog;
import net.evendanan.android.hagarfingerpainting.views.SettingsIconsView;
import net.evendanan.android.hagarfingerpainting.views.SettingsIconsView.SettingsIconsTouchedListener;
import net.evendanan.android.hagarfingerpainting.views.Whiteboard;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdView;

public class HagarFingerpaintingActivity extends FragmentActivity implements OnSharedPreferenceChangeListener, SettingsIconsTouchedListener {

    public static final String TAG = "HagarFingerpaintingActivity";

    private static final int DIALOG_NEW_PAPER = 12398;

	private static final int PAPER_INTENT_REQUEST = 58721;

	private static final String PAPER_INTENT_OBJECT_KEY = "PAPER_INTENT_OBJECT_KEY";
    
	private Whiteboard mWhiteboard;
	private ImageView mBackground;
	private AdView mAdView;
	private MaskFilter  mBlur;
	private boolean mBlurFilterApplied = false;
	private boolean mEraseMode = false;
	private TextView mPainterName;
	
	private ViewGroup mToolbox;
	private ImageView mEraserToolBoxIcon;

	private SettingsIconsView mSettingsIcons;
	
	private IntentDrivenPaperBackground mBackgroundPaper;
	private boolean mPaperCreated = false;

	private Animation mInAnimation;

	private Animation mOutAnimation;
    
	/*
	private static final int COLOR_MENU_ID = Menu.FIRST;
    //private static final int BLUR_MENU_ID = Menu.FIRST + 1;
    private static final int ERASE_MENU_ID = Menu.FIRST + 2;
    private static final int SAVE_MENU_ID = Menu.FIRST + 3;
    private static final int SHARE_MENU_ID = Menu.FIRST + 4;
    private static final int NEW_PAPER_MENU_ID = Menu.FIRST + 5;
    //private static final int SETTINGS_MENU_ID = Menu.FIRST + 6;
*/
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		setContentView(R.layout.main);
        mWhiteboard = (Whiteboard)findViewById(R.id.whiteboard);
        mPainterName = (TextView)findViewById(R.id.painter_name_text);
        mAdView = (AdView)findViewById(R.id.adView);
        mBackground = (ImageView)findViewById(R.id.background_image);
        mSettingsIcons = (SettingsIconsView)findViewById(R.id.settings_icons);
        mSettingsIcons.setOnSettingsIconsTouchedListener(this);
        mToolbox = (ViewGroup)findViewById(R.id.toolbox);
        
        for(View innerView : mToolbox.getTouchables())
        {
        	if (innerView instanceof ImageView)
        	{
        		if (innerView.getId() == R.id.toolbox_eraser)
        		{
        			mEraserToolBoxIcon = (ImageView) innerView;
        		}
        		
        		innerView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onToolboxClicked(v.getId());
					}
				});
        	}
        }
        
        mInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
		mOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_out_right);
		mOutAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mToolbox.setVisibility(View.GONE);
				mSettingsIcons.setVisibility(View.VISIBLE);
			}
		});
		
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        
        mBackgroundPaper = savedInstanceState == null? null : (IntentDrivenPaperBackground)savedInstanceState.getSerializable(PAPER_INTENT_OBJECT_KEY);
        mPaperCreated = false;
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/schoolbell.ttf");
        mPainterName.setTypeface(tf);
        mBlur = new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL);
        
        readyPaperTools();
    }

	protected void onToolboxClicked(int id) {
		switch(id)
		{
		case R.id.toolbox_close:
			mToolbox.startAnimation(mOutAnimation);
			break;
		case R.id.toolbox_color:
			new ColorPickerDialog(this, mWhiteboard, 0).show();
			break;
		case R.id.toolbox_eraser:
			mEraseMode = !mEraseMode;
			mEraserToolBoxIcon.setBackgroundColor(mEraseMode? Color.BLACK : Color.TRANSPARENT);
        	mWhiteboard.setEraserMode(mEraseMode);
        	break;
		case R.id.toolbox_save:
			takeScreenshot(true);
			break;
		case R.id.toolbox_share:
			File screenshotPath = takeScreenshot(false);
            Intent intent = new Intent();
    		intent.setAction(Intent.ACTION_SEND);
    		intent.setType("image/png");
    		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title_template, getPainterName()));
    		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_template, getPainterName()));
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenshotPath));
			try 
			{
				startActivity(Intent.createChooser(intent, getString(R.string.toolbox_share_title)));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this.getApplicationContext(), R.string.no_way_to_share, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.toolbox_new_paper:
			onNewPaperRequested();
			break;
		}
	}

	@Override
    protected void onResume() {
    	super.onResume();
    	
    	String painterName = getPainterName();
    	setTitle(getString(R.string.app_title, painterName));
    	
    	if (!mPaperCreated)
    	{
    		onNewPaperRequested();
    	}
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(PAPER_INTENT_OBJECT_KEY, mBackgroundPaper);
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
			
			colors.setSelection(2);
			
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
					
					e.commit();
			    	
					PaperBackground paper = (PaperBackground)colors.getSelectedItem();
					
					newPaper.dismiss();
					
					createNewPaper(paper);
				}
			});
			View cancelButton = newPaper.findViewById(R.id.new_paper_cancel_button);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					newPaper.dismiss();
					if (!mPaperCreated)
						HagarFingerpaintingActivity.this.finish();
				}
			});
			newPaper.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (!mPaperCreated)
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
			//recreating the items (so they lose their inner state)
			final Gallery colors = (Gallery)dialog.findViewById(R.id.colors_list);
			((PaperColorListAdapter)colors.getAdapter()).clearItemsState();
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	void createNewPaper(PaperBackground paper) {
		readyPaperTools();
        
        if (paper instanceof IntentDrivenPaperBackground && ((IntentDrivenPaperBackground)paper).getBackgroundDrawable(getApplicationContext()) == null)
        {
        	IntentDrivenPaperBackground intentPaper = (IntentDrivenPaperBackground)paper;
        	mBackgroundPaper = intentPaper;
            Intent i = intentPaper.getIntentToStartForResult(getApplicationContext());
        	startActivityForResult(Intent.createChooser(i, intentPaper.getActionTitle(getApplicationContext())), PAPER_INTENT_REQUEST);
        }
        else
        {
        	Drawable d = paper.getBackgroundDrawable(getApplicationContext());
    		mBackground.setImageDrawable(d);
    		mWhiteboard.eraseEntireWhiteboard();
        	mPaperCreated = true;
        }
	}

	private void readyPaperTools() {
		mWhiteboard.setMaskFilter(mBlur);
        mBlurFilterApplied = true;
        mWhiteboard.setEraserMode(false);
        mEraseMode = false;
        mEraserToolBoxIcon.setBackgroundColor(mEraseMode? Color.BLACK : Color.TRANSPARENT);
        mWhiteboard.eraseEntireWhiteboard();
        
        mPainterName.setText(getPainterName());
        mAdView.setVisibility(getShowAds()? View.VISIBLE : View.GONE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean handled = false;
		if (resultCode == RESULT_OK) {
			if (mBackgroundPaper != null && requestCode == PAPER_INTENT_REQUEST) {
				mBackgroundPaper.onActivityResult(getApplicationContext(), data);
				PaperBackground paper = mBackgroundPaper;
				mBackgroundPaper = null;
				if (paper.getBackgroundDrawable(getApplicationContext()) != null)
				{
					handled = true;
					createNewPaper(paper);
				}
			}
		}
		
		if (!handled) onNewPaperRequested();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case BLUR_MENU_ID:
                if (!mBlurFilterApplied) {
                    mWhiteboard.setMaskFilter(mBlur);
                } else {
                    mWhiteboard.setMaskFilter(null);                    
                }
                mBlurFilterApplied = !mBlurFilterApplied;
                return true;*/
        }
        return super.onOptionsItemSelected(item);
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
    
	private File takeScreenshot(boolean showToast) {
		View v = getWindow().getDecorView();
		final int originalAdsVisibility = mAdView.getVisibility();
		mAdView.setVisibility(View.INVISIBLE);
		mSettingsIcons.setVisibility(View.INVISIBLE);
		
		v.setDrawingCacheEnabled(true);
		Bitmap cachedBitmap = v.getDrawingCache();
		Bitmap copyBitmap = cachedBitmap.copy(Bitmap.Config.RGB_565, true);
		FileOutputStream output = null;
		File file = null;
		try
		{
			File path = Places.getScreenshotFolder();
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
			
			mAdView.setVisibility(originalAdsVisibility);
			mSettingsIcons.setVisibility(View.VISIBLE);
		}

		if (file != null)
		{
			if (showToast) Toast.makeText(getApplicationContext(), "Save fingerpainting to: "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			//sending a broadcast to the media scanner so it will scan the new screenshot.
			Intent requestScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			requestScan.setData(Uri.fromFile(file));
			sendBroadcast(requestScan); 
			
			return file;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onToolboxIconTouched() {
		mToolbox.setVisibility(View.VISIBLE);
		mToolbox.startAnimation(mInAnimation);
		mSettingsIcons.setVisibility(View.GONE);
	}

	@Override
	public void onSettingsIconTouched() {
		startActivity(new Intent(getApplicationContext(), FingerpaintSettingsActivity.class));
	}
}
