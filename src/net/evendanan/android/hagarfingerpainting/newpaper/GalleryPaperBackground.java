package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

public class GalleryPaperBackground implements IntentDrivenPaperBackground {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4547980727028557696L;

	private static final String TAG = "GalleryPaperBackground";
	
	private Drawable mLoadedGalleryImage = null;
	
	public GalleryPaperBackground()
	{
	}
	
	@Override
	public Drawable getIcon(Context appContext) {
		return appContext.getResources().getDrawable(R.drawable.paper_gallery);
	}

	@Override
	public Drawable getBackgroundDrawable(Context appContext) {
		return mLoadedGalleryImage;
	}

	@Override
	public Intent getIntentToStartForResult(Context appContext) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		
		return intent;
	}
	
	@Override
	public String getActionTitle(Context appContext) {
		return appContext.getString(R.string.pick_image_from_gallery);
	}
	
	@Override
	public void onActivityResult(Context appContext, Intent data) {		
		Uri selectedImageUri = data.getData();
		String selectedImagePath = getPath(appContext, selectedImageUri);
		
		Bitmap background = TextUtils.isEmpty(selectedImagePath)? null : BitmapFactory.decodeFile(selectedImagePath);
		//setting the correct ratio
		if (background != null)
		{
			final int width = background.getWidth();
			final int height = background.getHeight();
			
			DisplayMetrics dm = appContext.getResources().getDisplayMetrics();
	        final int displayWidth = dm.widthPixels;
	        final int displayHeight = dm.heightPixels;
	        
			Log.d(TAG, "Image selected '"+selectedImagePath+"' width "+width+" height "+height+" display width "+displayWidth+" display height "+displayHeight);
			if (height > width)
			{
				Matrix rotator = new Matrix();
				rotator.postRotate(90);
				background = Bitmap.createBitmap(background, 0, 0, width, height, rotator, true);
			}
	        
			mLoadedGalleryImage = new BitmapDrawable(background);
		}
		else
		{
			mLoadedGalleryImage = null;
		}
	}
	
	private String getPath(Context appContext, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = appContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
        	try
        	{
	        	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        	cursor.moveToFirst();
	        	return cursor.getString(column_index);
        	}
        	finally
        	{
        		cursor.close();
        	}
        }
        return null;
    }

}
