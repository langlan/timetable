package com.jytec.cs.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Class;

@RepositoryRestResource(collectionResourceRel = "class", path = "class")
public interface ClassRepository extends PagingAndSortingRepository<Class, Long> {

	Optional<Class> findByNameAndDegree(String name, String degree);
	
}
