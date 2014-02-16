package my.pack.test;

import java.util.ArrayList;

/**
 * 
 * @author VLD
 * It's test class for experiments.
 */
public class TestClass {

	public static void main(String[] args) {
		ArrayList<String> arr = new ArrayList<String>();
		arr.add("0");
		arr.add("1");
		arr.add("2");
		System.out.println(arr.get(0));
		arr.remove(0);
		System.out.println(arr.get(0));
	}
	
}
