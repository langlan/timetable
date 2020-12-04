package com.jytec.cs.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public abstract class BaseModel<ID_TYPE> {
	@JsonIgnore
	protected Date createdAt;
	@JsonIgnore
	protected Date updatedAt;

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PreUpdate
	@PrePersist
	public void beforeSave() {
		updatedAt = new Date();
		if (createdAt == null) {
			createdAt = updatedAt;
		}
	}

}
