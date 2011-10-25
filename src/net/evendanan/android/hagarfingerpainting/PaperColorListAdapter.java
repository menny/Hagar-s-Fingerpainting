package net.evendanan.android.hagarfingerpainting;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PaperColorListAdapter extends android.widget.BaseAdapter {

	private static final int[] msColors = new int[]
	                                                  {
		Color.WHITE,
		0xff202020,
		Color.GRAY,
		Color.RED,
	    Color.GREEN,
		Color.BLUE,
		Color.YELLOW
		                                              };
	
	public static int getColorAtPosition(int position)
	{
		return msColors[position-2];
	}
	
	private final Context mAppContext;
	
	public PaperColorListAdapter(Context c)
	{
		mAppContext = c;
	}
	
	@Override
	public int getCount() {
		return msColors.length;
	}

	@Override
	public Object getItem(int position) {
		return new Integer(msColors[position]);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	@Override
	public int getItemViewType(int position) {
		return 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i = new ImageView(mAppContext);
		i.setPadding(3, 3, 3, 3);
		Drawable d = mAppContext.getResources().getDrawable(R.drawable.paper);
		d.mutate();
		i.setImageDrawable(d);
		i.setScaleType(ScaleType.CENTER_INSIDE);
		i.setAdjustViewBounds(true);
		int color = ((Integer)getItem(position)).intValue();
		LightingColorFilter filter = new LightingColorFilter(color, 0xFF000000);
		i.setColorFilter(filter);
		
		return i;
	}
	
	
}
