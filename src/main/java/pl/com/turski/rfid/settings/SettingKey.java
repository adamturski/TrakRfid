package pl.com.turski.rfid.settings;

/**
 * User: Adam
 */
public enum SettingKey {
    SERVER_URL("http://192.168.1.2:8080/_ah/api"), GATE_ID("1");

    private String defValue;

    private SettingKey(String defValue) {
        this.defValue = defValue;
    }

    public String getKey() {
        return name();
    }

    public String getDefValue() {
        return defValue;
    }
}
