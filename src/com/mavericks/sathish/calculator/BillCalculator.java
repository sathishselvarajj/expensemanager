package com.mavericks.sathish.calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * This class has implementation that accepts expense data from txt file, parse it, split the bill among people
 * equally, gives information about who owes how much to whom, then write the information into file again.
 * 
 * This implementation is very dynamic, because in future if new person joins the home, we just need 
 * to capture the expense of the new person in the sheet. There is no place for static here.
 * 
 * Also if you need the share to be more precise (0.50 as 0.5023), you can always do that by increasing the decimals 
 * points configuration in line number 115.  
 * 
 * @author SathishKumar_S07
 *
 */
public class BillCalculator {
	private static Logger logger = Logger.getLogger(BillCalculator.class.getName());
	private static final String SPACE = " ";

	/**
	 * Method that recieves call from outside world that reads data from input file path, process it, write the data back into output filepath
	 * @param inputFilePath
	 * @param outputFilePath
	 */
	public void calculateShare(String inputFilePath, String outputFilePath){
		try{
			List<String> expense = loadFile(inputFilePath);
			List<String> shares = getShares(expense);
			writeIntoFile(shares, outputFilePath);
		}
		catch(Exception exception){
			logger.severe("Exception occured - "+exception.getMessage());
		}
	}

