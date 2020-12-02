package com.jytec.cs.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jytec.cs.domain.ClassCourse;

//@RepositoryRestResource(collectionResourceRel = "class-course", path = "class-course")
public interface ClassCourseRepository extends JpaRepository<ClassCourse, Long> {

//	@Query("Select CONCAT(cc.theClass.name, '[', cc.theClass.degree, ']-', cc.course.code) "
	@Query("Select CONCAT(cc.theClass.name, '-', cc.course.code) "
			+ "From ClassCourse cc Where cc.termYear=?1 And cc.termMonth=?2")
	List<String> findAllLogicKeyByTerm(@Param("termYear") short termYear, @Param("termMonth") byte termMonth);

//	@Query("Select CONCAT(cc.theClass.name, '[', cc.theClass.degree, ']-', cc.course.name), cc "
	@Query("Select CONCAT(cc.theClass.name, '-', cc.course.name), cc "
			+ "From ClassCourse cc Where cc.termYear=?1 And cc.termMonth=?2")
	List<Object[]> findAllWithKeyByTerm(@Param("termYear") short termYear, @Param("termMonth") byte termMonth);
}
