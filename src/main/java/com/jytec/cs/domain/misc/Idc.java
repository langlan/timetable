package com.jytec.cs.domain.misc;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * For only restored from backup.
 */
@Entity
public class Idc {
	public static final int IDC_MIN_VALUE = 1000000;
	public static final int IDC_MAX_VALUE = 9999999;

	public static final byte USED_BY_CLASS = 1;
	public static final byte USED_BY_TEACHER = 2;

	@Id
	private int id;
	private byte mtype; // 1:class, 2:teacher.
	private String name; // model-name: for backup&restore.

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getMtype() {
		return mtype;
	}

	public void setMtype(byte mtype) {
		this.mtype = mtype;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
