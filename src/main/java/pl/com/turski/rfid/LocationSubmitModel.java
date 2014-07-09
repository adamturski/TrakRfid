package pl.com.turski.rfid;

/**
 * User: Adam
 */
public class LocationSubmitModel {

    private Long vehicleId;
    private Double latitude;
    private Double longitude;

    public LocationSubmitModel(Long vehicleId, Double latitude, Double longitude) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
