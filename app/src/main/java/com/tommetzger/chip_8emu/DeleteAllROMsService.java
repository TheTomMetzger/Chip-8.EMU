
/**
 * Created by Tom on 4/15/17.
 */

package com.tommetzger.chip_8emu;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class DeleteAllROMsService extends Service
{
	private File ROMS_DIRECTORY;
	
	
	
	
	public DeleteAllROMsService()
	{
		
	}
	
	
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		
		ROMS_DIRECTORY = new File(String.valueOf(this.getDir("ROMs", MODE_PRIVATE)));
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		
		
		Toast.makeText(getApplicationContext(), "Deleting All ROMs...", Toast.LENGTH_LONG).show();
		
		File[] roms = ROMS_DIRECTORY.listFiles();
		if(roms != null)
		{
			int i;
			for(i = 0; i < roms.length; i++)
			{
				roms[i].delete();
			}
		}
		
		Toast.makeText(getApplicationContext(), "Done.", Toast.LENGTH_LONG).show();
		
		
		return START_NOT_STICKY;
	}
	
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
