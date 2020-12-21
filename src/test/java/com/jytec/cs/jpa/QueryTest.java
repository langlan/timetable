package com.jytec.cs.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jytec.cs.dao.common.Dao;
import com.jytec.cs.domain.Major;

@SpringBootTest
public class QueryTest {
	private @Autowired EntityManager em;
	private @Autowired Dao dao;

	@Test
	public void testUniqueSiteByName() {
//		CriteriaBuilder builder = em.getCriteriaBuilder();
//        CriteriaQuery<Major> query = builder.createQuery(Major.class);
//        Root<Major> m = query.from(Major.class);
// 
//        Predicate predicate = builder.conjunction();
//        query.where(predicate);
//        List<Major> result = em.createQuery(query).getResultList();
//        return result;
	}
	
	@Test
	public void testJPQL() {
		Query q = em.createQuery("Select m from Major m Where m.name like ?1");
		q.setParameter(1, "%理%");
		@SuppressWarnings("unchecked")
		List<Major> list = q.getResultList();
		System.out.println(list);
		List<Major> list2 = dao.find("Select m from Major m Where m.name like ?", "%理%");
		assertEquals(list.size(), list2.size());
		// assertArrayEquals(list.toArray(), list2.toArray());
	}
	
	@Test
	public void testMultipleFetchJoin() {
		Query q = em.createQuery("Select s From Schedule s Left join Fetch s.teachers t Left Join Fetch s.classes c Where c.id=?1");
		q.setParameter(1, 1L);
		q.getResultList();
	}
	
	@Test
	public void testJPQLSubStr() {
		Query q = em.createQuery("Select substr(m.date, 1, 7) from Schedule m Where m.id < 10");
		@SuppressWarnings("unchecked")
		List<String> list = q.getResultList();
		System.out.println(list);
	}

}
