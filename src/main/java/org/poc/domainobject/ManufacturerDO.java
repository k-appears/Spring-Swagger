package org.poc.domainobject;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ManufacturerDO
{
    @Column(name = "manufacturer")
    private String manufacturer;


    protected ManufacturerDO()
    {}


    public ManufacturerDO(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }


    public String getManufacturer()
    {
        return manufacturer;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ManufacturerDO that = (ManufacturerDO) o;
        return Objects.equals(manufacturer, that.manufacturer);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(manufacturer);
    }
}
