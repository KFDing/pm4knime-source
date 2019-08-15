package org.pm4knime.test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class DirectoryTests {
	public static void main(String[] args) {
		/*
		Iterable<Path> dirs =  FileSystems.getDefault().getRootDirectories();
		for(Path name : dirs) {
			System.err.println(name);
		}
		*/
		System.out.println(System.getProperty("java.library.path"));
		
		// try to run the test project in java program with lpsolve there.
	}
}