	/**
	 * Method to load the file from the path mentioned
	 * @param filePath
	 * @return List<String>
	 * @throws Exception
	 */
	private List<String> loadFile(String filePath) throws Exception {
		if(null!=filePath && !filePath.isEmpty()){
			logger.info("File path - "+filePath);
			File file = new File(filePath);
			if(file.isFile()){
				if(file.getName().endsWith(".txt")){
					//Using Java 7 feature try-with-resources that closes the stream automatically, else we have to handle it separately in finally block for closing the streams
					//File Reader for reading the file from the input path
					//BufferedReader for reading the file line by line
					try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
						//Initialize a list for storing the line as I do not want a entire file contents to be stored as huge string
						List<String> lineList = new ArrayList<>();
						String line;
						//Reading file line by line
						while ((line = bufferedReader.readLine()) != null) {
							logger.info("Line Read - "+line);
							lineList.add(line);
						}
						return lineList;
					}
					catch(Exception exception){
						logger.warning("Exception occurred - "+exception.getMessage());
						throw exception;
					}
				} else throw new RuntimeException("File is not in a right format. Expected in .txt");
			}
			else throw new RuntimeException("No such file exists....Please check...");
		}
		else throw new RuntimeException("Invalid File Path. Please check...");
	}

	/**
	 * Method to get shares and list the details about final settlement
	 * @param List<String> data
	 * @return List<String>
	 */
	private List<String> getShares(List<String> data){
		if(null!=data && !data.isEmpty()){
			HashMap<String, BigDecimal> membersMap = new HashMap<String, BigDecimal>();
			BigDecimal totalExpense = BigDecimal.ZERO;
			for(String line: data){
				try{
					//Splitting the line by Space identifier
					String words[] = line.split(SPACE);
					if(null!=words && words.length>3){
						String personName = words[0];
						//Extracting only the dollor spent (numbers with decimals) from the String using regex
						BigDecimal amount = new BigDecimal(words[2].replaceAll("[^0-9?!\\.]",""));
						if(null!=personName && personName.length()>0){
							//Summing up  the expense for existing person in the map using key
							if(membersMap.containsKey(personName)) membersMap.put(personName, membersMap.get(personName).add(amount));
							//Adding the expense for the new person in the map using key
							else membersMap.put(personName, amount);
							totalExpense = totalExpense.add(amount);
						}
					}
				}
				catch(Exception exception){
					logger.warning("Exception occurred - "+exception.getMessage());
					throw exception;}
			}
			logger.info("Total Expense - "+totalExpense);
			//Calculating average expense per person
			BigDecimal averagePerPerson = totalExpense.divide(new BigDecimal(membersMap.size()),2,RoundingMode.HALF_UP);
			logger.info("Average Expense per person - "+averagePerPerson);
			return calculateSharesFromMap(membersMap,averagePerPerson);
		}
		else throw new RuntimeException("No data read from file..");
	}

	/**
	 * Method to the amount owed by each person based on average expense 
	 * @param HashMap<String, BigDecimal> map
	 * @param BigDecimal averageExpense
	 * @return List<String>
	 */
	private List<String> calculateSharesFromMap(HashMap<String, BigDecimal> map, BigDecimal averageExpense){
		List<String> finalShareList = new ArrayList<>();
		for(Entry<String, BigDecimal> entry:map.entrySet()){
			//Finding who spends less than an average expense
			if(entry.getValue().compareTo(averageExpense)<0){
				for(Entry<String, BigDecimal> entry1:map.entrySet()){
					//Finding who spends more than an average
					if(entry1.getValue().compareTo(averageExpense)>0){
						//Finding total debt for a person
						BigDecimal totalDebt = averageExpense.subtract(entry.getValue());
						//Finding how much he has to return to the person he owes
						BigDecimal totalReturn = entry1.getValue().subtract(averageExpense);
						if(totalDebt.compareTo(totalReturn)>0){
							//Money settlement happens here
							map.put(entry.getKey(), map.get(entry.getKey()).add(totalReturn));
							map.put(entry1.getKey(), map.get(entry1.getKey()).subtract(totalReturn));
							finalShareList.add(new StringBuilder().append(entry.getKey()).append(" pays ").append(totalReturn).append(" to ").append(entry1.getKey()).toString());
						}
						else{
							//Money settlement happens here
							map.put(entry.getKey(), map.get(entry.getKey()).add(totalDebt));
							map.put(entry1.getKey(), map.get(entry1.getKey()).subtract(totalDebt));
							finalShareList.add(new StringBuilder().append(entry.getKey()).append(" pays ").append(totalDebt).append(" to ").append(entry1.getKey()).toString());
						}
					}
				}
			}
		}
		return finalShareList;

	}

	/**
	 * Method to write data into the file in mentioned path, line by line
	 * @param humanReadableData
	 * @param outputPath
	 * @throws Exception
	 */
	private void writeIntoFile(List<String> humanReadableData, String outputPath) throws Exception{
		//Perform the operation only if list has data
		if(null!= humanReadableData && !humanReadableData.isEmpty()){
			//Using Java 7 feature try-with-resources that closes the stream automatically, else we have to handle it separately in finally block for closing the streams
			try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath))){
				//Iterating through the list and writing the data line by line
				for(String data : humanReadableData){
					//Writing data
					if(data.isEmpty()) bufferedWriter.newLine();
					else {
						bufferedWriter.write(data);
						bufferedWriter.newLine();
					}
				}
				logger.info("File saved succefully saved in path - "+outputPath);
			}
			catch(Exception exception){
				logger.warning("Exception occurred - "+exception.getMessage());
				throw exception;
			}
		}
	}

	public static void main(String args[]) throws Exception{
		new BillCalculator().calculateShare("D:\\ExpenseManager/ExpenseSheet.txt","D:\\ExpenseManager/ShareDetails.txt");
		//System.out.println(new DolphinDecipher().findBestMatch("ehhhk","#@#$%ehhhhkkkkthisisawonderfulworldehhhhkkkkkk"));
		/*System.out.println(new DolphinDecipher().findBestMatch("ehhhk","ehhhhk#@#$%thisisawonderfulworld"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","#@#$%ehhhhkthisisawonderfulworld"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","#@#$%ehhhhhhhhkthisisawonderfulworld"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","#@#$%ehhhhkthisisawonderfulworldehhhhk"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","ehhhk??44545"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","ehhhhkkkkk"));
		System.out.println(new DolphinDecipher().findBestMatch("ehhhk","ekhhhhk"));*/
	}
}
