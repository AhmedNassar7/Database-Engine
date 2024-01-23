package NameInsertedBefore;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class DBAppTest {

	public static void main(String[] args) throws DBAppException, IOException, ClassNotFoundException, ParseException {
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream("config/DBApp.properties");
		properties.load(fis);
		DBApp.maxTuplesPerPage = Integer.parseInt(properties.getProperty("MaximumRowsCountinPage"));
		Bitmap.maxTuplesPerIndex = Integer.parseInt(properties.getProperty("BitmapSize"));
		String strTableName = "Student";
//		Hashtable htblColNameType = new Hashtable();
		Hashtable htblColNameValue = new Hashtable();
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
////		 htblColNameType.put("date", "java.lang.Date");
//		DBApp.createTable(strTableName, "id", htblColNameType);
//		//
		SQLTerm[] arrSQLTerms = new SQLTerm[1];
		String strArrOperators[] = new String[0];
		arrSQLTerms[0] = new SQLTerm(strTableName, "id", "=", new Integer(5));
		long start = System.currentTimeMillis();
		Iterator iterator = DBApp.selectFromTable(arrSQLTerms, strArrOperators);
		long end = System.currentTimeMillis();
		System.out.println("Without: " + (end - start));
//		while (iterator.hasNext()) {
//			Object object[] = (Object[]) iterator.next();
//			System.out.println(object.toString());
//		}
//		
		arrSQLTerms[0] = new SQLTerm(strTableName, "gpa", "=", new Double(1.9));
		long startIndex = System.currentTimeMillis();
		iterator = DBApp.selectFromTable(arrSQLTerms, strArrOperators);
		long endIndex = System.currentTimeMillis();
		System.out.println("With: "+(endIndex-startIndex));
//		htblColNameValue.put("id", new Integer(5));
//		htblColNameValue.put("name", new String("Zoz?"));
//		htblColNameValue.put("gpa", new Double(1.9));
////		DBApp.deleteFromTable(strTableName, htblColNameValue);
//		DBApp.updateTable(strTableName, "5", htblColNameValue);
//		htblColNameValue.clear();
//		htblColNameValue.put("id", new Integer(3));
//		htblColNameValue.put("name", new String("Zoz?"));
//		htblColNameValue.put("gpa", new Double(1.8));
//		DBApp.insertIntoTable(strTableName, htblColNameValue);
//		htblColNameValue.clear();
//		htblColNameValue.put("id", new Integer(2));
//		htblColNameValue.put("name", new String("Zoz?"));
//		htblColNameValue.put("gpa", new Double(1.9));
//		DBApp.insertIntoTable(strTableName, htblColNameValue);
//		htblColNameValue.clear();
//		htblColNameValue.put("id", new Integer(1));
//		htblColNameValue.put("name", new String("Zoz?"));
//		htblColNameValue.put("gpa", new Double(1.5));
//		DBApp.insertIntoTable(strTableName, htblColNameValue);
//		htblColNameValue.clear();
//		htblColNameValue.put("id", new Integer(4));
//		htblColNameValue.put("name", new String("Zoz?"));
//		htblColNameValue.put("gpa", new Double(1.5));
//		DBApp.insertIntoTable(strTableName, htblColNameValue);
//		htblColNameValue.clear();
//		DBApp.createBitmapIndex(strTableName, "gpa");
//		 htblColNameValue.put("gender", new Boolean( "false" ));
//		 htblColNameValue.put("date", new Date(1997 - 1900, 1 - 1, 19));
		// DBApp.updateTable(strTableName2, "Sun Jan 19 00:00:00 GMT+02:00 1997",
		// htblColNameValue);
		// DBApp.deleteFromTable(strTableName2, htblColNameValue);
		// DBApp.insertIntoTable(strTableName2, htblColNameValue);
		// htblColNameValue.clear( );
		// htblColNameValue.put("id", new Integer( 1200 ));
		// htblColNameValue.put("name", new String("Joe" ) );
		// htblColNameValue.put("gpa", new Double( 3 ) );
		// DBApp.updateUsingBitmap(strTableName, "2000",htblColNameValue);
		// DBApp.insertIntoTable(strTableName , htblColNameValue );
		// htblColNameValue.clear( );
		// htblColNameValue.put("id", new Integer( 1400 ));
		// htblColNameValue.put("name", new String("Dalia Noor" ) );
		// htblColNameValue.put("gpa", new Double( 1.25 ) );
		// DBApp.updateUsingBitmap(strTableName, "2000",htblColNameValue);
		// htblColNameValue.clear( );
		// htblColNameValue.put("id", new Integer( 200 ));
		// htblColNameValue.put("name", new String("John Noor" ) );
		// htblColNameValue.put("gpa", new Double( 1.5 ) );
		// DBApp.insertIntoTable( strTableName , htblColNameValue );
		// htblColNameValue.clear( );
		// htblColNameValue.put("id", new Integer(7842));
		// htblColNameValue.put("name", new String("Yes"));
		// htblColNameValue.put("gender", new Boolean(true));
		// htblColNameValue.put("date", new Date(1997 - 1900, 14 - 1, 12));
		// DBApp.insertIntoTable(strTableName, htblColNameValue);
		// DBApp.readTables("Student");
		DBApp.readTables(strTableName);
		Bitmap.readBitmap(strTableName, "gpa");
		// DBApp.createBitmapIndex(strTableName2, "date");
//		System.out.println(DBApp.EQUALNONINDEXED(strTableName, "date", new Date(1997 - 1900, 1 - 1, 19) + ""));
		// Bitmap.readBitmap(strTableName, "id");
//		Bitmap.readBitmap(strTableName2, "date");
	}

}
