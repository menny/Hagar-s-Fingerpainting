package net.evendanan.android.hagarfingerpainting.newpaper;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public interface PaperBackground extends Serializable {
	public Drawable getIcon();
	public Drawable getBackgroundDrawable();
}
