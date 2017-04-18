/**
 * Created by Tom on 4/4/17.
 */

package com.tommetzger.chip_8emu;

import android.os.Bundle;
import android.preference.PreferenceFragment;





public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
