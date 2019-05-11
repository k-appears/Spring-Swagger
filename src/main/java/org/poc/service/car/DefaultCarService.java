package org.poc.service.car;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import org.poc.dataaccessobject.CarRepository;
import org.poc.domainobject.CarDO;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;

@Service
public class DefaultCarService implements CarService
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCarService.class);

    private final CarRepository carRepository;


    public DefaultCarService(final CarRepository carRepository)
    {
        this.carRepository = carRepository;
    }


    @Override
    public CarDO find(Long carId) throws EntityNotFoundException
    {
        return carRepository
            .findById(carId)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Car with Id '%s' not found", carId)));
    }


    @Override
    public CarDO create(CarDO carDO) throws ConstraintsViolationException
    {
        try
        {
            return carRepository.save(carDO);
        }
        catch (DataIntegrityViolationException dive)
        {
            throw new ConstraintsViolationException(
                String.format("Car with Id '%s' already found with error: %s", carDO.getId(), dive.getMessage()));
        }

    }


    @Override
    public void update(CarDO carDoInput) throws EntityNotFoundException
    {
        CarDO carDoFound =
            carRepository
                .findById(carDoInput.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Car with Id '%s' not found", carDoInput.getId())));
        carDoFound.setConvertible(carDoInput.getConvertible());
        carDoFound.setEngineType(carDoInput.getEngineType());
        carDoFound.setLicensePlate(carDoInput.getLicensePlate());
        carDoFound.setManufacturer(carDoInput.getManufacturer());
        carDoFound.setRating(carDoInput.getRating());
        carDoFound.setSeatCount(carDoInput.getSeatCount());

        carRepository.save(carDoFound);
    }


    @Override
    public void delete(Long carId) throws EntityNotFoundException
    {
        CarDO carDoFound =
            carRepository
                .findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Car with Id '%d' not found", carId)));
        carRepository.delete(carDoFound);
    }

}
