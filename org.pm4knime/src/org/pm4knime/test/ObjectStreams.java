package org.pm4knime.test;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Calendar;

public class ObjectStreams {
	static final String dataFile = "invoiceData";
	
	static final BigDecimal[] prices = {
			new BigDecimal("19.99"), 
	        new BigDecimal("9.99"),
	        new BigDecimal("15.99"),
	        new BigDecimal("3.99"),
	        new BigDecimal("4.99")
	};
	
	static final int[] units = { 12, 8, 13, 29, 50 };
	static final String[] descs = { "Java T-shirt",
            "Java Mug",
            "Duke Juggling Dolls",
            "Java Pin",
            "Java Key Chain" };
	private int testIdx = 0;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{ // , ClassNotFoundException
		ObjectStreams os = new ObjectStreams();
		Class clazz = os.getClass();
		Field[] fList = clazz.getDeclaredFields();
		
		System.out.println(fList.length);
		for(Field f: fList) {
			System.out.println(f.getName());
			System.out.println(f.getType().getTypeName());
		}
		
		/*
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile)));
			
			out.writeObject(Calendar.getInstance());
			
			for(int i=0; i< prices.length;i++) {
				out.writeObject(prices[i]);
				out.writeInt(units[i]);
				out.writeUTF(descs[i]);
			}
		}finally {
			out.close();
		}
	
	ObjectInputStream in = null;
	try {
		in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dataFile)));
		
		Calendar date = null;
		BigDecimal price = null;
		int unit;
		String desc ;
		BigDecimal total =  new BigDecimal(0);
		
		date = (Calendar) in.readObject();
		
		System.out.format("On %tA, %<tB %<te, %<tY:%n", date);
		
		try {
			while(true) {
				price = (BigDecimal) in.readObject();
				unit = in.readInt();
				desc = in.readUTF();
				
				System.out.format("You ordered %d units of %s at $%.2f%n",
                        unit, desc, price);
				
				total =  total.add(price).multiply(new BigDecimal(unit));
			}
		}catch (EOFException e) {}	
		
		System.out.format("For a TOTAL of: $%.2f%n", total);
	}finally {
			in.close();
	}
		*/
	}
	
}
