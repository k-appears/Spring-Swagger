package org.poc.dataaccessobject;

import java.util.List;

import org.poc.domainobject.CarDO;
import org.poc.domainvalue.OnlineStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.security.access.annotation.Secured;

import org.poc.domainobject.DriverDO;

/**
 * Database Access Object for driver table.
 * <p/>
 */
@Secured("ROLE_USER")
public interface DriverRepository extends CrudRepository<DriverDO, Long>, QueryByExampleExecutor<DriverDO>
{
    DriverDO findByCar(CarDO carDO);


    List<DriverDO> findByOnlineStatus(OnlineStatus onlineStatus);
}
