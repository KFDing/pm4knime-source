package org.pm4knime.test;
/**
 * the question I want to verify is : why is possible to access privete member from other object in the same class?
 * it happens in 
 *  -- constructor public B(B other){...}
 *  -- clone method
 *  -- reflection, but how, I don't have example
 *  
 *   The reason is : the access modifier is on class level not on object level. 
 *   If the interaction is between two instances, they can access each other memebers. 
 *   Deep explanation behind it is, two objects in the same class know each other;
 *   and it's convenient to operate directly on them. 
 *   
 * At end, no test is needed anymore
 * @author kefang-pads
 *
 */
public class CloneTest {

}

class A{
	private int a = 0;
	private int b = 2;
	
}

class B{
	private int c = 10;
	private int d = 12;
	
	public B() {}
	
	public B(B other) {
		c = other.c;
		d = other.d;
		
		
	}
}