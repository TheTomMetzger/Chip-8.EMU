/**
 * Created by Tom on 4/16/17.
 */

package com.tommetzger.chip_8emu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import java.util.List;





public class ROMListAdapter extends BaseAdapter
{
	
	Context context;
	List<String> data;
	private static LayoutInflater inflater = null;
	SharedPreferences preferences;
	
	
	
	
	public ROMListAdapter(Context context, List<String> data)
	{
		this.context = context;
		this.data = data;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	
	
	
	@Override
	public int getCount()
	{
		return data.size();
	}
	
	
	
	
	@Override
	public Object getItem(int position)
	{
		return data.get(position);
	}
	
	
	
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	
	
	
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		View rowItem = view;
		
		if (rowItem == null)
		{
			rowItem = inflater.inflate(R.layout.row, null);
		}
		
		String romName = data.get(position);
		
		TextView romLabel = (TextView) rowItem.findViewById(R.id.romLabel);
		romLabel.setText(romName);
		
		RatingBar romRating = (RatingBar) rowItem.findViewById(R.id.romRating);
		romRating.setNumStars(preferences.getInt(romName+"Rating", 0));
		
		
		return rowItem;
	}
}
