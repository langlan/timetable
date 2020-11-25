package com.github.langlan.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Site;

@RepositoryRestResource(collectionResourceRel = "site", path = "site")
public interface SiteRepository extends PagingAndSortingRepository<Site, Long> {

	// @Query("Select r.name || FUNCTION('IFNULL', r.type, 'null') From Room r")
	// @Query("Select CONCAT(r.name, r.type) From Room r")
	@Query("Select r.name, r.roomType From Site r") //for now, therer's no nulls, but just in case..
	List<String[]> findAllLogicKeys();

	@Query("Select r From Site r Where r.name=?1")
	Optional<Site> findUniqueByName(String name);

}
