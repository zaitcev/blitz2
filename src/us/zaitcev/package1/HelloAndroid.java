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
import android.view.MenuInflater;
import android.view.MenuItem;
// import android.widget.LinearLayout;
import android.widget.TextView;

public class HelloAndroid extends Activity {

  // private LinearLayout root;
  private ActionBar abar;
  private TextView info;
  boolean options_menu_inhibit;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); /* BTW, inflation and repaint happen later */

    abar = getActionBar();

    // Only works after onFinishInflate()
    // root = (LinearLayout) findViewById(R.id.root);
    // setContentView(root);

    // TextView info = new TextView(this);
    info = (TextView) findViewById(R.id.text_status);

    String infoText = getResources().getString(R.string.text_hello);
    info.setText(infoText);

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /*
   * From the docs:
   *  Deriving classes should always call through to the base class
   *  implementation.
   *  You must return true for the menu to be displayed; if you return false
   *  it will not be shown.
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (options_menu_inhibit) {
      addInfo("Pf");
      return false;
    }
    addInfo("Pt");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    FragmentManager mgr;

    switch (item.getItemId()) {
    case R.id.action_settings:
      if (abar != null) {
        abar.setDisplayHomeAsUpEnabled(true);
      }

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

      setMenuInhibit(true);

      return true;

    case android.R.id.home:
      /*
       * Generally speaking, Up is different from Back. However, it seems
       * that in our case it's okay to do this, because there's only one
       * path into Settings.
       */
      mgr = getFragmentManager();
      mgr.popBackStack();

      if (abar != null) {
        abar.setDisplayHomeAsUpEnabled(false);
      }
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

  public void addInfo(String msg) {
    if (info != null) {
      info.setText(info.getText() + " " + msg);
    }
  }

  public void setMenuInhibit(boolean inhibit) {
    options_menu_inhibit = inhibit;
    invalidateOptionsMenu();
    if (inhibit) {
      addInfo("Mi");
    } else {
      addInfo("Me");
    }
  }
}
