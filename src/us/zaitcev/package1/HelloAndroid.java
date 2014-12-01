package us.zaitcev.package1;

/* For subpackages */
// import us.zaitcev.package1.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelloAndroid extends Activity {
  // private LinearLayout root;
  private TextView info;
  private ActionBar abar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); /* BTW, inflation and repaint happen later */

    // Only works after onFinishInflate()
    // root = (LinearLayout) findViewById(R.id.root);
    // setContentView(root);

    // TextView info = new TextView(this);
    info = (TextView) findViewById(R.id.text_status);

    abar = getActionBar();
    if (abar != null) {
      String infoText = getResources().getString(R.string.text_hello);
      info.setText(infoText);
    } else {
       info.setText("null abar");
       /* XXX establish some kind of "defective" mode and avoid a crash? */
    }

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    FragmentManager mgr;

    switch (item.getItemId()) {
    case R.id.action_settings:
      abar.setDisplayHomeAsUpEnabled(true);

      mgr = getFragmentManager();
      FragmentTransaction trans = mgr.beginTransaction();
      SettingsFragment sf = new SettingsFragment();
      trans.replace(R.id.root, sf);
      /*
       * This back stack is managed by the activity and allows the user to
       * return to the previous fragment state, by pressing the Back button.
       */
      trans.addToBackStack(null);
      trans.commit();

      return true;

    case android.R.id.home:
      /*
       * Generally speaking, Up is different from Back. However, it seems
       * that in our case it's okay to do this, because there's only one
       * path into Settings.
       */
      mgr = getFragmentManager();
      mgr.popBackStack();

      abar.setDisplayHomeAsUpEnabled(false);
      return true;

    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /*
   * The onNavigateUp is only available in API level 16.
  @Override
  public boolean onNavigateUp() {
    FragmentManager mgr = getFragmentManager();
    mgr.popBackStack();

    abar.setDisplayHomeAsUpEnabled(false);
    return true; // "returns true if this Activity is finished"
  }
  */
}
}
