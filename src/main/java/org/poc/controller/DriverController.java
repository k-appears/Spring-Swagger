package org.poc.controller;

import org.poc.controller.mapper.DriverMapper;
import org.poc.datatransferobject.CarDTO;
import org.poc.datatransferobject.DriverDTO;
import org.poc.domainobject.DriverDO;
import org.poc.domainvalue.OnlineStatus;
import org.poc.exception.AssignedDriverNotOnlineException;
import org.poc.exception.CarAlreadyInUseException;
import org.poc.exception.ConstraintsViolationException;
import org.poc.exception.EntityNotFoundException;
import org.poc.service.driver.DriverService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.poc.service.driver.DefaultDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * All operations with a driver will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("v1/drivers")
public class DriverController
{

    private final DriverService driverService;


    @Autowired
    public DriverController(final DriverService driverService)
    {
        this.driverService = driverService;
    }


    @GetMapping("/{driverId}")
    public DriverDTO getDriver(@PathVariable long driverId) throws EntityNotFoundException
    {
        return DriverMapper.makeDriverDTO(driverService.find(driverId));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDTO createDriver(@Valid @RequestBody DriverDTO driverDTO) throws ConstraintsViolationException
    {
        DriverDO driverDO = DriverMapper.makeDriverDO(driverDTO);
        return DriverMapper.makeDriverDTO(driverService.create(driverDO));
    }


    @DeleteMapping("/{driverId}")
    public void deleteDriver(@PathVariable long driverId) throws EntityNotFoundException
    {
        driverService.delete(driverId);
    }


    @PutMapping("/{driverId}")
    public void updateLocation(
        @PathVariable long driverId, @RequestParam double longitude, @RequestParam double latitude)
        throws EntityNotFoundException
    {
        driverService.updateLocation(driverId, longitude, latitude);
    }


    @GetMapping
    public List<DriverDTO> findDrivers(@RequestParam OnlineStatus onlineStatus)
    {
        return DriverMapper.makeDriverDTOList(driverService.find(onlineStatus));
    }


    @PostMapping("/select")
    public void selectCarByDriver(@RequestParam long driverId, @RequestParam long carId) throws EntityNotFoundException, CarAlreadyInUseException, AssignedDriverNotOnlineException
    {
        driverService.selectCarByDriver(driverId, carId);
    }


    @PostMapping("/deselect")
    public void deselectCarByDriver(@RequestParam long driverId) throws EntityNotFoundException
    {
        driverService.deselectCarByDriver(driverId);
    }


    /**
     * Search by attributes using AND in a full-text search, results contain everything in which all the terms appear.</br>
     *
     * Using <code>Map</code> as input is <b>not</b> scalable due to the custom parser for each <code><Entry/code> to filter results @see {@link DefaultDriverService#searchByFieldsOfCarAndDriver(Map)} parsing method}.
     * If using {@link CarDTO} and {@link DriverDTO} the Swagger UI will show JSON representation of both classes with all fields but not all fields need to be shown as criteria.
     * The solution is to use annotation <code>@Example</code> but due to <a href="https://github.com/springfox/springfox/issues/1503">springfox issue</a>, <code>@Example</code> section in Swagger UI can not be overwritten.
     *
     * @param params Contains filter criteria of car and driver attributes
     * @return List of DriverDTO matching criteria
     */
    @PostMapping("/search")
    @ApiOperation(value = "Search by attributes in car and driver, if repeated attributes last one is used. See Javadoc", response = Iterable.class)
    public List<DriverDTO> search(
        @ApiParam(
            value = "{\"username\": \"string\", \"onlineStatus\": \"ONLINE, OFFLINE\", \"convertible\": Boolean, \"engineType\": \"ELECTRIC, GAS, HYBRID\" , \"manufacturer\" : \"string\", \"rating\": Float, \"seatCount\": Integer}",
            required = true, examples = @Example(
                value = {
                    @ExampleProperty(value = "{'Examples not overwritten':'https://github.com/springfox/springfox/issues/1503'}", mediaType = "application/json")
                })) @RequestBody @NotEmpty Map<String, String> params)
    {
        return DriverMapper.makeDriverDTOList(driverService.searchByFieldsOfCarAndDriver(params));
    }

}
