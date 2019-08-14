package org.pm4knime.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * try to figure out the Serialization mechanism in java and try to apply it in Alignment.
 * <1> one class without the serialize but using input and output stream
 * <2> one is with partial and other not partial reference
 * <3> alignment object to use it
 * 
 *  relation to getter and setter
 * @author kefang-pads
 *
 */
public class SerializeUtil {
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		Student s = new Student();
		s.setID(15);
		s.setAge(00);
		s.setName("second try");
		s.setDep(Student.Department.Informatik);
		// Person p = new Person();
		try(FileOutputStream fos = new FileOutputStream("temp.out");
		    ObjectOutputStream oos = new ObjectOutputStream(fos)){
			
			oos.writeObject(s);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		FileInputStream fis = new FileInputStream("temp.out");
		ObjectInputStream oin = new ObjectInputStream(fis);
		Student ss = (Student) oin.readObject();
		System.out.println("Student ID :" + ss.getAge());
		System.out.println("Student ID :" + ss.getName());
		System.out.println("Student ID :" + ss.getID());
		System.out.println("Student Dep :" + ss.getDep());
	
	}
}

class Person{
	private String name="default";
	private int age = 20;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
}

class Student extends Person implements Serializable{
	public enum Department{Informatik, BWL};
	
	private int studentID;
	private Department dep;
	
	public Student() {}
	
	public Student(int id) {
		studentID = id;
	}
	public Department getDep() {
		return dep;
	}

	public void setDep(Department dep) {
		this.dep = dep;
	}

	
	
	public void setID(int id) {
		studentID = id;
	}
	
	public int getID() {
		return studentID;
	}
	
	// customized the output stream here 
	private void writeObject(ObjectOutputStream ostream) throws IOException {
		
		// perform the default serialization for all non-transient, non-static fields
		// ostream.defaultWriteObject();
		ostream.writeUTF(getName());
		ostream.writeInt(getAge());
		ostream.writeInt(getID());
		ostream.writeObject(getDep());
	}
	
	private void readObject(ObjectInputStream istream) throws ClassNotFoundException, IOException{      
		// istream.defaultReadObject();
		String name = istream.readUTF();
		int age = istream.readInt();
		int sid = istream.readInt();
		Department d = (Department) istream.readObject();
		
		setAge(age);
		setName(name);
		setID(sid);
		setDep(d);
		
    }
}