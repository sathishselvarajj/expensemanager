# expensemanager
Java project to calculate expense and display each ones shares
/**
 * This application accepts expense data from txt file, parse it, split the bill among people
 * equally, gives information about who owes how much to whom, then write the information into file again.
 * 
 * The implementation is very dynamic, because in future if new person joins the home, we just need 
 * to capture the expense of the new person in the sheet. There is no place for static here.
 * 
 * Also if you need the share to be more precise (0.50 as 0.5023), you can always do that by increasing the decimals 
 * points configuration in line number 115.  
 * 
 * @author SathishKumarSelvaraj
 *
 */
