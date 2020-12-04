package com.jytec.cs.domain.misc;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Idc {
	public static final int IDC_MIN_VALUE = 1000000;
	public static final int IDC_MAX_VALUE = 9999999;
	
	public static final byte USED_BY_CLASS = 1;
	public static final byte USED_BY_TEACHER = 2;
	
	@Id
	private int id;
	private byte used; // 1:class, 2:teacher.

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getUsed() {
		return used;
	}

	public void setUsed(byte used) {
		this.used = used;
	}

}
