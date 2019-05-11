package org.poc.datatransferobject;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.poc.domainvalue.GeoCoordinate;
import org.poc.domainvalue.OnlineStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverDTO
{
    private Long id;

    @NotNull(message = "Username can not be null!")
    private String username;

    @NotNull(message = "Password can not be null!")
    private String password;

    private GeoCoordinate coordinate;
    @JsonProperty(value = "car")
    private CarDTO carDTO;

    @JsonProperty(required = true)
    private OnlineStatus onlinestatus;


    private DriverDTO()
    {}


    private DriverDTO(Long id, String username, String password, GeoCoordinate coordinate, CarDTO carDTO, OnlineStatus onlinestatus)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.coordinate = coordinate;
        this.carDTO = carDTO;
        this.onlinestatus = onlinestatus;
    }


    public static DriverDTOBuilder newBuilder()
    {
        return new DriverDTOBuilder();
    }


    @JsonProperty
    public Long getId()
    {
        return id;
    }


    public String getUsername()
    {
        return username;
    }


    public String getPassword()
    {
        return password;
    }


    public GeoCoordinate getCoordinate()
    {
        return coordinate;
    }


    public CarDTO getCarDTO()
    {
        return carDTO;
    }


    public OnlineStatus getOnlinestatus()
    {
        return onlinestatus;
    }

    public static class DriverDTOBuilder
    {
        private Long id;
        private String username;
        private String password;
        private GeoCoordinate coordinate;
        private CarDTO carDTO;
        private OnlineStatus onlinestatus;


        public DriverDTOBuilder setId(Long id)
        {
            this.id = id;
            return this;
        }


        public DriverDTOBuilder setUsername(String username)
        {
            this.username = username;
            return this;
        }


        public DriverDTOBuilder setPassword(String password)
        {
            this.password = password;
            return this;
        }


        public DriverDTOBuilder setCoordinate(GeoCoordinate coordinate)
        {
            this.coordinate = coordinate;
            return this;
        }


        public DriverDTOBuilder setCarDTO(CarDTO carDTO)
        {
            this.carDTO = carDTO;
            return this;
        }


        public DriverDTOBuilder setOnlineStatus(OnlineStatus onlinestatus)
        {
            this.onlinestatus = onlinestatus;
            return this;
        }


        public DriverDTO createDriverDTO()
        {
            return new DriverDTO(id, username, password, coordinate, carDTO, onlinestatus);
        }

    }
}
