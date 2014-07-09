package pl.com.turski.rfid;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;
import com.appspot.trak.movement.Movement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * User: Adam
 */
public class SubmitMovementTask extends AsyncTask<MovementSubmitModel, Void, String> {

    @Override
    protected String doInBackground(MovementSubmitModel... submitModels) {
        try {
            Movement movement = App.getMovementService();
            MovementSubmitModel submitModel = submitModels[0];

            Tag tag = submitModel.getTag();
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        String shipmentId = readText(ndefRecord);
                        movement.addMovementFromGate(Long.parseLong(shipmentId), submitModel.getGateId()).execute();
                    } catch (UnsupportedEncodingException e) {
                        Log.e(App.TAG, "Unsupported Encoding", e);
                    }
                }
            }

        } catch (IOException e) {
            Log.e(App.TAG, "IOException occured during adding movement", e);
            //App.showAlert("Wystąpił błąd podczas wysyłania lokalizacji na serwer: " + e.getMessage());
        }
        return null;
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }
}
