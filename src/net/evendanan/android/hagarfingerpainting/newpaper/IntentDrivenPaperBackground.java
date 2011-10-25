package net.evendanan.android.hagarfingerpainting.newpaper;

import android.content.Intent;

public interface IntentDrivenPaperBackground extends PaperBackground {
	Intent getIntentToStartForResult();
	String getActionTitle();
	int getRequestCode();
	
	void onActivityResult(int requestCode, int resultCode, Intent data);
}
