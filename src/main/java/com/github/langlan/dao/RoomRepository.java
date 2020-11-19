package com.github.langlan.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Room;

@RepositoryRestResource(collectionResourceRel = "room", path = "room")
public interface RoomRepository extends PagingAndSortingRepository<Room, Long> {

	// @Query("Select r.name || FUNCTION('IFNULL', r.type, 'null') From Room r")
	// @Query("Select CONCAT(r.name, r.type) From Room r")
	@Query("Select r.name, r.type From Room r") //for now, therer's no nulls, but just in case..
	List<String[]> findAllLogicKeys();

}
