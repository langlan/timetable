package com.jytec.cs.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Course;

@RepositoryRestResource(collectionResourceRel = "course", path = "course")
public interface CourseRepository extends PagingAndSortingRepository<Course, String> {
	
}
