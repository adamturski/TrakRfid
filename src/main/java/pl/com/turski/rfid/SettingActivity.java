package pl.com.turski.rfid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import pl.com.turski.trak.rfid.R;

public class SettingActivity extends Activity {

    TextView serverUrlText;
    TextView gateIdText;
    Button saveButton;
    Button cancelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initView();
    }

    private void initView() {
        SharedPreferences settings = this.getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
        serverUrlText = (TextView) findViewById(R.id.serverUrlText);
        serverUrlText.setText(settings.getString(SettingKey.SERVER_URL.getKey(), SettingKey.SERVER_URL.getDefValue()));
        gateIdText = (TextView) findViewById(R.id.gateIdText);
        gateIdText.setText(settings.getString(SettingKey.GATE_ID.getKey(), SettingKey.GATE_ID.getDefValue()));
        final Context context = this;
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveSettings();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
        cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveSettings() {
        SharedPreferences settings = this.getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SettingKey.SERVER_URL.getKey(), serverUrlText.getText().toString());
        editor.putString(SettingKey.GATE_ID.getKey(), gateIdText.getText().toString());
        editor.commit();
    }
}
