package net.evendanan.android.hagarfingerpainting.newpaper;

import java.io.Serializable;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface PaperBackground extends Serializable {
	public Drawable getIcon(Context appContext);
	public Drawable getBackgroundDrawable(Context appContext);
}
