package us.zaitcev.package1;

/* For subpackages */
// import us.zaitcev.package1.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

// import java.io.BufferedInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HttpsURLConnection;
import android.content.res.Resources.NotFoundException;
import android.util.Base64;

class AppErrorException extends Exception {
  // We don't know why Exception is Serializable in Android but not normal Java.
  private static final long serialVersionUID = 1L;
  // For some reason, Exception does not have a constructor(String).
  // So, we include our own trivial version. Yeah, Java is odd.
  private String message;
  AppErrorException(String msg) {
    message = msg;
  }
  public String toString() {
    return message;
  }
}

public class HelloAndroid extends Activity {

  // private LinearLayout root;
  private ActionBar abar;
  private TextView info;
  boolean options_menu_inhibit;

  // https://developer.android.com/training/articles/security-ssl.html
  // XXX private?
  public SSLContext addcert() throws AppErrorException {
    Certificate ca;
    SSLContext ctx;

    try {
      // Load CAs from an InputStream
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      InputStream caInput = getResources().openRawResource(R.raw.cacert);
      try {
          ca = cf.generateCertificate(caInput);
      } finally {
        try { caInput.close(); } catch (IOException e) { ; /* well, son */ }
      }
    } catch (CertificateException e) {
      throw new AppErrorException(e.toString());
    }

    try {
      // Create a KeyStore containing our trusted CAs
      // The only way to initialize a KeyStore is to load it. So we load null.
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null, null);
      ks.setCertificateEntry("ca", ca);

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(ks);

      // Create an SSLContext that uses our TrustManager
      ctx = SSLContext.getInstance("TLS");
      ctx.init(null, tmf.getTrustManagers(), null);

    } catch (KeyManagementException|KeyStoreException|
             IOException|NoSuchAlgorithmException|CertificateException e) {
      throw new AppErrorException(e.toString());
    }

    return ctx;
  }

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
      // addInfo("Pf");
      return false;
    }
    // addInfo("Pt");
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

  private class DownloadResult {
    public String error;
    public String data;
    DownloadResult(String error_, String data_) {
      error = error_;
      data = data_;
    }
  }

  private class DownloadShareTask extends AsyncTask<URL, Void, DownloadResult> {

    protected DownloadResult doInBackground(URL... urls) {
      URL url = urls[0];
      URLConnection conn;
      InputStream is;
      InputStreamReader isr;
      String ret = "-";

      try {
        conn = url.openConnection();
      } catch (IOException e) {
        // XXX Can't show the location because of passwords.
        return new DownloadResult("error connecting", null);
      }

      if (conn instanceof HttpsURLConnection) {
        // XXX compare hostnames and only do this for certain domains.

        SSLContext ctx;
        try {
          ctx = addcert();
        } catch (AppErrorException e) {
          return new DownloadResult("addcert error, " + e, null);
        }

        HttpsURLConnection sslconn = (HttpsURLConnection) conn;
        sslconn.setSSLSocketFactory(ctx.getSocketFactory());
      }

      // Authenticator.setDefault(new Authenticator() {
      //   protected PasswordAuthentication getPasswordAuthentication() {
      //     return new PasswordAuthentication("username",
      //                                       "password".toCharArray());
      //   }
      // });

      // HttpURLConnection will not use the username and password in the URL
      // unless we force the issue.
      if (url.getUserInfo() != null) {
        // String authString = username + ":" + password;
        byte[] b = Base64.encode(url.getUserInfo().getBytes(), Base64.NO_WRAP);
        String basicAuth = "Basic " + new String(b);
        conn.addRequestProperty("Authorization", basicAuth);
      }

      try {
        is = conn.getInputStream();
      } catch (javax.net.ssl.SSLHandshakeException e) {
        // This is a subclass of IOException, but we're interested int his
        // specifically because it happens when CA is unknown.
        return new DownloadResult("SSL handshake failed, " + e, null);
      } catch (IOException e) {
        // XXX Can't show the location because of passwords.
        return new DownloadResult("get failed", null);
      } catch (java.lang.IllegalArgumentException e) {
        // something like  host = null
        return new DownloadResult("bad connection", null);
      } catch (Exception e) {
        // You wouldn't believe how many undocumented exceptions Android throws.
        // Could even be android.os.NetworkOnMainThreadException.
        // e.getMessage() returns null, only e.toString() works
        return new DownloadResult("get, " + e, null);
      }
      try {
        // Not bothering with conn.getContentEncoding(), our protocol is UTF-8.
        try {
          isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          return new DownloadResult("UTF-8", null);
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
        // Android throws exceptions that we do not even realize.
        // At least report them.
        return new DownloadResult("download, " + e, null);
      } finally {
        // conn.disconnect(); -- only for HttpURLConnection
        try { is.close(); } catch (IOException e) { ; /* well, son */ }
      }
      return new DownloadResult(null, ret);
    }

    protected void onPostExecute(DownloadResult result) {
      if (result.error != null) {
        setInfo("ERROR: " + result.error);
      } else {
        setInfo("Clip: " + result.data);
        setClipboard(result.data);
      }
    }
  }

  public void setClipboard(String text) {
    ClipboardManager clipboard = (ClipboardManager)
        getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("blitz2", text);
    clipboard.setPrimaryClip(clip);
  }

  // public void addInfo(String msg) {
  //   if (info != null) {
  //     info.setText(info.getText() + " " + msg);
  //   }
  // }

  public void setInfo(String msg) {
    if (info != null) {
      info.setText(msg);
    }
  }

  public void setMenuInhibit(boolean inhibit) {
    options_menu_inhibit = inhibit;
    invalidateOptionsMenu();
    // if (inhibit) {
    //   addInfo("Mi");
    // } else {
    //   addInfo("Me");
    // }
  }
}
