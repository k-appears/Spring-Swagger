package org.poc;

import java.util.Arrays;

import org.poc.datatransferobject.CarDTO;
import org.poc.domainobject.CarDO;
import org.poc.domainobject.ManufacturerDO;

public class Utils
{
    public static CarDTO copyCarDTOUpdated(CarDTO carDTO)
    {
        CarDTO car =
            new CarDTO(
                !carDTO.getConvertible(),
                differentEngineType(carDTO.getEngineType()),
                carDTO.getLicensePlate() + "Random",
                new ManufacturerDO(carDTO.getManufacturer().getManufacturer() + "Random"),
                carDTO.getRating() + 1F,
                (short) (carDTO.getSeatCount() + (short) 1));
        car.setId(carDTO.getId());
        return car;
    }


    public static CarDTO createCarDTO()
    {
        return new CarDTO(Boolean.FALSE, CarDO.EngineType.GAS, "licensePlate", new ManufacturerDO("Manufacturer"), 1.0F, (short) 5);
    }


    private static CarDO.EngineType differentEngineType(CarDO.EngineType engineType)
    {
        return Arrays
            .stream(CarDO.EngineType.values()).filter(et -> !et.equals(engineType)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No engine type different than " + engineType.toString()));
    }
}
