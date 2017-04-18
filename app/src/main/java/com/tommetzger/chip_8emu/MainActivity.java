package com.tommetzger.chip_8emu;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private Context context;
    private MainActivity thisClass;

    private Toolbar navBar;

    private String[] romFiles;
    private File romsDirectory;

    private ListView romList;
	
	private ArrayAdapter<String> arrayAdapter;
	
	SharedPreferences preferences;
	SharedPreferences.Editor preferencesEditor;
	
	private SortService sortService;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;
        thisClass = MainActivity.this;
	
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		preferencesEditor = preferences.edit();


        navBar = (Toolbar) findViewById(R.id.navBar);
        setSupportActionBar(navBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		Intent intent = new Intent(this, SortService.class);
		bindService(intent, localConnection, Context.BIND_AUTO_CREATE);
		

        romList = (ListView) findViewById(R.id.romsList);
		final List<String> arrayList = new ArrayList<>();



        romsDirectory = new File(String.valueOf(this.getDir("ROMs", MODE_PRIVATE)));
        if (!romsDirectory.exists())
        {
            Log.d("","Making ROMs Directory...");
            if (!romsDirectory.mkdirs())
            {
                Log.d("ERROR: ", "Could Not Make Directory: " + romsDirectory);
            }
            Log.d("", "Done.");
            //display NO ROMs text
        }
        else
        {
            Log.d("", "ROMs Directory Exists.");
            if (romsDirectory.listFiles().length > 0)
            {
                Log.d("", "Listing Files...");
                for (int i = 0; i < romsDirectory.listFiles().length; i++)
                {
                    Log.d("", "   FileName: " + romsDirectory.listFiles()[i].getName());
                    arrayList.add(romsDirectory.listFiles()[i].getName());
                }
                Log.d("", "Done Listing Files.");
                Log.d("", "ROM Count: " + romsDirectory.listFiles().length);
            }
            else
            {
                Log.d("", "No ROMs");
                //display NO ROMs text
            }
        }
        
        if (preferences.getString("Sort", "") == "Alphabetically")
        {
            sortService.sortAlphabetically(arrayList);
        }
        else if(preferences.getString("Sort", "") == "Stars")
		{
			//do something else
		}

        final ROMListAdapter arrayAdapter = new ROMListAdapter(this, arrayList);
        romList.setAdapter(arrayAdapter);
        
        romList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d("", "Launch EmulatorActivity...");
                Intent emulationIntent = new Intent(thisClass, EmulationActivity.class);
                Bundle data = new Bundle();
                data.putString("ROM", romsDirectory.listFiles()[position].getAbsolutePath());
                emulationIntent.putExtras(data);
                startActivity(emulationIntent);
            }
        });
	
        romList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                AlertDialog.Builder deleteROMAlertBuilder = new AlertDialog.Builder(context);
                deleteROMAlertBuilder.setTitle("Delete ROM?");
                deleteROMAlertBuilder.setMessage("Would you like to delete this ROM from your library?");
                deleteROMAlertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("", "Attempting To Delete File: " + arrayList.get(position).toString() + "...");
                        File romToDelete = new File(romsDirectory + "/" + arrayList.get(position).toString());
                        romToDelete.delete();
                        arrayList.remove(position);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
                deleteROMAlertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Do Nothing
                    }
                });

                AlertDialog deleteROMAlert = deleteROMAlertBuilder.create();
                deleteROMAlert.show();
                return true;
            }
        });
    }
    
    
    
    
    public void setROMRating(View view)
	{
		Log.d("","CLICK !!!!!!!!");
		RatingBar romRatingBar = (RatingBar) view;
		String romName;
		
		RelativeLayout row = (RelativeLayout) romRatingBar.getParent();
		
		for(int i = 0; i < row.getChildCount(); i++)
		{
			LinearLayout nextChild = (LinearLayout) row.getChildAt(i);
			if (nextChild.getId() == R.id.romLabelContainer)
			{
				TextView romLabel = (TextView) nextChild.getChildAt(0);
				romName = romLabel.getText().toString();
				
				Log.d("", "Set Rating For: "+romName);
			}
		}
		
//		preferencesEditor.putInt(romName+"Rating", romRatingBar.getNumStars());
	}

	
	

    @Override
    protected void onResume()
    {
        super.onResume();


//        Log.d("", "Checking for new ROMs");
//        if (romsDirectory.listFiles().length > 0)
//        {
//            Log.d("", "Listing Files...");
//            for (int i = 0; i < romsDirectory.listFiles().length; i++)
//            {
//                Log.d("", "   FileName: " + romsDirectory.listFiles()[0].getName());
//            }
//            Log.d("", "Done Listing Files.");
//            Log.d("", "ROM Count: " + romsDirectory.listFiles().length);
//        }
//        else
//        {
//            Log.d("", "No ROMs");
//            //display NO ROMs text
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.searchButtonItem:
                Intent launchWebBrowserActivity = new Intent(getApplicationContext(), WebBrowserActivity.class);
                startActivity(launchWebBrowserActivity);
                break;
            case R.id.settingsButtonItem:
                Intent launchSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(launchSettingsActivity);
                break;
            default:
                break;
        }


        return true;
    }
    
    
    
	private ServiceConnection localConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			SortService.LocalBinder binder = (SortService.LocalBinder) service;
			sortService = binder.getService();
		}
		
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			
		}
	};
}
