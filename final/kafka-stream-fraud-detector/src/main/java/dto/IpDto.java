package dto;

import org.apache.commons.lang.builder.ToStringBuilder;

public class IpDto {
    String ipAddress;
    long nbLoginFailure = 0;
    String startTime;
    String endTime;

    public IpDto (String ipAddress, long nbLoginFailure, String startTime, String endTime) {
        this.ipAddress = ipAddress;
        this.nbLoginFailure = nbLoginFailure;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getNbLoginFailure() {
        return nbLoginFailure;
    }

    public void setNbLoginFailure(long nbLoginFailure) { this.nbLoginFailure = nbLoginFailure; }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String time) {
        this.startTime = time;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String time) {
        this.endTime = time;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
