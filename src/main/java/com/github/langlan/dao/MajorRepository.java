package com.github.langlan.dao;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Major;

@RepositoryRestResource(collectionResourceRel = "major", path = "major")
public interface MajorRepository extends PagingAndSortingRepository<Major, Long> {

	List<Major> findByName(@Param("name") String name);

}
