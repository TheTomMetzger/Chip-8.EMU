/**
 * Created by Tom on 4/17/17.
 */

package com.tommetzger.chip_8emu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;





class DownloadTestROM extends AsyncTask<String, String, String>
{
	private String ROMS_DIRECTORY;
	
	
	
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
	}
	
	
	
	
	@Override
	protected String doInBackground(String... params)
	{
		int count;
		
		try
		{
			Log.d("TEST ROM DOWNLOADER", "Attempting to download Test ROM...");
			
			ROMS_DIRECTORY = new File(params[1]).toString();
			
			URL url = new URL(params[0]);
			URLConnection connection = url.openConnection();
			connection.connect();
			
			String fileName = url.toString().substring(url.toString().lastIndexOf('/') + 1);
			int lengthOfFile = connection.getContentLength();
			
			InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
			
			OutputStream outputStream = new FileOutputStream(ROMS_DIRECTORY + "/" + fileName);
			
			byte data[] = new byte[1024];
			
			long total = 0;
			
			while ((count = inputStream.read(data)) != -1)
			{
				total += count;
				publishProgress("" + (int) ((total * 100) / lengthOfFile));
				outputStream.write(data, 0, count);
			}
			
			outputStream.flush();
			
			outputStream.close();
			inputStream.close();
		}
		catch (Exception e)
		{
			Log.e("ERROR: ", e.getMessage());
		}
		
		
		return null;
	}
	
	
	
	
	@Override
	protected void onProgressUpdate(String... progress)
	{
//		pDialog.setProgress(Integer.parseInt(progress[0]));
	}
}