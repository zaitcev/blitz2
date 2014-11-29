package us.zaitcev.package1;

/* For subpackages */
// import us.zaitcev.package1.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelloAndroid extends Activity {
  private LinearLayout root;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); /* BTW, inflation and repaint happen later */

    // Only works after onFinishInflate()
    // root = (LinearLayout) findViewById(R.id.root);
    // setContentView(root);

    // TextView info = new TextView(this);
    TextView info = (TextView) findViewById(R.id.text_status);

    // XXX Use ActionBar for something productive or get rid of it.
    ActionBar abar = getActionBar();
    if (abar != null) {
      String infoText = getResources().getString(R.string.text_hello);
      info.setText(infoText);
    } else {
       info.setText("null abar");
    }
  }
}
