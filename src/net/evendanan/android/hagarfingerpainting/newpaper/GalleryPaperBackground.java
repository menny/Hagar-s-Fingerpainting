package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

public class GalleryPaperBackground implements IntentDrivenPaperBackground {

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != SELECT_IMAGE_REQUEST_CODE) return;
		
		Uri selectedImageUri = data.getData();
		String selectedImagePath = getPath(selectedImageUri);
		
		mLoadedGalleryImage = TextUtils.isEmpty(selectedImagePath)? null : Drawable.createFromPath(selectedImagePath);
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
