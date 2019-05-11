package org.poc.service.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.poc.dataaccessobject.DriverRepository;
import org.poc.domainobject.CarDO;
import org.poc.domainvalue.OnlineStatus;
import org.poc.exception.AssignedDriverNotOnlineException;
import org.poc.exception.CarAlreadyInUseException;
import org.springframework.data.domain.Example;

import com.google.common.collect.ImmutableMap;
import org.poc.dataaccessobject.CarRepository;
import org.poc.domainobject.DriverDO;
import org.poc.domainobject.ManufacturerDO;
import org.poc.exception.EntityNotFoundException;

@RunWith(MockitoJUnitRunner.class)

public class DefaultDriverServiceTest
{

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private DefaultDriverService driverService;
    private static final Long DRIVER_ID = 10L;
    private static final Long CAR_ID = 20L;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Captor
    ArgumentCaptor<Example<DriverDO>> argumentExampleDriver;
    @Captor
    ArgumentCaptor<Example<CarDO>> argumentExampleCar;


    @BeforeClass
    public static void setUp()
    {
        MockitoAnnotations.initMocks(DriverService.class);
    }


    @Test
    public void selectCarByDriver_NotFoundCar()
    {
        when(driverRepository.findById(any())).thenReturn(Optional.of(new DriverDO(USERNAME, PASSWORD)));
        when(carRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.selectCarByDriver(DRIVER_ID, CAR_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Could not find car with id: " + CAR_ID);
    }


    @Test
    public void selectCarByDriver_NotFoundDriver()
    {
        when(driverRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.selectCarByDriver(DRIVER_ID, CAR_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Could not find driver with id: " + DRIVER_ID);
    }


    @Test
    public void selectCarByDriver_twice_SecondDifferentDriver() throws CarAlreadyInUseException, EntityNotFoundException, AssignedDriverNotOnlineException
    {
        DriverDO driver1 = new DriverDO(USERNAME, PASSWORD);
        driver1.setId(DRIVER_ID);
        driver1.setOnlineStatus(OnlineStatus.ONLINE);
        DriverDO driver2 = new DriverDO(USERNAME + 2, PASSWORD + 2);
        driver2.setId(DRIVER_ID + 2);
        driver2.setOnlineStatus(OnlineStatus.ONLINE);
        CarDO carDO = new CarDO();
        carDO.setId(CAR_ID);

        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(driver1));
        when(driverRepository.findById(DRIVER_ID + 2)).thenReturn(Optional.of(driver2));
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.of(carDO));
        when(driverRepository.findByCar(carDO)).thenReturn(null).thenReturn(driver1);

        driverService.selectCarByDriver(DRIVER_ID, CAR_ID);

        assertThatThrownBy(() -> driverService.selectCarByDriver(DRIVER_ID + 2, CAR_ID))
            .isInstanceOf(CarAlreadyInUseException.class)
            .hasMessageContaining("Car with Id '" + CAR_ID + "' is already assigned to driver " + DRIVER_ID);
    }


    @Test
    public void selectCarByDriver_twice_SecondSameDriver() throws CarAlreadyInUseException, EntityNotFoundException, AssignedDriverNotOnlineException
    {
        DriverDO driver = new DriverDO(USERNAME, PASSWORD);
        driver.setId(DRIVER_ID);
        driver.setOnlineStatus(OnlineStatus.ONLINE);
        CarDO carDO = new CarDO();
        carDO.setId(CAR_ID);

        when(driverRepository.findById(any())).thenReturn(Optional.of(driver));
        when(carRepository.findById(any())).thenReturn(Optional.of(carDO));
        when(driverRepository.findByCar(carDO)).thenReturn(null).thenReturn(driver);

        driverService.selectCarByDriver(DRIVER_ID, CAR_ID);

        driverService.selectCarByDriver(DRIVER_ID, CAR_ID);

        ArgumentCaptor<CarDO> argument = ArgumentCaptor.forClass(CarDO.class);
        verify(carRepository, times(2)).save(argument.capture());
        List<CarDO> arguments = argument.getAllValues();
        assertThat(arguments).containsExactly(carDO, carDO);
        assertThat(arguments.get(0).getDriver()).isEqualTo(driver);
    }


    @Test
    public void selectCarByDriver() throws CarAlreadyInUseException, EntityNotFoundException, AssignedDriverNotOnlineException
    {
        DriverDO driverDO = new DriverDO(USERNAME, PASSWORD);
        driverDO.setOnlineStatus(OnlineStatus.ONLINE);
        CarDO carDO = new CarDO();
        when(driverRepository.findById(any())).thenReturn(Optional.of(driverDO));
        when(carRepository.findById(any())).thenReturn(Optional.of(carDO));
        when(driverRepository.findByCar(any())).thenReturn(null);

        driverService.selectCarByDriver(DRIVER_ID, CAR_ID);

        ArgumentCaptor<CarDO> argument = ArgumentCaptor.forClass(CarDO.class);
        verify(carRepository, times(1)).save(argument.capture());
        assertThat(argument.getValue().getDriver()).isEqualTo(driverDO);
    }


    @Test
    public void deselectCarByDriver_NotFoundDriver()
    {
        when(driverRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.deselectCarByDriver(DRIVER_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Could not find driver with id: " + DRIVER_ID);
    }


    @Test
    public void deSelectCarByDriver() throws EntityNotFoundException
    {
        DriverDO driverDO = new DriverDO(USERNAME, PASSWORD);
        CarDO carDO = new CarDO();
        carDO.setId(CAR_ID);
        carDO.setDriver(driverDO);

        when(driverRepository.findById(any())).thenReturn(Optional.of(driverDO));
        driverService.deselectCarByDriver(DRIVER_ID);

        ArgumentCaptor<DriverDO> argument = ArgumentCaptor.forClass(DriverDO.class);
        verify(driverRepository, times(1)).save(argument.capture());
        assertThat(argument.getValue().getCar()).isNull();
    }


    @Test
    public void search_Username()
    {
        String username = "AnyName";
        Map<String, String> parameters = ImmutableMap.of("username", username);

        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(driverRepository, times(1)).findAll(argumentExampleDriver.capture());
        assertThat(argumentExampleDriver.getValue().getProbe().getUsername()).isEqualTo(username);
        assertThat(argumentExampleDriver.getValue().getProbe().getId()).isNull();
    }


    @Test
    public void search_InvalidOnlineStatus()
    {
        String onlineStatus = "AnyStatus";
        Map<String, String> parameters = ImmutableMap.of("onlineStatus", onlineStatus);

        assertThatThrownBy(() -> driverService.searchByFieldsOfCarAndDriver(parameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AnyStatus");
    }


    @Test
    public void search_UsernameAndOnlineStatus()
    {
        String username = "AnyName";
        String onlineStatus = OnlineStatus.OFFLINE.toString();
        Map<String, String> parameters = ImmutableMap.of("username", username, "onlineStatus", onlineStatus);

        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(driverRepository, times(1)).findAll(argumentExampleDriver.capture());
        assertThat(argumentExampleDriver.getValue().getProbe().getUsername()).isEqualTo(username);
        assertThat(argumentExampleDriver.getValue().getProbe().getOnlineStatus()).isEqualTo(OnlineStatus.OFFLINE);
        assertThat(argumentExampleDriver.getValue().getProbe().getId()).isNull();
    }


    @Test
    public void search_Manufacturer()
    {
        String manufactureName = "AnyManufactureName";
        ManufacturerDO manufacturer = new ManufacturerDO(manufactureName);
        Map<String, String> parameters = ImmutableMap.of("manufacturer", manufacturer.getManufacturer());
        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(carRepository, times(1)).findAll(argumentExampleCar.capture());
        assertThat(argumentExampleCar.getValue().getProbe().getManufacturer().getManufacturer()).isEqualTo(manufactureName);
        assertThat(argumentExampleCar.getValue().getProbe().getId()).isNull();
    }


    @Test
    public void search_Rating()
    {
        Float rating = 50.0F;
        Map<String, String> parameters = ImmutableMap.of("rating", rating.toString());
        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(carRepository, times(1)).findAll(argumentExampleCar.capture());
        assertThat(argumentExampleCar.getValue().getProbe().getRating()).isEqualTo(rating);
        assertThat(argumentExampleCar.getValue().getProbe().getId()).isNull();
    }


    @Test
    public void search_RateAndManufacture()
    {
        Float rating = 50.0F;
        String manufactureName = "AnyManufactureName";
        ManufacturerDO manufacturer = new ManufacturerDO(manufactureName);
        Map<String, String> parameters = ImmutableMap.of("rating", rating.toString(), "manufacturer", manufacturer.getManufacturer());

        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(carRepository, times(1)).findAll(argumentExampleCar.capture());
        assertThat(argumentExampleCar.getValue().getProbe().getRating()).isEqualTo(rating);
        assertThat(argumentExampleCar.getValue().getProbe().getManufacturer().getManufacturer()).isEqualTo(manufactureName);
        assertThat(argumentExampleCar.getValue().getProbe().getId()).isNull();
    }


    @Test
    public void search_LicensePlateAndRatingAndUsernameAndOnlineStatus()
    {
        String username = "AnyName";
        String onlineStatus = OnlineStatus.OFFLINE.toString();
        String manufactureName = "AnyManufactureName";
        ManufacturerDO manufacturer = new ManufacturerDO(manufactureName);
        Float rating = 50.0F;
        Map<String, String> parameters =
            ImmutableMap.of("username", username, "onlineStatus", onlineStatus, "rating", rating.toString(), "manufacturer", manufacturer.getManufacturer());

        driverService.searchByFieldsOfCarAndDriver(parameters);

        verify(carRepository, times(1)).findAll(argumentExampleCar.capture());
        assertThat(argumentExampleCar.getValue().getProbe().getRating()).isEqualTo(rating);
        assertThat(argumentExampleCar.getValue().getProbe().getManufacturer().getManufacturer()).isEqualTo(manufactureName);
        verify(driverRepository, times(1)).findAll(argumentExampleDriver.capture());
        assertThat(argumentExampleDriver.getValue().getProbe().getUsername()).isEqualTo(username);
        assertThat(argumentExampleDriver.getValue().getProbe().getOnlineStatus()).isEqualTo(OnlineStatus.OFFLINE);
    }
}
