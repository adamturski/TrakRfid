package pl.com.turski.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * User: Adam
 */
public class LocationReceiver extends BroadcastReceiver {

    double oldLatitude, oldLongitude;
    double latitude, longitude;

    @Override
    public void onReceive(final Context context, final Intent calledIntent) {
        latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);

        if (oldLatitude != latitude || oldLongitude != longitude) {
            oldLatitude = latitude;
            oldLongitude = longitude;
            updateRemote( latitude, longitude);
        }
    }

    private void updateRemote(final double latitude, final double longitude) {
        Log.d("TRAK_rfid", "Sending location to server: [latitude='" + latitude + "', longitude='" + longitude + "']");
        SharedPreferences settings = App.getAppContext().getSharedPreferences("pl.com.turski.trak.rfid", Context.MODE_PRIVATE);
        String vehicleId = settings.getString(SettingKey.VEHICLE_ID.getKey(), SettingKey.VEHICLE_ID.getDefValue());
        LocationSubmitModel submitModel = new LocationSubmitModel(Long.parseLong(vehicleId),latitude,longitude);
        new SubmitLocationTask().execute(submitModel);
    }
}
