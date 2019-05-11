package org.poc.service.car;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.poc.dataaccessobject.CarRepository;
import org.poc.domainobject.CarDO;
import org.poc.exception.ConstraintsViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import org.poc.exception.EntityNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCarServiceTest
{

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private DefaultCarService carService;


    @BeforeClass
    public static void setUp()
    {
        MockitoAnnotations.initMocks(CarService.class);
    }


    @Test
    public void create() throws ConstraintsViolationException
    {
        CarDO car = new CarDO();
        when(carRepository.save(any(CarDO.class))).thenReturn(car);
        carService.create(car);
        verify(carRepository, times(1)).save(car);
    }


    @Test
    public void create_alreadyStored()
    {
        CarDO car = new CarDO();
        when(carRepository.save(any(CarDO.class))).thenThrow(new DataIntegrityViolationException("Exception thrown"));

        assertThatThrownBy(() -> carService.create(car))
            .isInstanceOf(ConstraintsViolationException.class)
            .hasMessageContaining("Car with Id 'null' already found with error: Exception thrown");
    }


    @Test
    public void find() throws EntityNotFoundException
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        carService.find(car.getId());

        verify(carRepository, times(1)).findById(car.getId());
    }


    @Test
    public void find_notFound()
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.find(car.getId()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Car with Id 'null' not found");
    }


    @Test
    public void delete() throws EntityNotFoundException
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        doNothing().when(carRepository).delete(any(CarDO.class));
        carService.delete(car.getId());

        verify(carRepository, times(1)).findById(car.getId());
        verify(carRepository, times(1)).delete(car);
    }


    @Test
    public void delete_notFound()
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.delete(car.getId()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Car with Id 'null' not found");
    }


    @Test
    public void update() throws EntityNotFoundException
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(carRepository.save(any(CarDO.class))).thenReturn(car);
        carService.update(car);

        verify(carRepository, times(1)).findById(car.getId());
        verify(carRepository, times(1)).save(car);
    }


    @Test
    public void update_notFound()
    {
        CarDO car = new CarDO();
        when(carRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.update(car))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Car with Id 'null' not found");
    }
}
