package net.evendanan.android.hagarfingerpainting.newpaper;

import android.content.Context;
import android.content.Intent;

public interface IntentDrivenPaperBackground extends PaperBackground {
	Intent getIntentToStartForResult(Context appContext);
	String getActionTitle(Context appContext);
	
	void onActivityResult(Context appContext, Intent data);
}
