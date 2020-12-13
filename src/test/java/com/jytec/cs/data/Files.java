package com.jytec.cs.data;

import java.io.File;

public interface Files {
	public static final File dir = new File("C:/Users/langlan/Desktop/课表");
	
	public static File of(String fileName) {
		return new File(dir, fileName);
	}
}
