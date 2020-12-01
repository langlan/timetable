package com.jytec.cs.dao;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.jytec.cs.domain.Dept;

//@RepositoryRestResource(collectionResourceRel = "dept", path = "dept")
public interface DeptRepository extends PagingAndSortingRepository<Dept, Long> {

	List<Dept> findByName(@Param("name") String name);

}
