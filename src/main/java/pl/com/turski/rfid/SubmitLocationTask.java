package pl.com.turski.rfid;

import android.os.AsyncTask;
import android.util.Log;
import com.appspot.trak.location.Location;

import java.io.IOException;

/**
 * User: Adam
 */
public class SubmitLocationTask extends AsyncTask<LocationSubmitModel, Void, Void> {

    @Override
    protected Void doInBackground(LocationSubmitModel... submitModels) {
        try {
            Location location = App.getLocationService();
            LocationSubmitModel submitModel = submitModels[0];
            if (submitModel == null) {
                Log.w("TRAK_rfid", "SubmitModel is null");
                return null;
            }

            location.addVehicleLocation(submitModel.getVehicleId(), submitModel.getLatitude(), submitModel.getLongitude()).execute();
        } catch (IOException e) {
            Log.e("TRAK_rfid", "IOException occured during adding vehicle localization", e);
            //App.showAlert("Wystąpił błąd podczas wysyłania lokalizacji na serwer: " + e.getMessage());
        }
        return null;
    }
}
