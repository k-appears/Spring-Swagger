package org.poc.datatransferobject;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.poc.domainobject.CarDO;
import org.poc.domainobject.ManufacturerDO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarDTO
{
    private Long id;

    @NotNull(message = "license plate can not be null!")
    private String licensePlate;
    private Boolean convertible;
    private CarDO.EngineType engineType;
    private Float rating;
    private Short seatCount;
    private ManufacturerDO manufacturer;
    @JsonIgnore
    private DriverDTO driver;


    public CarDTO()
    {}


    public CarDTO(Boolean convertible, CarDO.EngineType engineType, String licensePlate, ManufacturerDO manufacturer, Float rating, Short seatCount)
    {
        this.licensePlate = licensePlate;
        this.convertible = convertible;
        this.engineType = engineType;
        this.manufacturer = manufacturer;
        this.rating = rating;
        this.seatCount = seatCount;
    }


    public Long getId()
    {
        return id;
    }


    public String getLicensePlate()
    {
        return licensePlate;
    }


    public Boolean getConvertible()
    {
        return convertible;
    }


    public CarDO.EngineType getEngineType()
    {
        return engineType;
    }


    public Float getRating()
    {
        return rating;
    }


    public Short getSeatCount()
    {
        return seatCount;
    }


    public ManufacturerDO getManufacturer()
    {
        return manufacturer;
    }


    public DriverDTO getDriver()
    {
        return driver;
    }


    public void setDriver(DriverDTO driver)
    {
        this.driver = driver;
    }


    public void setId(Long id)
    {
        this.id = id;
    }
}
