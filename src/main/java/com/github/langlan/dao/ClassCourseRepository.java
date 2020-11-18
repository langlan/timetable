package com.github.langlan.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.ClassCourse;

@RepositoryRestResource(collectionResourceRel = "class-course", path = "class-course")
public interface ClassCourseRepository extends JpaRepository<ClassCourse, Long> {
	
	@Query("Select cc.theClass, cc.course.code From ClassCourse cc Where cc.termYear=?1 And cc.termMonth=?2")
	List<Object[]> findClassAndCourseCodeByTerm(@Param("termYear") short termYear, @Param("termMonth") byte termMonth);
}
