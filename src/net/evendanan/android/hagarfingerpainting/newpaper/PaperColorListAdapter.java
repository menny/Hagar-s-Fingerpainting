package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PaperColorListAdapter extends android.widget.BaseAdapter {

	private final PaperBackground[] mPapers;
	
	private final Context mAppContext;
	
	public PaperColorListAdapter(Context c)
	{
		mAppContext = c;
		
		mPapers = new PaperBackground[]
		                              {
				new SimplePaperBackground(c, Color.WHITE, R.drawable.new_blank_paper_white),
				new SimplePaperBackground(c, Color.DKGRAY, R.drawable.new_blank_paper_black),
				new SimplePaperBackground(c, Color.RED, R.drawable.new_blank_paper_red),
				new SimplePaperBackground(c, Color.GREEN, R.drawable.new_blank_paper_green),
				new SimplePaperBackground(c, Color.BLUE, R.drawable.new_blank_paper_blue)
		                              };
	}
	
	@Override
	public int getCount() {
		return mPapers.length;
	}

	@Override
	public Object getItem(int position) {
		return mPapers[position];
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
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return getCount();
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i = new ImageView(mAppContext);
		i.setPadding(3, 3, 3, 3);
		PaperBackground paper = (PaperBackground)getItem(position);
		i.setImageDrawable(paper.getIcon());
		i.setScaleType(ScaleType.CENTER_INSIDE);
		
		return i;
	}
	
	
}