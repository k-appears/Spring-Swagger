package org.poc.domainobject;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "car",
    uniqueConstraints = @UniqueConstraint(name = "uc_licenseplate", columnNames = {"licensePlate"}))
public class CarDO
{
    @Id
    @SequenceGenerator(name = "carSeqGen", sequenceName = "carSeq", initialValue = 100, allocationSize = 1)
    @GeneratedValue(generator = "carSeqGen")
    private Long id;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final ZonedDateTime dateCreated = ZonedDateTime.now();

    @Column(nullable = false)
    @NotNull(message = "License can not be null!")
    private String licensePlate;

    @Column(nullable = false)
    @NotNull(message = "Seat count can not be null!")
    @Min(value = 0, message = "The value must be positive")
    private Short seatCount;

    @Column(nullable = false)
    private Boolean convertible;

    @Column
    private Float rating;

    @Enumerated(EnumType.STRING)
    @Column
    private EngineType engineType;

    @Embedded
    private ManufacturerDO manufacturer;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_fk")
    private DriverDO driver;


    public CarDO()
    {}


    public CarDO(Boolean convertible, CarDO.EngineType engineType, String licensePlate, ManufacturerDO manufacturer, Float rating, Short seatCount)
    {
        this.convertible = convertible;
        this.engineType = engineType;
        this.licensePlate = licensePlate;
        this.manufacturer = manufacturer;
        this.rating = rating;
        this.seatCount = seatCount;
    }


    public Long getId()
    {
        return id;
    }


    public void setId(Long id)
    {
        this.id = id;
    }


    public Boolean getConvertible()
    {
        return convertible;
    }


    public void setConvertible(Boolean convertible)
    {
        this.convertible = convertible;
    }


    public EngineType getEngineType()
    {
        return engineType;
    }


    public void setEngineType(EngineType engineType)
    {
        this.engineType = engineType;
    }


    public ManufacturerDO getManufacturer()
    {
        return manufacturer;
    }


    public void setManufacturer(ManufacturerDO manufacturer)
    {
        this.manufacturer = manufacturer;
    }


    public String getLicensePlate()
    {
        return licensePlate;
    }


    public void setLicensePlate(String licensePlate)
    {
        this.licensePlate = licensePlate;
    }


    public Short getSeatCount()
    {
        return seatCount;
    }


    public void setSeatCount(Short seatCount)
    {
        this.seatCount = seatCount;
    }


    public Float getRating()
    {
        return rating;
    }


    public void setRating(Float rating)
    {
        this.rating = rating;
    }


    public DriverDO getDriver()
    {
        return driver;
    }


    public void setDriver(DriverDO driver)
    {
        this.driver = driver;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CarDO carDO = (CarDO) o;
        return Objects.equals(id, carDO.id)
            &&
            Objects.equals(dateCreated, carDO.dateCreated) &&
            Objects.equals(licensePlate, carDO.licensePlate) &&
            Objects.equals(seatCount, carDO.seatCount) &&
            Objects.equals(convertible, carDO.convertible) &&
            Objects.equals(rating, carDO.rating) &&
            engineType == carDO.engineType &&
            Objects.equals(manufacturer, carDO.manufacturer);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(id, dateCreated, licensePlate, seatCount, convertible, rating, engineType, manufacturer);
    }

    public enum EngineType
    {
        ELECTRIC, GAS, HYBRID
    }

}
