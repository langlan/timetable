package com.jytec.cs.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Teacher;

@RepositoryRestResource(collectionResourceRel = "teacher", path = "teacher")
public interface TeacherRepository extends PagingAndSortingRepository<Teacher, Long> {
	Optional<Teacher> findByName(@Param("name") String name);
}
