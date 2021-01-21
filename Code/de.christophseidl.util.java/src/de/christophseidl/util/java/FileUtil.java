package de.christophseidl.util.java;

import java.io.File;

public class FileUtil {
	public static String getExtension(File file) {
		String filename = file.getPath();
		return getExtension(filename);
	}
	
	public static String getExtension(String pathAndFilename) {
		int index = pathAndFilename.lastIndexOf(".");
		
		if (index == -1) {
			return "";
		}
		
		String rawExtension = pathAndFilename.substring(index + 1);
		return rawExtension.toLowerCase();
	}
}
