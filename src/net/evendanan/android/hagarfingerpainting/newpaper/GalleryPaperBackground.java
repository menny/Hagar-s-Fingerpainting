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
import android.view.Window;

public class GalleryPaperBackground implements IntentDrivenPaperBackground {

	private static final String TAG = "GalleryPaperBackground";
	
	private static final int SELECT_IMAGE_REQUEST_CODE = 89123;
	private final Context mAppContext;
	private final Drawable mIcon;
	private Drawable mLoadedGalleryImage = null;
	
	public GalleryPaperBackground(Context context)
	{
		mAppContext = context;
		mIcon = context.getResources().getDrawable(R.drawable.paper_gallery);
	}
	
	@Override
	public Drawable getIcon() {
		return mIcon;
	}

	@Override
	public Drawable getBackgroundDrawable() {
		return mLoadedGalleryImage;
	}

	@Override
	public Intent getIntentToStartForResult() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		
		return intent;
	}
	
	@Override
	public String getActionTitle() {
		return mAppContext.getString(R.string.pick_image_from_gallery);
	}
	
	@Override
	public int getRequestCode() {
		return SELECT_IMAGE_REQUEST_CODE;
	}
	
	@Override
	public void onActivityResult(Intent data) {		
		Uri selectedImageUri = data.getData();
		String selectedImagePath = getPath(selectedImageUri);
		
		Bitmap background = TextUtils.isEmpty(selectedImagePath)? null : BitmapFactory.decodeFile(selectedImagePath);
		//setting the correct ratio
		if (background != null)
		{
			final int width = background.getWidth();
			final int height = background.getHeight();
			
			DisplayMetrics dm = mAppContext.getResources().getDisplayMetrics();
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
	
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = mAppContext.getContentResolver().query(uri, projection, null, null, null);
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
