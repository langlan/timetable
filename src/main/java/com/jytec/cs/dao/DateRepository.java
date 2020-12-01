package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jytec.cs.domain.Date;

//@RepositoryRestResource(collectionResourceRel = "date", path = "date")
public interface DateRepository extends JpaRepository<Date, String> {
}
