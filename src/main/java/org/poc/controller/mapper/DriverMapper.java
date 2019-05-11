package org.poc.controller.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.poc.datatransferobject.CarDTO;
import org.poc.datatransferobject.DriverDTO;
import org.poc.domainobject.CarDO;
import org.poc.domainobject.DriverDO;
import org.poc.domainvalue.GeoCoordinate;

public class DriverMapper
{
    public static DriverDO makeDriverDO(DriverDTO driverDTO)
    {
        DriverDO driverDO = new DriverDO(driverDTO.getUsername(), driverDTO.getPassword());
        driverDO.setId(driverDTO.getId());
        driverDO.setOnlineStatus(driverDTO.getOnlinestatus());
        return driverDO;
    }


    public static DriverDTO makeDriverDTO(DriverDO driverDO)
    {
        DriverDTO.DriverDTOBuilder driverDTOBuilder =
            DriverDTO
                .newBuilder()
                .setId(driverDO.getId())
                .setPassword(driverDO.getPassword())
                .setUsername(driverDO.getUsername())
                .setOnlineStatus(driverDO.getOnlineStatus());

        GeoCoordinate coordinate = driverDO.getCoordinate();
        if (coordinate != null)
        {
            driverDTOBuilder.setCoordinate(coordinate);
        }
        CarDO carDO = driverDO.getCar();
        if (carDO != null)
        {
            CarDTO carDTO = new CarDTO();
            carDTO.setId(carDO.getId());
            driverDTOBuilder.setCarDTO(carDTO);
        }

        return driverDTOBuilder.createDriverDTO();
    }


    public static List<DriverDTO> makeDriverDTOList(Collection<DriverDO> drivers)
    {
        return drivers
            .stream()
            .map(DriverMapper::makeDriverDTO)
            .collect(Collectors.toList());
    }
}
