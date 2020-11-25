package com.jytec.cs.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.jytec.cs.domain.Date;

@RepositoryRestResource(collectionResourceRel = "date", path = "date")
public interface DateRepository extends JpaRepository<Date, String> {
}
