package us.shandian.game.twozero.settings;

import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Bundle;

import us.shandian.game.twozero.R;
import us.shandian.game.twozero.InputListener;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
    private ListPreference mSensitivity;
    private ListPreference mVariety;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        addPreferencesFromResource(R.xml.settings);
        
        mSensitivity = (ListPreference) findPreference(SettingsProvider.KEY_SENSITIVITY);
        mVariety = (ListPreference) findPreference(SettingsProvider.KEY_VARIETY);
        
        mSensitivity.setOnPreferenceChangeListener(this);
        mVariety.setOnPreferenceChangeListener(this);
        
        // Initialize values
        int sensitivity = SettingsProvider.getInt(SettingsProvider.KEY_SENSITIVITY, 1);
        mSensitivity.setValueIndex(sensitivity);
        String[] sensitivitySummaries = getResources().getStringArray(R.array.settings_sensitivity_entries);
        mSensitivity.setSummary(sensitivitySummaries[sensitivity]);
        
        int variety = SettingsProvider.getInt(SettingsProvider.KEY_VARIETY, 0);
        mVariety.setValueIndex(variety);
        String[] varietySummaries = getResources().getStringArray(R.array.settings_variety_entries);
        mVariety.setSummary(varietySummaries[variety]);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSensitivity) {
            int sensitivity = Integer.valueOf((String) newValue);
            String[] sensitivitySummaries = getResources().getStringArray(R.array.settings_sensitivity_entries);
            mSensitivity.setSummary(sensitivitySummaries[sensitivity]);
            SettingsProvider.putInt(SettingsProvider.KEY_SENSITIVITY, sensitivity);
            InputListener.loadSensitivity();
            return true;
        } else if (preference == mVariety) {
            int variety = Integer.valueOf((String) newValue);
            String[] varietySummaries = getResources().getStringArray(R.array.settings_variety_entries);
            mVariety.setSummary(varietySummaries[variety]);
            SettingsProvider.putInt(SettingsProvider.KEY_VARIETY, variety);
            Toast.makeText(this, R.string.msg_restart, 1000).show();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
