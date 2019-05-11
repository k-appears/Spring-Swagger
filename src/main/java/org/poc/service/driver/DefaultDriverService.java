package org.poc.service.driver;

import org.poc.dataaccessobject.CarRepository;
import org.poc.dataaccessobject.DriverRepository;
import org.poc.domainobject.CarDO;
import org.poc.domainobject.DriverDO;
import org.poc.domainobject.ManufacturerDO;
import org.poc.domainvalue.GeoCoordinate;
import org.poc.domainvalue.OnlineStatus;
import org.poc.exception.AssignedDriverNotOnlineException;
import org.poc.exception.CarAlreadyInUseException;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service to encapsulate the link between DAO and controller and to have business logic for some driver specific things.
 * <p/>
 */
@Service
public class DefaultDriverService implements DriverService
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDriverService.class);

    private final DriverRepository driverRepository;
    private final CarRepository carRepository;


    public DefaultDriverService(DriverRepository driverRepository, CarRepository carRepository)
    {
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
    }


    /**
     * Selects a driver by id.
     *
     * @param driverId
     * @return found driver
     * @throws EntityNotFoundException if no driver with the given id was found.
     */
    @Override
    public DriverDO find(Long driverId) throws EntityNotFoundException
    {
        return findDriver(driverId);
    }


    /**
     * Creates a new driver.
     *
     * @param driverDO
     * @return
     * @throws ConstraintsViolationException if a driver already exists with the given username, ... .
     */
    @Override
    public DriverDO create(DriverDO driverDO) throws ConstraintsViolationException
    {
        DriverDO driver;
        try
        {
            driver = driverRepository.save(driverDO);
        }
        catch (DataIntegrityViolationException e)
        {
            LOG.warn("ConstraintsViolationException while creating a driver: {}", driverDO, e);
            throw new ConstraintsViolationException(e.getMessage());
        }
        return driver;
    }


    /**
     * Deletes an existing driver by id.
     *
     * @param driverId
     * @throws EntityNotFoundException if no driver with the given id was found.
     */
    @Override
    @Transactional
    public void delete(Long driverId) throws EntityNotFoundException
    {
        DriverDO driverDO = findDriver(driverId);
        driverDO.setDeleted(true);
    }


    /**
     * Update the location for a driver.
     *
     * @param driverId
     * @param longitude
     * @param latitude
     * @throws EntityNotFoundException
     */
    @Override
    @Transactional
    public void updateLocation(long driverId, double longitude, double latitude) throws EntityNotFoundException
    {
        DriverDO driverDO = findDriver(driverId);
        driverDO.setCoordinate(new GeoCoordinate(latitude, longitude));
    }


    /**
     * Find all drivers by online state.
     *
     * @param onlineStatus
     */
    @Override
    public List<DriverDO> find(OnlineStatus onlineStatus)
    {
        return driverRepository.findByOnlineStatus(onlineStatus);
    }


    /**
     * Assign driver to a car
     *
     * @param driverId
     * @param carId
     * @throws EntityNotFoundException
     * @throws CarAlreadyInUseException
     * @throws AssignedDriverNotOnlineException
     */
    @Override
    public void selectCarByDriver(Long driverId, Long carId) throws EntityNotFoundException, CarAlreadyInUseException, AssignedDriverNotOnlineException
    {
        DriverDO driverDO = findDriver(driverId);
        CarDO carDO = findCar(carId);
        assert driverDO != null && carDO != null;
        if (driverDO.getOnlineStatus() != OnlineStatus.ONLINE)
        {
            throw new AssignedDriverNotOnlineException("Driver '" + driverId + "' not ONLINE");
        }

        DriverDO driverWithCar = driverRepository.findByCar(carDO);
        if (driverWithCar != null && !driverDO.equals(driverWithCar))
        {
            throw new CarAlreadyInUseException("Car with Id '" + carDO.getId() + "' is already assigned to driver " + driverWithCar.getId());
        }
        if (driverDO.equals(driverWithCar))
        {
            LOG.info("No modification: Car with Id '" + carDO.getId() + "' is already assigned to driver " + driverWithCar.getId());
        }
        carDO.setDriver(driverDO);
        carRepository.save(carDO);
    }


    /**
     * Remove car assignation of a driver
     *
     * @param driverId
     * @throws EntityNotFoundException
     */
    @Override
    public void deselectCarByDriver(Long driverId) throws EntityNotFoundException
    {
        DriverDO driver = findDriver(driverId);
        CarDO carDO = driver.getCar();
        if (carDO != null)
        {
            driver.setCar(null);
            carDO.setDriver(null);
        }

        driverRepository.save(driver);
    }


    /**
     * Search driver by car and driver attributes
     *
     * @param params
     * @return
     */
    @Override
    public List<DriverDO> searchByFieldsOfCarAndDriver(Map<String, String> params)
    {
        DriverDO driverDO = fromUsernameOnlineStatus(params);
        Example<DriverDO> exampleDriver =
            Example
                .of(
                    driverDO, ExampleMatcher
                        .matching()
                        .withIgnoreNullValues()
                        .withIgnorePaths("id", "dateCreated")
                        .withIgnoreCase()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        CarDO carDO = fromLicensePlateRatingSeatCountEngineTypeManufacture(params);
        Example<CarDO> exampleCar =
            Example
                .of(
                    carDO, ExampleMatcher
                        .matching()
                        .withIgnoreNullValues()
                        .withIgnorePaths("id", "dateCreated")
                        .withIgnoreCase()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        List<DriverDO> filteredDrivers = (List<DriverDO>) driverRepository.findAll(exampleDriver);
        List<CarDO> filteredCars = (List<CarDO>) carRepository.findAll(exampleCar);
        List<DriverDO> filteredDriversAssignedToCars = filteredCars.stream().map(CarDO::getDriver).filter(Objects::nonNull).collect(Collectors.toList());

        //Intersecting two lists
        return filteredDrivers.stream().filter(filteredDriversAssignedToCars::contains).collect(Collectors.toList());
    }


    private DriverDO findDriver(Long driverId) throws EntityNotFoundException
    {
        return driverRepository
            .findById(driverId)
            .orElseThrow(() -> new EntityNotFoundException("Could not find driver with id: " + driverId));
    }


    private CarDO findCar(Long carId) throws EntityNotFoundException
    {
        return carRepository
            .findById(carId)
            .orElseThrow(() -> new EntityNotFoundException("Could not find car with id: " + carId));
    }


    private DriverDO fromUsernameOnlineStatus(Map<String, String> fields)
    {
        DriverDO driverDO = new DriverDO(fields.get("username"), null);
        driverDO.setOnlineStatus(null);
        if (fields.containsKey("onlineStatus"))
        {
            driverDO.setOnlineStatus(OnlineStatus.valueOf(fields.get("onlineStatus")));
        }
        driverDO.setDeleted(null);
        return driverDO;
    }


    private CarDO fromLicensePlateRatingSeatCountEngineTypeManufacture(Map<String, String> fields)
    {
        Boolean convertible = fields.containsKey("convertible") ? Boolean.valueOf(fields.get("convertible")) : null;
        CarDO.EngineType engineType = fields.containsKey("engineType") ? CarDO.EngineType.valueOf(fields.get("engineType")) : null;
        ManufacturerDO manufacturer = fields.containsKey("manufacturer") ? new ManufacturerDO(fields.get("manufacturer")) : null;
        Float rating = fields.containsKey("rating") ? Float.valueOf(fields.get("rating")) : null;
        Short seatCount = fields.containsKey("seatCount") ? Short.valueOf(fields.get("seatCount")) : null;

        return new CarDO(convertible, engineType, fields.get("licensePlate"), manufacturer, rating, seatCount);
    }

}
