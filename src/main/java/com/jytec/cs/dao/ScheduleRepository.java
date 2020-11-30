package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Schedule;
import com.jytec.cs.service.TermService;

@RepositoryRestResource(collectionResourceRel = "schedule", path = "schedule")
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query("Select count(*) From Schedule s Where " //
			+ "s.termYear=?3 And s.termMonth=?4 And " //
			+ "s.theClass.name=?1 and s.theClass.degree=?2 And " + "s.trainingType='"+ Schedule.TRAININGTYPE_NON +"'")
	int countNonTrainingByClassAndTerm(String className, String classDegree, short termYear, byte termMonth);

	@Query("Select count(*) From Schedule s Where " //
			+ "s.termYear=?3 And s.termMonth=?4 And " //
			+ "s.theClass.name=?1 and s.theClass.degree=?2 And " + "s.trainingType<>'"+ Schedule.TRAININGTYPE_NON +"'")
	int countTrainingByClassAndTerm(String name, String degree, short termYear, byte termMonth);
	
	/**
	 * use when rebuild/init term data, or after import schedule data.
	 * 
	 * @see TermService#rebuildScheduleDate(com.jytec.cs.domain.Term)
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "Update Schedule s set s.date="
			+ "(Select d.date From Week w Join Date d On(d.date between w.firstDay And w.lastDay) "
			+ "  Where w.termYear=s.termYear And w.termMonth=s.termMonth And w.weekno=s.weekno And d.dayOfWeek=s.dayOfWeek) "
			+ "Where s.termYear=?1 And s.termMonth=?2")
	int updateDateByTerm(short termYear, byte termMonth);

	
}
