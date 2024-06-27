package dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class LocationDto {
    String userId;
    String realmId;
    String location;
    Long time;

    @JsonCreator
    public LocationDto( @JsonProperty("userId") String userId,
                        @JsonProperty("realmId") String realmId,
                        @JsonProperty("location") String location,
                        @JsonProperty("time")Long time) {
        this.userId = userId;
        this.realmId = realmId;
        this.location = location;
        this.time = time;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
