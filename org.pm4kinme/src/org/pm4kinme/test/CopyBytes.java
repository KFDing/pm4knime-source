package org.pm4kinme.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyBytes {
public static void main(String[] args) throws IOException{
	FileInputStream inputStream = null;
	FileOutputStream outputStream = null;
	String path = "/home/dkf/ProcessMining/programs/KNIME_Development/Workflow/datasets/";
	try {
		inputStream =  new FileInputStream(path + "xanadu.txt");
		outputStream = new FileOutputStream(path + "outagin.txt");
		int c, num=0;
		while((c= inputStream.read())!=-1) {
			
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
