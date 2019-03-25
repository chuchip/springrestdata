package com.profesorp.springdataresttest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.springframework.data.rest.core.annotation.RestResource;

import lombok.Data;

@Entity
@Data
@RestResource(rel="customers", path="customer")
public class CustomerEntity {	
	@Id
	long id;
	
	@Column
	String name;
	
	@Column
	String address;
	
	@Column
	String telephone;
	
	@OneToOne
	CityEntity city;
}
