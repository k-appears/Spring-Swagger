package org.poc.domainvalue;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OnlineStatus
{
    @JsonProperty("ONLINE")
    ONLINE,
    @JsonProperty("OFFLINE")
    OFFLINE
}
