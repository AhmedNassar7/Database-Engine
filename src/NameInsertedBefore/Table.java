package NameInsertedBefore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Table {

	String path;
	String strTableName;
	String strClusteringKeyColumn;
	Hashtable<String, String> htblColNameType;

	Table(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType)
			throws IOException {
		this.strTableName = strTableName;
		this.strClusteringKeyColumn = strClusteringKeyColumn;
		this.htblColNameType = htblColNameType;
		createMetaData();

		// path not initialzied
	}

	void createMetaData() throws IOException {
		File dir = new File("data/metadata.csv");
		FileWriter fileWriter = new FileWriter(dir, true);

		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter out = new PrintWriter(bw);
		for (String colName : htblColNameType.keySet()) {
			String dataType = htblColNameType.get(colName);
			// I set the INDEXED field to false,but it should be set in the function
			// createBitmapIndex
			out.print(strTableName + ", " + colName + ", " + dataType + ", "
					+ (colName.equals(strClusteringKeyColumn) ? "True, " : "False, ") + "False\n");
		}
		out.flush();
		out.close();
		fileWriter.close();
	}

	public void insertIntoTable(Hashtable<String, Object> htblColNameValue) throws DBAppException {
		/*
		 * 
		 * make sure to handle the name of .class files load page by page to search for
		 * the place of the tuple using the clustered column if it is the right place,
		 * update the page shift down and write it
		 */
	}

	public void updateTable(String strKey, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		/*
		 * 
		 * make sure to handle the name of .class files load page by page to search for
		 * the place of the tuple using the clustered column if it is the right place,
		 * update the page and write it
		 */

	}

	public void deleteFromTable(Hashtable<String, Object> htblColNameValue) throws DBAppException {

		/*
		 * 
		 * make sure to handle the name of .class files load page by page to search for
		 * the place of the tuple using the clustered column if it is the right place,
		 * update the page and write it
		 */

	}
}
