package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jytec.cs.domain.Schedule;
import com.jytec.cs.service.TermService;

//@RepositoryRestResource(collectionResourceRel = "schedule", path = "schedule")
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query("Select count(*) From Schedule s Where " //
			+ "s.termId=?3 And " //
			+ "s.theClass.name=?1 and s.theClass.degree=?2 And " + "s.trainingType='" + Schedule.TRAININGTYPE_NON + "'")
	int countNonTrainingByClassAndTerm(String className, String classDegree, String termId);

	@Query("Select count(*) From Schedule s Where " //
			+ "s.termId=?3 And " //
			+ "s.theClass.name=?1 and s.theClass.degree=?2 And " + "s.trainingType<>'" + Schedule.TRAININGTYPE_NON
			+ "' And " + "s.weekno between ?4 And ?5")
	int countTrainingByClassAndTermAndWeek(String name, String degree, String termId, byte weeknoStart, byte weeknoEnd);

	/**
	 * use when rebuild/init term data, or after import schedule data.
	 * 
	 * @see TermService#rebuildScheduleDate(com.jytec.cs.domain.Term)
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "Update Schedule s set s.date=" //
			+ "(Select d.date From Week w" //
			+ "  Join Date d On(d.date between w.firstDay And w.lastDay) " //
			+ "  Where w.termId=s.termId And w.weekno=s.weekno And d.dayOfWeek=s.dayOfWeek) " //
			+ "Where s.termId=?1")
	int updateDateByTerm(String termId);

}
