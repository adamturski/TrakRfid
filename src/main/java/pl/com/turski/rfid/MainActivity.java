package pl.com.turski.rfid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import pl.com.turski.trak.rfid.R;

public class MainActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    TextView rfidStatusLabel;
    TextView connectionStatusLabel;
    Button rfidStatusRefreshButton;
    Button connectionStatusRefreshButton;
    Button settingsButton;

    private NfcAdapter nfcAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
        checkrfidStatus();
        checkConnectionStatus();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                SharedPreferences settings = this.getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
                String gateId = settings.getString(SettingKey.GATE_ID.getKey(), SettingKey.GATE_ID.getDefValue());
                MovementSubmitModel submitModel = new MovementSubmitModel(Long.parseLong(gateId), tag);
                new SubmitMovementTask().execute(submitModel);

            } else {
                Log.d(App.TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    SharedPreferences settings = this.getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
                    String gateId = settings.getString(SettingKey.GATE_ID.getKey(), SettingKey.GATE_ID.getDefValue());
                    MovementSubmitModel submitModel = new MovementSubmitModel(Long.parseLong(gateId), tag);
                    new SubmitMovementTask().execute(submitModel);
                    break;
                }
            }
        }
    }

    private void initView() {
        final Context context = this;
        rfidStatusLabel = (TextView) findViewById(R.id.rfidStatusLabel);
        rfidStatusLabel.setText("");
        connectionStatusLabel = (TextView) findViewById(R.id.appStatusLabel);
        connectionStatusLabel.setText("");
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingActivity.class);
                startActivity(intent);
            }
        });
        rfidStatusRefreshButton = (Button) findViewById(R.id.rfidStatusRefreshButton);
        rfidStatusRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkrfidStatus();
            }
        });
        connectionStatusRefreshButton = (Button) findViewById(R.id.appStatusRefreshButton);
        connectionStatusRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkConnectionStatus();
            }
        });
    }

    private void checkrfidStatus() {
        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            rfidStatusLabel.setText("OK");
        } else {
            rfidStatusLabel.setText("Wyłączony");
        }
    }

    private void checkConnectionStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            connectionStatusLabel.setText("OK");
        } else {
            connectionStatusLabel.setText("Brak");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, nfcAdapter);
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, nfcAdapter);
        super.onPause();
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}
