/**
 * Created by Tom on 4/2/17.
 */

package com.tommetzger.chip_8emu;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;





public class SettingsActivity extends AppCompatActivity
{
    private Toolbar navBar;
	
	SharedPreferences preferences;
	SharedPreferences.Editor preferencesEditor;
	
	
	

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		preferencesEditor = preferences.edit();


        navBar = (Toolbar) findViewById(R.id.navBar);
        navBar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(navBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getFragmentManager().beginTransaction().replace(R.id.settingsFragmentFrame,
                new SettingsFragment()).commit();
        
//        getFragmentManager().beginTransaction().replace(R.id.settingsFragmentFrame2,
//                new SettingsFragment2()).commit();
    }
    
    
    
    
    public void sortROMsAlphabetically(View view)
	{
		preferencesEditor.putString("Sort", "Alphabetically");
		preferencesEditor.commit();
		//refresh main activity
	}
	
	
	
	
	public void downloadTestROM(View view)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.checker_background);
		builder.setContentTitle("Download Started");
		builder.setContentText("Downloading Test ROM");
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);

		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, builder.build());
		
		new DownloadTestROM().execute("https://www.opensource.southernerd.us/resources/downloads/cos355/projects/chip8-emu/INVADERS", String.valueOf(this.getDir("ROMs", Context.MODE_PRIVATE)));
		//refresh main activity
	}
	
	
	
	
	public static void testROMDownloaded()
	{
		
	}
    
    
    
    
    public void deleteAllROMs(View view)
    {
//		Toast.makeText(getApplicationContext(), "Deleting All ROMs...", Toast.LENGTH_LONG).show();
		startService(new Intent(getApplicationContext(), DeleteAllROMsService.class));
//		Toast.makeText(getApplicationContext(), "Done.", Toast.LENGTH_LONG).show();
    }
    
    
    
    
    public void resetSettings(View view)
	{
		preferences.edit().clear().commit();
		finish();
		startActivity(getIntent());
		Toast.makeText(getApplicationContext(), "Settings Reset.", Toast.LENGTH_LONG).show();
	}
}
