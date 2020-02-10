package com.mavericks.sathish.calculator.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mavericks.sathish.calculator.BillCalculator;

public class BillCalculatorTest {
	static BillCalculator billCalculator = null;

	@BeforeClass
	public static void setUp(){
		billCalculator = new BillCalculator();
	}

	@Test
	public void testCalculateSharesPositive(){
		billCalculator.calculateShare("resources/ExpenseSheet.txt","resources/ShareDetails.txt");
		File file = new File("resources/ShareDetails.txt");
		Assert.assertTrue(file.exists());
	}

	@Test
	public void testCalculateSharesNegative(){
		try{
			billCalculator.calculateShare(null,"resources/ShareDetails.txt");
		}
		catch(Exception exception){
			Assert.assertEquals("Invalid File Path. Please check...", exception.getMessage());
		}
	}

	@Test
	public void testGetSharesPositive() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Object[] parameters = new Object[1];
		Class[] paramTypes = new Class[1];
		paramTypes[0] = java.util.List.class;
		Method method = BillCalculator.class.getDeclaredMethod("getShares",paramTypes);
		method.setAccessible(true);

		List<String> inputExpense = new ArrayList<String>();
		inputExpense.add("Claire paid $100.10 for phone bill.");
		inputExpense.add("Bob paid $55.90 for petrol.");
		inputExpense.add("David paid $170.80 for groceries.");
		inputExpense.add("David paid $33.40 for breakfast.");
		inputExpense.add("Bob paid $85.60 for lunch.");
		inputExpense.add("Claire paid $103.45 for dinner.");
		inputExpense.add("Alicia paid $30.80 for snacks.");
		inputExpense.add("Alicia paid $70 for house-cleaning.");
		inputExpense.add("David paid $63.50 for utilities.");
		parameters[0] = inputExpense;

		List<String> shares = (List<String>) method.invoke(billCalculator, parameters);
		Assert.assertTrue(shares.contains("Alicia pays 25.16 to Claire"));
		Assert.assertTrue(shares.contains("Alicia pays 52.43 to David"));
		Assert.assertTrue(shares.contains("Bob pays 36.88 to David"));
	}	

	@Test
	public void testGetSharesNegative() throws NoSuchMethodException{
		Object[] parameters = new Object[1];
		Class[] paramTypes = new Class[1];
		paramTypes[0] = java.util.List.class;
		Method method = BillCalculator.class.getDeclaredMethod("getShares",paramTypes);
		method.setAccessible(true);
		List<String> inputExpense = new ArrayList<String>();
		parameters[0] = inputExpense;
		try {
			method.invoke(billCalculator, parameters);
		} catch (IllegalAccessException | InvocationTargetException e) {
			Assert.assertEquals("No data read from file..", e.getCause().getMessage());
		}
	}
}
