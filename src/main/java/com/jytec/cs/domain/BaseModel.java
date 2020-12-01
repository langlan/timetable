package com.jytec.cs.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class BaseModel<ID_TYPE> {
	protected Date createdAt;
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
