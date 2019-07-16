package org.pm4knime.test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyCharacters {
	
public static void main(String[] args) throws IOException{
	FileReader inputStream = null;
	FileWriter outputStream = null;
	String path = "/home/dkf/ProcessMining/programs/KNIME_Development/Workflow/datasets/";
	try {
		inputStream =  new FileReader(path + "xanadu.txt");
		outputStream = new FileWriter(path + "outagin2.txt");
		int c, num=0;
		while((c= inputStream.read())!=-1) {
			System.out.println(c);
			outputStream.write(c);
			num++;
		}
		System.out.println(num);
	}finally {
		if(inputStream !=null) {
			inputStream.close();
		}
		if(outputStream != null) {
			outputStream.close();
		}
	}
}
}
