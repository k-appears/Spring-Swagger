package org.poc.controller;

import org.poc.controller.mapper.DriverMapper;
import org.poc.datatransferobject.CarDTO;
import org.poc.domainobject.CarDO;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;
import org.poc.service.car.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * All operations with a car will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("v1/cars")
public class CarController
{
    private final CarService carService;


    @Autowired
    public CarController(final CarService carService)
    {
        this.carService = carService;
    }


    @GetMapping("/{carId}")
    public CarDTO getCar(@PathVariable long carId) throws EntityNotFoundException
    {
        return createCarDTO(carService.find(carId));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarDTO createCar(@Valid @RequestBody CarDTO carDTO) throws ConstraintsViolationException
    {
        CarDO carDO = createCarDO(carDTO);
        CarDO newCar = carService.create(carDO);
        return createCarDTO(newCar);
    }


    @DeleteMapping("/{carId}")
    public void deleteCar(@PathVariable long carId) throws EntityNotFoundException
    {
        carService.delete(carId);
    }


    @PutMapping("/{carId}")
    public void update(@Valid @RequestBody CarDTO carDTO) throws EntityNotFoundException
    {
        CarDO carDO = createCarDO(carDTO);
        carService.update(carDO);
    }


    private CarDTO createCarDTO(CarDO car)
    {
        CarDTO carDTO =
            new CarDTO(
                car.getConvertible(),
                car.getEngineType(),
                car.getLicensePlate(),
                car.getManufacturer(),
                car.getRating(),
                car.getSeatCount());
        carDTO.setId(car.getId());
        if (car.getDriver() != null)
        {
            carDTO.setDriver(DriverMapper.makeDriverDTO(car.getDriver()));
        }
        return carDTO;
    }


    CarDO createCarDO(CarDTO car)
    {
        CarDO carDO =
            new CarDO(
                car.getConvertible(),
                car.getEngineType(),
                car.getLicensePlate(),
                car.getManufacturer(),
                car.getRating(),
                car.getSeatCount());
        carDO.setId(car.getId());
        if (car.getDriver() != null)
        {
            carDO.setDriver(DriverMapper.makeDriverDO(car.getDriver()));
        }
        return carDO;
    }
}
