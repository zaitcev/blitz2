package us.zaitcev.package1;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    // abar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onDestroy () {
    Activity activity = getActivity();
    ActionBar abar = activity.getActionBar();
    abar.setDisplayHomeAsUpEnabled(false);
    super.onDestroy();
  }
}
