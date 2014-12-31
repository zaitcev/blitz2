package us.zaitcev.package1;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment
       extends PreferenceFragment
       implements OnSharedPreferenceChangeListener {
  public static final String KEY_PREF_SHARE_TYPE = "pref_shareType";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    // abar.setDisplayHomeAsUpEnabled(true);

    // HelloAndroid my_activity = (HelloAndroid) getActivity();

    /*
     * It's quite odd that inflation is asynchronous elsewhere, but we
     * can do findPreference() right after addPreferencesFromResource().
     * But it works.
     */
    ListPreference p = (ListPreference) findPreference(KEY_PREF_SHARE_TYPE);
    if (p != null) {
      String type = p.getEntry().toString();
      if (type != null) {
        // my_activity.addInfo("Scx");
        p.setSummary(type);
      } else {
        // my_activity.addInfo("Sct");
      }
    } else {
      // my_activity.addInfo("Scn");
    }

    // XXX sadly this does not work
    // setHasOptionsMenu(false);  // "has items to contribute"
  }

  @Override
  public void onResume() {
    super.onResume();
    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
    sp.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
    sp.unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public void onDestroy () {
    HelloAndroid my_activity = (HelloAndroid) getActivity();
    // my_activity.addInfo("Sd");

    /* Maybe call setDisplayHomeAsUpEnabled(false) in this method too? */
    my_activity.setMenuInhibit(false);

    Activity activity = getActivity();
    ActionBar abar = activity.getActionBar();
    abar.setDisplayHomeAsUpEnabled(false);

    super.onDestroy();
  }

  /*
   * Docs say not to paint anything in onInflate, use onAttach or onCreateView
   * instead. P3: But onAttach is before onCreate... so?
   * N.B. Added in API level 12
   */
  /*
  @Override
  public void onInflate(Activity activity, AttributeSet attrs,
                        Bundle savedInstanceState) {
    HelloAndroid my_activity = (HelloAndroid) getActivity();
    my_activity.addInfo("Si");
    super.onInflate(activity, attrs, savedInstanceState);
  }
   */

  public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
    // HelloAndroid my_activity = (HelloAndroid) getActivity();

    if (key.equals(KEY_PREF_SHARE_TYPE)) {
      // Preference connectionPref = findPreference(key);
      // connectionPref.setSummary(sp.getString(key, ""));
      ListPreference p = (ListPreference) findPreference(KEY_PREF_SHARE_TYPE);
      if (p == null) {
        // my_activity.addInfo("St1");
        return;
      }
      String type = p.getEntry().toString();
      if (type == null) {
        // my_activity.addInfo("St2");
        return;
      }
      // my_activity.addInfo("St");
      p.setSummary(type);
    }
  }
}
