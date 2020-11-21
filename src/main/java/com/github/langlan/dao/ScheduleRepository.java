package com.github.langlan.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.ClassCourse;
import com.github.langlan.domain.Schedule;

@RepositoryRestResource(collectionResourceRel = "schedule", path = "schedule")
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query("Select count(*) From Schedule s Where s.classCourse=?1")
	int countByClassCourse(ClassCourse classCourse);

	@Query("Select count(*) From Schedule s Where " //
			+ "s.classCourse.termYear=?3 And s.classCourse.termMonth=?4 And " //
			+ "s.classCourse.theClass.name=?1 and s.classCourse.theClass.degree=?2")
	int countByClassAndTerm(String className, String classDegree, short termYear, byte termMonth);
}
