package com.jytec.cs.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jytec.cs.domain.Term;

//@RepositoryRestResource(collectionResourceRel = "term", path = "term")
public interface TermRepository extends JpaRepository<Term, String> {
	@Query("Select t From Term t Where t.id<>?1 And(" //
			+ "?2 Between t.firstDay And t.lastDay Or " //
			+ "?3 Between t.firstDay And t.lastDay)")
	List<Term> findOverlappedTerms(String termId, String firstDay, String lastDay);

	@Modifying
	@Query("Delete From Week w Where w.termId=?1 And (w.lastDay<?2 Or w.firstDay>?3)")
	int deleteOutofRangeWeeks(String termId, String firstDay, String lastDay);

	@Modifying
	@Query("Delete From Date d Where d.termId=?1 And d.date not between ?2 And ?3")
	int deleteOutofRangeDays(String id, String firstDay, String lastDay);
}
