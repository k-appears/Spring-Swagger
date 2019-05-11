package org.poc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.poc.datatransferobject.CarDTO;
import org.poc.datatransferobject.DriverDTO;
import org.poc.domainobject.CarDO;
import org.poc.domainobject.DriverDO;
import org.poc.domainobject.ManufacturerDO;
import org.poc.domainvalue.OnlineStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Sql({"classpath:drop_schema.sql", "classpath:schema.sql", "classpath:data.sql"})
@SpringBootTest(classes = ServerApplicantTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServerApplicantTestApplicationTests
{

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private String urlCars;
    private String urlDrivers;


    @Before
    public void init()
    {
        restTemplate =
            restTemplateBuilder
                .basicAuthorization("user", "password")
                .errorHandler(new DefaultResponseErrorHandler()
                {
                    protected boolean hasError(HttpStatus statusCode)
                    {
                        return false;
                    }
                }).build();
        urlCars = "http://localhost:" + port + "/v1/cars/";
        urlDrivers = "http://localhost:" + port + "/v1/drivers/";
    }


    @Test
    public void getCar_FromInitData()
    {
        ResponseEntity<CarDTO> responseEntity = restTemplate.getForEntity(urlCars + "{carId}", CarDTO.class, "1");

        int status = responseEntity.getStatusCodeValue();
        CarDTO carDTO = responseEntity.getBody();

        assertThat(HttpStatus.OK.value()).isEqualTo(status);

        assertThat(carDTO).isNotNull();
        // Data loaded from data.sql
        assertThat(carDTO.getId()).isEqualTo(1L);
        assertThat(carDTO.getConvertible()).isEqualTo(Boolean.TRUE);
        assertThat(carDTO.getEngineType()).isEqualTo(CarDO.EngineType.GAS);
        assertThat(carDTO.getLicensePlate()).isEqualTo("EU-001");
        assertThat(carDTO.getManufacturer().getManufacturer()).isEqualTo("MANUFACTURER-X");
        assertThat(carDTO.getRating()).isEqualTo(5.0F);
        assertThat(carDTO.getSeatCount()).isEqualTo((short) 4);

    }


    @Test
    public void createCar()
    {
        CarDTO carDTO = createCarDTO("licensePlate_createCar");
        ResponseEntity<CarDTO> postResponse = restTemplate.postForEntity(urlCars, carDTO, CarDTO.class);

        assertThat(postResponse).isNotNull();
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(carDTO).isEqualToIgnoringGivenFields(postResponse.getBody(), "id");
        assertThat(postResponse.getBody().getId()).isEqualTo(100L); //initial value is 100
    }


    @Test
    public void createCar_sameLicensePlate()
    {
        CarDTO carDTO1 = createCarDTO("licensePlate_samePlate");
        restTemplate.postForEntity(urlCars, carDTO1, CarDTO.class);

        CarDTO carDTO2 = createCarDTO("licensePlate_samePlate");
        ResponseEntity<String> postResponse = restTemplate.exchange(urlCars, HttpMethod.POST, new HttpEntity<>(carDTO2), String.class);

        int status = postResponse.getStatusCodeValue();
        assertThat(status).isEqualTo(HttpStatus.CONFLICT.value());
    }


    @Test
    public void deleteCar()
    {
        // loaded from data.sql
        CarDTO carDTO = restTemplate.getForObject(urlCars + "{carId}", CarDTO.class, "1");
        assertThat(carDTO).isNotNull();
        assertThat(carDTO.getId()).isEqualTo(1L);

        restTemplate.delete(urlCars + "{carId}", "1");
        ResponseEntity<String> responseEntity = restTemplate.exchange(urlCars + "{carId}", HttpMethod.DELETE, new HttpEntity<>(""), String.class, "1");

        int status = responseEntity.getStatusCodeValue();
        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody()).isEqualTo("Car with Id '1' not found");
    }


    @Test
    public void deleteCar_NotExists()
    {
        ResponseEntity<String> responseEntityNotFound = restTemplate.exchange(urlCars + "{carId}", HttpMethod.GET, new HttpEntity<>(""), String.class, Integer.MAX_VALUE);
        int statusNotFound = responseEntityNotFound.getStatusCodeValue();
        assertThat(statusNotFound).isEqualTo(HttpStatus.NOT_FOUND.value());

        ResponseEntity<String> responseEntity = restTemplate.exchange(urlCars + "{carId}", HttpMethod.DELETE, new HttpEntity<>(""), String.class, Integer.MAX_VALUE);

        int status = responseEntity.getStatusCodeValue();
        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody()).isEqualTo("Car with Id '" + Integer.MAX_VALUE + "' not found");
    }


    @Test
    public void updateCar()
    {
        // loaded from data.sql
        CarDTO input = restTemplate.getForObject(urlCars + "{carId}", CarDTO.class, "1");
        assertThat(input).isNotNull();
        CarDTO carDTOModified = Utils.copyCarDTOUpdated(input);

        ResponseEntity<CarDTO> responseEntity = restTemplate.exchange(urlCars + "{carId}", HttpMethod.PUT, new HttpEntity<>(carDTOModified), CarDTO.class, "1");

        int status = responseEntity.getStatusCodeValue();
        assertThat(status).isEqualTo(HttpStatus.OK.value());

        CarDTO output = restTemplate.getForObject(urlCars + "{carId}", CarDTO.class, "1");

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(input.getId());
        assertThat(output.getLicensePlate()).isNotEqualTo(input.getLicensePlate());
        assertThat(output.getConvertible()).isNotEqualTo(input.getConvertible());
        assertThat(output.getEngineType()).isNotEqualTo(input.getEngineType());
        assertThat(output.getManufacturer()).isNotEqualTo(input.getManufacturer());
        assertThat(output.getRating()).isNotEqualTo(input.getRating());
        assertThat(output.getSeatCount()).isNotEqualTo(input.getSeatCount());
    }


    @Test
    public void updateCar_NotExists()
    {
        ResponseEntity<String> responseEntityNotFound = restTemplate.exchange(urlCars + "{carId}", HttpMethod.GET, new HttpEntity<>(""), String.class, Long.MAX_VALUE);
        int statusNotFound = responseEntityNotFound.getStatusCodeValue();
        assertThat(statusNotFound).isEqualTo(HttpStatus.NOT_FOUND.value());

        CarDTO carDTO = new CarDTO(null, null, "FAKE_LICENSE", null, null, null);
        carDTO.setId(Long.MAX_VALUE);

        ResponseEntity<String> responseEntity = restTemplate.exchange(urlCars + "{carId}", HttpMethod.PUT, new HttpEntity<>(carDTO), String.class, Long.MAX_VALUE);

        int status = responseEntity.getStatusCodeValue();
        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody()).isEqualTo("Car with Id '" + Long.MAX_VALUE + "' not found");
    }


    @Test
    public void selectCarByDriver_carAlreadyAssigned()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("driverId", 40)
                .queryParam("carId", 1);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> responseEntity =
            restTemplate
                .exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        DriverDO driverDO_41 = restTemplate.getForObject(urlDrivers + "{driverId}", DriverDO.class, "41");
        assertThat(driverDO_41).isNotNull();
        assertThat(driverDO_41.getCar()).isNull();

        DriverDO driverDO_40 = restTemplate.getForObject(urlDrivers + "{driverId}", DriverDO.class, "40");
        assertThat(driverDO_40).isNotNull();
        assertThat(driverDO_40.getCar().getId()).isEqualTo(1);
    }


    @Test
    public void selectCarByDriver_parameterCarIdNotSent()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "select").queryParam("driverId", 41);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void selectCarByDriver_driverNotExists()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "select").queryParam("driverId", Integer.MAX_VALUE).queryParam("carId", 1);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void selectCarByDriver_carNotExists()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "select").queryParam("driverId", 41).queryParam("carId", Integer.MAX_VALUE);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void selectCarByDriver_offlineDriver()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "select").queryParam("driverId", 42).queryParam("carId", 1);
        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).contains("Driver '42' not ONLINE");
    }


    @Test
    public void selectCarByDriver_twice_DifferentSecondOfflineDriver()
    {
        CarDTO carDTO = createCarDTO("licensePlate_selectCar_twice");
        ResponseEntity<CarDTO> carResponse = restTemplate.postForEntity(urlCars, carDTO, CarDTO.class);

        DriverDTO driver1 = DriverDTO.newBuilder().setId(10L).setUsername("User1").setPassword("P1").setOnlineStatus(OnlineStatus.ONLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse1 = restTemplate.postForEntity(urlDrivers, driver1, DriverDTO.class);

        DriverDTO driver2 =
            DriverDTO.newBuilder().setId(20L).setUsername("User2").setPassword("P2").setOnlineStatus(OnlineStatus.OFFLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse2 = restTemplate.postForEntity(urlDrivers, driver2, DriverDTO.class);

        assertThat(carResponse.getBody()).isNotNull();
        assertThat(driverResponse1.getBody()).isNotNull();
        assertThat(driverResponse2.getBody()).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder1 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse1.getBody().getId());

        ResponseEntity<Void> responseEntity =
            restTemplate
                .exchange(
                    builder1.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        UriComponentsBuilder builder2 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse2.getBody().getId());

        ResponseEntity<String> responseEntity2 =
            restTemplate
                .exchange(
                    builder2.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    String.class);

        assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity2.getBody()).isEqualTo("Driver '2' not ONLINE");
    }


    @Test
    public void selectCarByDriver_twice_DifferentSecondOnlineDriver()
    {
        CarDTO carDTO = createCarDTO("licensePlate_selectCar_twice");
        ResponseEntity<CarDTO> carResponse = restTemplate.postForEntity(urlCars, carDTO, CarDTO.class);

        DriverDTO driver1 = DriverDTO.newBuilder().setId(10L).setUsername("User1").setPassword("P1").setOnlineStatus(OnlineStatus.ONLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse1 = restTemplate.postForEntity(urlDrivers, driver1, DriverDTO.class);

        DriverDTO driver2 = DriverDTO.newBuilder().setId(20L).setUsername("User2").setPassword("P2").setOnlineStatus(OnlineStatus.ONLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse2 = restTemplate.postForEntity(urlDrivers, driver2, DriverDTO.class);

        assertThat(carResponse.getBody()).isNotNull();
        assertThat(driverResponse1.getBody()).isNotNull();
        assertThat(driverResponse2.getBody()).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder1 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse1.getBody().getId());

        ResponseEntity<Void> responseEntity =
            restTemplate
                .exchange(
                    builder1.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        UriComponentsBuilder builder2 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse2.getBody().getId());

        ResponseEntity<String> responseEntity2 =
            restTemplate
                .exchange(
                    builder2.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    String.class);

        assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity2.getBody()).isEqualTo("Car with Id '" + carResponse.getBody().getId() + "' is already assigned to driver " + driverResponse1.getBody().getId());
    }


    @Test
    public void selectCarByDriver_twice_SameSecondOnlineDriver()
    {
        CarDTO carDTO = createCarDTO("licensePlate_selectCar_twice");
        ResponseEntity<CarDTO> carResponse = restTemplate.postForEntity(urlCars, carDTO, CarDTO.class);

        DriverDTO driver1 = DriverDTO.newBuilder().setId(10L).setUsername("User1").setPassword("P1").setOnlineStatus(OnlineStatus.ONLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse1 = restTemplate.postForEntity(urlDrivers, driver1, DriverDTO.class);

        assertThat(carResponse.getBody()).isNotNull();
        assertThat(driverResponse1.getBody()).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder1 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse1.getBody().getId());

        ResponseEntity<Void> responseEntity =
            restTemplate
                .exchange(
                    builder1.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        UriComponentsBuilder builder2 =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse1.getBody().getId());

        ResponseEntity<Void> responseEntity2 =
            restTemplate
                .exchange(
                    builder2.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class);

        assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void selectCarByDriver()
    {
        CarDTO carDTO = createCarDTO("licensePlate_selectCar");
        ResponseEntity<CarDTO> carResponse = restTemplate.postForEntity(urlCars, carDTO, CarDTO.class);

        DriverDTO driverDTO = DriverDTO.newBuilder().setId(20L).setUsername("User20").setPassword("Pass20").setOnlineStatus(OnlineStatus.ONLINE).createDriverDTO();
        ResponseEntity<DriverDTO> driverResponse = restTemplate.postForEntity(urlDrivers, driverDTO, DriverDTO.class);

        assertThat(carResponse.getBody()).isNotNull();
        assertThat(driverResponse.getBody()).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(urlDrivers + "select")
                .queryParam("carId", carResponse.getBody().getId())
                .queryParam("driverId", driverResponse.getBody().getId());

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> responseEntity =
            restTemplate
                .exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        DriverDO driverDO = restTemplate.getForObject(urlDrivers + "{driverId}", DriverDO.class, driverResponse.getBody().getId());
        assertThat(driverDO).isNotNull();
        assertThat(driverDO.getCar().getId()).isEqualTo(carResponse.getBody().getId());
    }


    private CarDTO createCarDTO(String licensePlate_selectCar)
    {
        return new CarDTO(Boolean.FALSE, CarDO.EngineType.ELECTRIC, licensePlate_selectCar, new ManufacturerDO("Manufacturer"), 1.0F, (short) 5);
    }


    @Test
    public void deselectCarByDriver_carNotAssigned()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "deselect").queryParam("driverId", 41);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void deselectCarByDriver_driverNotExists()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "deselect").queryParam("driverId", Integer.MAX_VALUE);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void deselectCarByDriver()
    {
        // Data loaded from data.sql
        CarDO carDOBefore = restTemplate.getForObject(urlCars + "{carId}", CarDO.class, 1);
        DriverDO driverDOBefore = restTemplate.getForObject(urlDrivers + "{driverId}", DriverDO.class, 40);
        assertThat(driverDOBefore).isNotNull();
        assertThat(carDOBefore).isNotNull();
        assertThat(driverDOBefore.getCar().getId()).isEqualTo(carDOBefore.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlDrivers + "deselect").queryParam("driverId", 40);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        CarDO carDO = restTemplate.getForObject(urlCars + "{carId}", CarDO.class, 1);
        assertThat(carDO).isNotNull();
        assertThat(carDO.getDriver()).isNull();
        DriverDO driverDO = restTemplate.getForObject(urlDrivers + "{driverId}", DriverDO.class, 40);
        assertThat(driverDO).isNotNull();
        assertThat(driverDO.getCar()).isNull();
    }


    @Test
    public void searchDriverBy_NoAssignedCarWithEngineTypeElectric()
    {
        // Data loaded from data.sql
        Map<String, String> parameters = ImmutableMap.of("engineType", "ELECTRIC");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>(parameters, headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(0);
    }


    @Test
    public void searchDriverBy_AssignedCarManufactureX()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"manufacturer\": \"MANUFACTURER-X\"}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).extracting("id").containsExactly(40L);
    }


    @Test
    public void searchDriverBy_ManufactureContaining()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"manufacturer\": \"MANU\"}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).extracting("id").containsExactlyInAnyOrder(40L, 42L);
    }


    @Test
    public void searchDriverBy_AttributeRepeated()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"manufacturer\": \"MANUFACTURER-\", \"manufacturer\": \"INVALID_NAME\"}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(0);
    }


    @Test
    public void searchDriveBy_NotValidFieldCriteria_ReturnsAll()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"id\": 1}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(2);
        assertThat(cars).extracting("id").containsExactlyInAnyOrder(40L, 42L);
    }


    @Test
    public void searchDrive_ByBooleanConvertible()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"convertible\": false}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(1);
        assertThat(cars).extracting("id").containsExactlyInAnyOrder(42L);
    }


    @Test
    public void searchDrive_ByEnumEngineType()
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>("{\"engineType\": \"" + CarDO.EngineType.HYBRID.name() + "\"}", headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(1);
        assertThat(cars).extracting("id").containsExactlyInAnyOrder(42L);
    }


    @Test
    public void search_LicensePlate_And_Manufacturer_And_Username_And_OnlineStatus() throws JsonProcessingException
    {
        // Data loaded from data.sql
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ImmutableMap<String, String> attributes = ImmutableMap.of("licensePlate", "EU-0", "manufacturer", "MAN", "username", "driver-", "onlineStatus", "ONLINE");
        String body = new ObjectMapper().writeValueAsString(attributes);
        ResponseEntity<List<CarDTO>> responseEntity =
            restTemplate
                .exchange(
                    urlDrivers + "search",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<List<CarDTO>>()
                    {});
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CarDTO> cars = responseEntity.getBody();

        assertThat(cars).hasSize(1);
        assertThat(cars).extracting("id").containsExactlyInAnyOrder(40L);
    }

}
