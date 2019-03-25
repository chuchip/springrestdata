package com.profesorp.springdataresttest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.rest.core.annotation.RestResource;

import lombok.Data;

@Entity
@Data
@RestResource(exported=false)
public class CityEntity {		
	@Id 
	int id;
	@Column
	String name;
	@Column
	String province;
}
