package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;

public class SimplePaperBackground implements PaperBackground {

	private final Drawable mIcon;
	private final Drawable mBackground;
	
	public SimplePaperBackground(Context appContext, int color, int backgroundResId)
	{
		Drawable d = appContext.getResources().getDrawable(R.drawable.paper);
		d.mutate();
		d.setColorFilter(color, Mode.LIGHTEN);
		mIcon = d;
		
		mBackground = appContext.getResources().getDrawable(backgroundResId);
	}
	
	@Override
	public Drawable getIcon() {
		return mIcon;
	}

	@Override
	public Drawable getBackgroundDrawable() {
		return mBackground;
	}
	
}
