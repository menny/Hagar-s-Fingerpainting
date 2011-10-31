package net.evendanan.android.hagarfingerpainting.newpaper;

import net.evendanan.android.hagarfingerpainting.R;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PaperColorListAdapter extends android.widget.BaseAdapter {

	private final PaperBackground[] mPapers;
	
	private final LayoutInflater mInflator;

	private final Context mAppContext;
	
	public PaperColorListAdapter(Context c)
	{
		mAppContext = c;
		mInflator = (LayoutInflater)c.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		
		mPapers = new PaperBackground[]
		                              {
				new CameraPaperBackground(),
				new GalleryPaperBackground(),
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
		ViewGroup v = (ViewGroup)mInflator.inflate(R.layout.paper_icon_view, null);
		ImageView i = (ImageView)v.findViewById(R.id.paper_icon);
		PaperBackground paper = (PaperBackground)getItem(position);
		i.setImageDrawable(paper.getIcon(mAppContext));
		
		return v;
	}

	public void clearItemsState() {
		for(PaperBackground p : mPapers)
		{
			if (p instanceof PaperBackgroundState)
			{
				((PaperBackgroundState)p).clearInnerState();
			}
		}
	}
	
	
}
