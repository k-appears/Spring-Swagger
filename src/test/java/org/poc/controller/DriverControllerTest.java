package org.poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.poc.service.driver.DriverService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringJUnit4ClassRunner.class)
public class DriverControllerTest
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;


    @BeforeClass
    public static void setup()
    {
        MockitoAnnotations.initMocks(DriverController.class);
    }


    @Before
    public void init()
    {
        mvc = MockMvcBuilders.standaloneSetup(driverController).dispatchOptions(true).build();
        doNothing().when(driverService).selectCarByDriver(any(), any());
        doNothing().when(driverService).deselectCarByDriver(any());
    }


    @Test
    public void testSelectCarByDriver() throws Exception
    {
        doNothing().when(driverService).selectCarByDriver(any(Long.class), any(Long.class));

        MvcResult result =
            mvc
                .perform(
                    post("/v1/drivers/select")
                        .param("driverId", "1")
                        .param("carId", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(driverService, times(1)).selectCarByDriver(1L, 1L);
    }


    @Test
    public void testDeSelectCarByDriver() throws Exception
    {
        doNothing().when(driverService).deselectCarByDriver(any(Long.class));

        MvcResult result =
            mvc
                .perform(
                    post("/v1/drivers/deselect")
                        .param("driverId", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(driverService, times(1)).deselectCarByDriver(1L);
    }

}
