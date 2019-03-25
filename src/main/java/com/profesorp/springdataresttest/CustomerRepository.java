package com.profesorp.springdataresttest;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository  extends CrudRepository<CustomerEntity, Long>  {
	
	public List<CustomerEntity> findByNameIgnoreCaseContaining(@Param("name") String name);
}
