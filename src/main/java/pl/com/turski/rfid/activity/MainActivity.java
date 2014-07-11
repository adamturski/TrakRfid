package pl.com.turski.rfid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.appspot.trak.movement.Movement;
import pl.com.turski.rfid.App;
import pl.com.turski.rfid.settings.SettingKey;
import pl.com.turski.trak.rfid.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends Activity {

    private SharedPreferences settings;
    private NfcAdapter nfcAdapter;

    Button settingsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
        checkNfc();
        settings = this.getSharedPreferences("pl.com.turski.trak.app", Context.MODE_PRIVATE);
    }

    private void initView() {
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkNfc() {
        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Włącz obsługę NFC i spróbuj ponownie")
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (App.MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new SubmitMovementTask(this).execute(tag);
            } else {
                Log.d(App.TAG, "Niewspierany typ tagu: " + type);
                Toast.makeText(this, "Niewspierany typ tagu: " + type, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            setupForegroundDispatch(this, nfcAdapter);
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(App.MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            stopForegroundDispatch(this, nfcAdapter);
        }
        super.onPause();
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private class SubmitMovementTask extends AsyncTask<Tag, Void, Void> {

        private Context context;
        private ProgressDialog progressDialog;

        private SubmitMovementTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Aktualizacja statusu przesyłki...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Tag... tags) {
            try {
                Movement movement = App.getMovementService();
                Tag tag = tags[0];

                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    return null;
                }

                NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                NdefRecord[] records = ndefMessage.getRecords();
                for (NdefRecord ndefRecord : records) {
                    if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                        try {
                            String shipmentId = readText(ndefRecord);
                            String gateId = settings.getString(SettingKey.GATE_ID.getKey(), SettingKey.GATE_ID.getDefValue());
                            movement.addMovementFromGate(Long.parseLong(shipmentId), Long.parseLong(gateId)).execute();
                        } catch (UnsupportedEncodingException e) {
                            Log.e(App.TAG, "Unsupported Encoding", e);
                        }
                    }
                }

            } catch (IOException e) {
                Log.e(App.TAG, "IOException occured during adding movement", e);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Wystąpił błąd podczas dodawania przesunięcia przesyłki", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
        }
    }
}
