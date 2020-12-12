package com.jytec.cs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.jytec.cs.domain.Site;

//@RepositoryRestResource(collectionResourceRel = "site", path = "site")
public interface SiteRepository extends PagingAndSortingRepository<Site, Long> {

	// @Query("Select r.name || FUNCTION('IFNULL', r.type, 'null') From Room r")
	// @Query("Select CONCAT(r.name, r.type) From Room r")
	@Query("Select r.name, r.roomType From Site r") // for now, there's no nulls, but just in case..
	List<String[]> findAllLogicKeys();

	@Query("Select r From Site r Where r.name=?1")
	Optional<Site> findUniqueByName(String name);

	Optional<Site> findByCode(String code);

	@Query("Select r From Site r Where Not Exists (Select rr.id From Site rr Where rr.name=r.name And rr.id!=r.id)")
	List<Site> findAllWithUniqueName();

	@Query("Select r From Site r Where Exists (Select rr.id From Site rr Where rr.name=r.name And rr.id<>r.id)")
	List<Site> findAllWithNotUniqueName();

}
