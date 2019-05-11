package org.poc.dataaccessobject;

import org.poc.domainobject.CarDO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.security.access.annotation.Secured;

/**
 * Database Access Object for car table.
 * <p/>
 */
@Secured("ROLE_USER")
public interface CarRepository extends CrudRepository<CarDO, Long>, QueryByExampleExecutor<CarDO>
{

}
