package pl.com.turski.rfid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.appspot.trak.movement.Movement;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import pl.com.turski.rfid.settings.SettingKey;

/**
 * User: Adam
 */
public class App extends Application {

    public final static String TAG = "TRAK_RFID";

    private static Context context;

    @Override
    public void onCreate() {
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }

    public static Movement getMovementService() {
        SharedPreferences settings = App.getAppContext().getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
        String serverUrl = settings.getString(SettingKey.SERVER_URL.getKey(), SettingKey.SERVER_URL.getDefValue());
        Movement.Builder builder = new Movement.Builder(
                new ApacheHttpTransport(), new GsonFactory(), null).setRootUrl(serverUrl);
        return builder.build();
    }
}