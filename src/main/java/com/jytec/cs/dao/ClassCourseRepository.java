package com.jytec.cs.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jytec.cs.domain.ClassCourse;

//@RepositoryRestResource(collectionResourceRel = "class-course", path = "class-course")
public interface ClassCourseRepository extends JpaRepository<ClassCourse, Long> {

//	@Query("Select CONCAT(cc.theClass.name, '[', cc.theClass.degree, ']-', cc.course.code) "
	@Query("Select CONCAT(cc.theClass.name, '-', cc.course.code), cc "
			+ "From ClassCourse cc Where cc.termId=?1")
	List<Object[]> findAllIndexedByClassNameCourseCode(String termId);

//	@Query("Select CONCAT(cc.theClass.name, '[', cc.theClass.degree, ']-', cc.course.name), cc "
	@Query("Select CONCAT(cc.theClass.name, '-', cc.course.name), cc "
			+ "From ClassCourse cc Where cc.termId=?1")
	List<Object[]> findAllIndexedByNames(String termId);

	int deleteAllByTermId(String termId);
}
