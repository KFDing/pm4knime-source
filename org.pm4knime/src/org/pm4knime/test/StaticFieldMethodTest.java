package org.pm4knime.test;

import java.util.ArrayList;
import java.util.List;

public class StaticFieldMethodTest {
	static String name = "static field method test";
	static List<String> nameList = createNameList();
	
	public static String createName() {
		return "ok, stuff here";
	}
	
	public static List<String> createNameList() {
		// test variable there
		List<String> nameList = new ArrayList<String>();
		nameList.add("Kefang Ding");
		return nameList;
	}
	
	public static void main(String[] args) {
		/*
		String newName = "static field method test";
		if(name.equals(newName))
			System.out.println("name instance here are the same");
		
		String sName1 = StaticFieldMethodTest.createName();
		String sName2 = StaticFieldMethodTest.createName();
		
		if(sName1.equals(sName2))
			System.out.println("static name here are the same");
		*/
		List<String> nameList1 = StaticFieldMethodTest.createNameList();
		List<String> nameList2 = StaticFieldMethodTest.createNameList();
		if(nameList1.equals(nameList2))
			System.out.println("static name here are the same");
		
		if(nameList1 == nameList2)
			System.out.println("static name reference here are the same");
		
		StaticFieldMethodTest test1 = new StaticFieldMethodTest();
		StaticFieldMethodTest test2 = new StaticFieldMethodTest();
		if(test1.nameList == test2.nameList)
			System.out.println("static name field is the same");
		
	}
}
