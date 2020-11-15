package com.github.langlan.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Room {
	@Id
	String room;
	String no;
	String type;
	int capcity;
	/** 最终指派 */
	String usage;
	String multimedia;
	String memo;
}
