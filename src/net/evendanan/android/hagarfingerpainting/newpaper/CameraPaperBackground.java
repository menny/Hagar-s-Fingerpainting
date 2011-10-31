package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

public class CameraPaperBackground implements IntentDrivenPaperBackground, PaperBackgroundState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1967981913885081398L;

	private static final String TAG = "CameraPaperBackground";
	
	private Drawable mLoadedCameraImage = null;
	
	public CameraPaperBackground(){
	}
	
	@Override
	public Drawable getIcon(Context appContext) {
		return appContext.getResources().getDrawable(R.drawable.paper_camera);
	}

	@Override
	public Drawable getBackgroundDrawable(Context appContext) {
		return mLoadedCameraImage;
	}

	@Override
	public Intent getIntentToStartForResult(Context appContext) {
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		
		return intent;
	}
	
	@Override
	public String getActionTitle(Context appContext) {
		return appContext.getString(R.string.take_photo_as_paper);
	}
	
	@Override
	public void onActivityResult(Context appContext, Intent data) {
		Bitmap bm = (Bitmap) data.getExtras().get("data");
		
		if (bm == null)
		{
			Log.d(TAG, "No image received from camera.");
			return;
		}
		final int width = bm.getWidth();
		final int height = bm.getHeight();
		
		DisplayMetrics dm = appContext.getResources().getDisplayMetrics();
        final int displayWidth = dm.widthPixels;
        final int displayHeight = dm.heightPixels;
        
		Log.d(TAG, "Image taken from camera width "+width+" height "+height+" display width "+displayWidth+" display height "+displayHeight);
		if (height > width)
		{
			Matrix rotator = new Matrix();
			rotator.postRotate(90);
			bm = Bitmap.createBitmap(bm, 0, 0, width, height, rotator, true);
		}
        
		mLoadedCameraImage = new BitmapDrawable(bm);
	}
	
	@Override
	public void clearInnerState() {
		mLoadedCameraImage = null;
	}
}
