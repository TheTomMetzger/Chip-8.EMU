package com.tommetzger.chip_8emu;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;


public class SortService extends Service
{
	private final IBinder localBinder = new LocalBinder();
	
	
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return localBinder;
	}
	
	
	
	
	public void sortAlphabetically(List<String> listToSort)
	{
		java.util.Collections.sort(listToSort);
	}
	
	
	
	
	public class LocalBinder extends Binder
	{
		SortService getService()
		{
			return SortService.this;
		}
	}
}
