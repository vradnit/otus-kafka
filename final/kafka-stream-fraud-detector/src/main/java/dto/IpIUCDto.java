package dto;

import org.apache.commons.lang.builder.ToStringBuilder;

public class IpIUCDto {
    String userId;
    String realmId;
    String ipAddress;
    Long nbLoginFailure;
    String startTime;
    String endTime;

    public IpIUCDto(String key, long nbLoginFailure, String startTime, String endTime) {
        String[] keyIpICU = key.split(":");
        this.userId = keyIpICU[0];
        this.realmId = keyIpICU[1];
        this.ipAddress = keyIpICU[2];
        this.nbLoginFailure = nbLoginFailure;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String location) {
        this.ipAddress = ipAddress;
    }

    public long getNbLoginFailure() { return nbLoginFailure; }

    public void setNbLoginFailure(long nbLoginFailure) { this.nbLoginFailure = nbLoginFailure; }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String time) {
        this.startTime = time;
    }

    public String getEndTime() { return endTime; }

    public void setEndTime(String time) { this.endTime = time; }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
