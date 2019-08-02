package org.pm4knime.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * this class scans the lib folder and create the item for Bundle-ClassPath: in Manifest.mf following its format
 * Bundle-ClassPath: 
    .,
    bin/,
   
   One line for one jar, and one space before new line
   
   should convert this class into another project
 * @author kefang-pads
 *
 */
public class ClassPathSetting {
	
	static String FILE_EXTENTION = ".jar";
	
	public static List<String> extractJars(String folderPath){
		List<String> jarNameList = new ArrayList();
		
		// scan the folderPath
		
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		List<String> verList = new ArrayList<String>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			File fi = listOfFiles[i];
			
			if (fi.isFile() && fi.getName().endsWith(FILE_EXTENTION)) {
				// get its name with extensiton
				jarNameList.add(
						fi.getName());
			}
			
		}
		
		return jarNameList;
	}
	
	/**
	 *  write the jar name into the file, if there are a lot, we can buffer them and write them one by one
	 * @param jarNameList
	 * @throws IOException 
	 */
	public static void writeFile(String filePath, List<String> jarNameList)  {
		File file = new File(filePath);
		try( FileWriter ft = new FileWriter(file)) {
			
			BufferedWriter bw = new BufferedWriter(ft);
			
			for(String jarName : jarNameList) {
				String line = " lib/" + jarName + "," +System.getProperty("line.separator");
				bw.write(line);
			}
			
			bw.close();
		}catch(IOException e) {
			System.out.print(e);
		}

	}
	
	public static void main(String[] args) {
		/*
		String path = System.getProperty("java.class.path");
		String [] branches = path.split(";");
		System.out.println("To output path :");
		int i=0;
		for(String branch: branches) {
			System.out.println(i++ + ":" + branch);
		}
		*/
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);
		
		String libPath = "lib";
		List jarList =  extractJars(libPath);
		
		System.out.println(jarList.size());
		
		String filePath = "jarlist.txt";
		// writeFile(filePath, jarList.subList(0, 10));
		writeFile(filePath, jarList);
	}
}
