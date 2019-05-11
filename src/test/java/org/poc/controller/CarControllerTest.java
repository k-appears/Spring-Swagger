package org.poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.poc.Utils;
import org.poc.datatransferobject.CarDTO;
import org.poc.domainobject.CarDO;
import org.poc.service.car.CarService;
import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class CarControllerTest
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;
    private CarDTO input;

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;


    @BeforeClass
    public static void setup()
    {
        MockitoAnnotations.initMocks(CarController.class);
    }


    @Before
    public void init()
    {
        mvc = MockMvcBuilders.standaloneSetup(carController).dispatchOptions(true).build();
        input = Utils.createCarDTO();
        input.setId(1L);
        CarDO carDO = carController.createCarDO(input);

        doReturn(carDO).when(carService).find(any(Long.class));
        doReturn(carDO).when(carService).create(any(CarDO.class));
        doNothing().when(carService).update(any(CarDO.class));
        doNothing().when(carService).delete(any(Long.class));
    }


    @Test
    public void testGetCar() throws Exception
    {
        carController.getCar(input.getId());

        MvcResult result =
            mvc
                .perform(get("/v1/cars/{carId}", input.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CarDTO output = mapper.readValue(responseBody, CarDTO.class);
        assertThat(output).isEqualToIgnoringGivenFields(input, "id");
    }


    @Test
    public void createCar() throws Exception
    {
        String jsonInString = mapper.writeValueAsString(input);

        carController.createCar(input);
        MvcResult result =
            mvc
                .perform(
                    post("/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonInString))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CarDTO output = mapper.readValue(responseBody, CarDTO.class);
        assertThat(output).isEqualToIgnoringGivenFields(input, "id");
    }


    @Test
    public void updateCar() throws Exception
    {
        carController.createCar(input);

        CarDTO modified = Utils.copyCarDTOUpdated(input);

        String updatedInputString = mapper.writeValueAsString(modified);

        carController.update(input);

        mvc
            .perform(
                put("/v1/cars/{carId}", input.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(updatedInputString))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(Strings.EMPTY));
    }


    @Test
    public void deleteCar() throws Exception
    {
        doNothing().when(carService).delete(any(Long.class));

        carController.deleteCar(1L);

        mvc
            .perform(delete("/v1/cars/{carId}", 1L))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(Strings.EMPTY));
    }
}
