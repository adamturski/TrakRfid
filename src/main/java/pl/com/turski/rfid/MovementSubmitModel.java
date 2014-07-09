package pl.com.turski.rfid;

import android.nfc.Tag;

/**
 * User: Adam
 */
public class MovementSubmitModel {

    private Long gateId;
    private Tag tag;

    public MovementSubmitModel(Long gateId, Tag tag) {
        this.gateId = gateId;
        this.tag = tag;
    }

    public Long getGateId() {
        return gateId;
    }

    public void setGateId(Long gateId) {
        this.gateId = gateId;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
