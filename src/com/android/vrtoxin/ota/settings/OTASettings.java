/*
     Copyright (C) 2013 SlimRoms
     Copyright (C) 2016 The VRToxin Project

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/

package com.android.vrtoxin.ota.settings;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.android.vrtoxin.ota.updater.UpdateListener;
import com.android.vrtoxin.R;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class OTASettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    @SuppressWarnings("unused")
    private static final String TAG = "OTASettings";

    private static final String KEY_UPDATE_INTERVAL = "update_interval";
    private static final String LAST_INTERVAL = "lastInterval";

    private ListPreference mUpdateInterval;
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.ota_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mUpdateInterval = (ListPreference) prefs.findPreference(KEY_UPDATE_INTERVAL);
        mUpdateInterval.setValueIndex(getUpdateInterval());
        mUpdateInterval.setSummary(mUpdateInterval.getEntry());
        mUpdateInterval.setOnPreferenceChangeListener(this);     
        
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mUpdateInterval) {
            int intervalValue = Integer.valueOf((String) objValue);
            int index = mUpdateInterval.findIndexOfValue((String) objValue);
            setUpdateInterval(intervalValue);
            mUpdateInterval.setSummary(mUpdateInterval.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void setUpdateInterval(int interval) {
            boolean enableUpdateCheck = true;
            switch(interval) {
                case 0:
                    UpdateListener.interval = AlarmManager.INTERVAL_DAY;
                    break;
                case 1:
                    UpdateListener.interval = AlarmManager.INTERVAL_HALF_DAY;
                    break;
                case 2:
                    UpdateListener.interval = AlarmManager.INTERVAL_HOUR;
                    break;
                case 3:
                    enableUpdateCheck = false;
                    break;
            }
            if (enableUpdateCheck) {
                SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
                prefs.edit().putLong(LAST_INTERVAL, UpdateListener.interval).apply();
                WakefulIntentService.cancelAlarms(this);
                WakefulIntentService.scheduleAlarms(new UpdateListener(), this, true);
            } else {
                SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
                prefs.edit().putLong(LAST_INTERVAL, 1).apply();
                com.android.vrtoxin.ota.updater.ConnectivityReceiver.disableReceiver(this);
                WakefulIntentService.cancelAlarms(this);
            }
    }

    private int getUpdateInterval() {
        SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
        long value = prefs.getLong(LAST_INTERVAL,0);
        int settingsValue;
        if (value == AlarmManager.INTERVAL_DAY) {
            settingsValue = 0;
        } else if (value == AlarmManager.INTERVAL_HALF_DAY || value == 0) {
            settingsValue = 1;
        } else if (value == AlarmManager.INTERVAL_HOUR) {
            settingsValue = 2;
        } else {
            settingsValue = 3;
        }
        return settingsValue;
    }

}
