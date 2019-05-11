package org.poc.service.car;

import org.poc.domainobject.CarDO;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;

public interface CarService
{

    CarDO find(Long carId) throws EntityNotFoundException;


    CarDO create(CarDO carDO) throws ConstraintsViolationException;


    void delete(Long carId) throws EntityNotFoundException;


    void update(CarDO carDO) throws EntityNotFoundException;

}
