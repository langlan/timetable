package com.github.langlan.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.github.langlan.domain.Term;

@RepositoryRestResource(collectionResourceRel = "term", path = "term")
public interface TermRepository extends JpaRepository<Term, String> {
	
}
