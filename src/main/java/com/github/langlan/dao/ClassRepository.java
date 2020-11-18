package com.github.langlan.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Class;

@RepositoryRestResource(collectionResourceRel = "class", path = "class")
public interface ClassRepository extends PagingAndSortingRepository<Class, Long> {
	
}
