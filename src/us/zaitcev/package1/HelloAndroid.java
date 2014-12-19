package us.zaitcev.package1;

/* For subpackages */
// import us.zaitcev.package1.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
// import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
// import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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

    // final Button go = (Button) findViewById(R.id.button_go);
    // go.setOnClickListener(new Button.OnClickListener() {
    //   @Override public void onClick(View arg0) {
    //     info.setText("Go"); // XXX
    //   }
    // });
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

  public void onGo(View arg) {
    URL url;

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    String loc = sp.getString("pref_shareLocation", "*");
    // info.setText(loc);

    try {
      url = new URL(loc);
    } catch (MalformedURLException e) {
      // XXX Can't show the location because of passwords.
      info.setText("bad share location");
      return;
    }
    new DownloadShareTask().execute(url);
  }

  private class DownloadShareTask extends AsyncTask<URL, Void, String> {

    protected String doInBackground(URL... urls) {
      URL url = urls[0];
      URLConnection conn;
      InputStream is;
      InputStreamReader isr;
      String ret = "-";

      try {
        conn = url.openConnection();
      } catch (IOException e) {
        // XXX Can't show the location because of passwords.
        return "error connecting";
      }
      try {
        is = conn.getInputStream();
      } catch (IOException e) {
        // XXX Can't show the location because of passwords.
        // XXX Are we leaking a socket here? No .disconnect(), what to do?
        return "get failed";
      } catch (java.lang.IllegalArgumentException e) {
        // something like  host = null
        return "bad connection";
      } catch (Exception e) {
        // You wouldn't believe how many undocumented exceptions Android throws.
        // Could even be android.os.NetworkOnMainThreadException.
        // e.getMessage() returns null
        return "get error " + e;
      }
      try {
        // Not bothering with conn.getContentEncoding(), our protocol is UTF-8.
        try {
          isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // should never happen
          return "UTF-8";
        }
        // BufferedReader in = BufferedReader(isr);
        char[] buf = new char[1000];
        int len;
        try {
          len = isr.read(buf, 0, buf.length);
        } catch (IOException e) {
          len = -1;
        }
        if (len < 0) {
          ret = "*";
        } else {
          ret = new String(buf, 0, len);
        }
      } catch (Exception e) {
        // Android
        ret = "Error";
      } finally {
        // conn.disconnect(); -- only for HttpURLConnection
        try {
          is.close();
        } catch (IOException e) {
          ; // well, son
        }
      }
      return ret;
    }

    protected void onPostExecute(String result) {
      addInfo(result);
    }
  }

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
