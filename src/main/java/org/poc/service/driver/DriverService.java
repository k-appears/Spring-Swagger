package org.poc.service.driver;

import org.poc.domainobject.DriverDO;
import org.poc.domainvalue.OnlineStatus;
import org.poc.exception.AssignedDriverNotOnlineException;
import org.poc.exception.CarAlreadyInUseException;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface DriverService
{

    DriverDO find(Long driverId) throws EntityNotFoundException;


    DriverDO create(DriverDO driverDO) throws ConstraintsViolationException;


    void delete(Long driverId) throws EntityNotFoundException;


    void updateLocation(long driverId, double longitude, double latitude) throws EntityNotFoundException;


    List<DriverDO> find(OnlineStatus onlineStatus);


    void selectCarByDriver(Long driverId, Long carId) throws EntityNotFoundException, CarAlreadyInUseException, AssignedDriverNotOnlineException;


    void deselectCarByDriver(Long driverId) throws EntityNotFoundException;


    List<DriverDO> searchByFieldsOfCarAndDriver(Map<String, String> params);
}
