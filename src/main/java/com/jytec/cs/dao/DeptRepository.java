package com.jytec.cs.dao;

import static com.jytec.cs.domain.Dept.DEPT_TYPE_ELSE;
import static com.jytec.cs.domain.Dept.DEPT_TYPE_NORMAL;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.jytec.cs.domain.Dept;

//@RepositoryRestResource(collectionResourceRel = "dept", path = "dept")
public interface DeptRepository extends PagingAndSortingRepository<Dept, Long> {

	List<Dept> findByName(@Param("name") String name);

	@Modifying
	@Query("Update Dept d Set d.type='" + DEPT_TYPE_NORMAL + "' Where Exists (Select m From Major m Where m.dept=d)")
	int updateTypeOfNormal();
	
	@Modifying
	@Query("Update Dept d Set d.type='" + DEPT_TYPE_ELSE + "' Where Not Exists (Select m From Major m Where m.dept=d)")
	int updateTypeOfElse();
}
