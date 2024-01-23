package NameInsertedBefore;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBApp {
	static int maxTuplesPerPage;
	static ArrayList<Table> tables;
	static String deprecated = "empty cell";
	static String nullEntry = "empty cell";

	public static void init() {
		tables = new ArrayList();
	}

	public static Boolean ExistTable(String strTableName) throws IOException {
		List<List<String>> allinfoInMetaData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/metadata.csv"))) {
			String line;
			int co = 0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(strTableName)) {
					return true;
				}
			}

		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	public static Table getTable(String strTableName) {
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).strTableName == strTableName)
				return tables.get(i);
		}
		return null;
	}

	public static void writeTable() throws IOException {

		try {
			FileOutputStream fileOut = new FileOutputStream("tables1");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(tables);
			out.close();
			fileOut.close();

		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static void readTable() throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream("tables1");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		tables = (ArrayList<Table>) in.readObject();
		in.close();
		fileIn.close();
	}

	public static ArrayList<Pair> search(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws FileNotFoundException, IOException {
		ArrayList<Pair> pageIndex = new ArrayList<>();
		File file = new File("docs/pages/");
		String[] paths = file.list();
		int counter = 0;
		String last = null;
		Object[] tuple = new Object[htblColNameValue.size()];
		int idx = 0;
		tuple = getValueInOrder(strTableName, htblColNameValue);
		for (int jj = 0; jj < paths.length; jj++) {
			String[] x = paths[jj].split("_");
			if (x[0].equals(strTableName)) {
				last = paths[jj];
				counter++;
				Vector<Object> v = getNumberOfTuples("docs/pages/" + last);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					boolean equals = true;
					for (int j = 0; j < o.length && j < tuple.length; j++) {
						if (!o[j].equals(tuple[j]) && !(tuple[j] instanceof String && tuple[j].equals(deprecated)))
							equals = false;
					}
					if (equals)
						pageIndex.add(new Pair(paths[jj], i));
				}
			}
		}
		return pageIndex;
	}

	public static void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws DBAppException, IOException {
		if (!ExistTable(strTableName))
			new Table(strTableName, strClusteringKeyColumn, htblColNameType);
		else
			throw new DBAppException();
	}

	static void validateEntry(String strTableName, Hashtable<String, Object> htblColNameValue, boolean insert)
			throws DBAppException, FileNotFoundException, IOException {
		List<List<String>> allinfoInMetaData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/metadata.csv"))) {
			String line;
			int co = 0;
			int f = 0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(strTableName)) {
					co++;
					Object givenValue = htblColNameValue.get(values[1].substring(1));
					if (givenValue == null)
						continue;
					if (values[2].substring(1).equals("java.lang.String"))
						if (!(givenValue instanceof String))
							throw new DBAppException();
					if (values[2].substring(1).equals("java.lang.Integer"))
						if (!(givenValue instanceof Integer))
							throw new DBAppException();
					if (values[2].substring(1).equals("java.lang.Double"))
						if (!(givenValue instanceof Double))
							throw new DBAppException();
					if (values[2].substring(1).equals("java.lang.Boolean"))
						if (!(givenValue instanceof Boolean)) {
							throw new DBAppException();
						}

					if (values[2].substring(1).equals("java.util.Date")) {

						try {
							new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
									.parse(givenValue.toString());
						} catch (ParseException e) {
							throw new DBAppException();
						}

					}
					if (givenValue != null && values[3].substring(1).equals("True"))
						f = 1;
				}
			}
			co = insert ? co + 1 : co;
			if (f == 0)
				throw new DBAppException();
		}
	}

	public static void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws DBAppException, IOException, ParseException, ClassNotFoundException {
		htblColNameValue.put("TouchDate", new Date(System.currentTimeMillis()));
		validateEntry(strTableName, htblColNameValue, true);
		int p1 = getIndex(strTableName, htblColNameValue);
		pushDown(p1 + 1, strTableName, htblColNameValue);
		Bitmap.updateOnInsert(p1, strTableName, htblColNameValue);
	}

	static Object[] getValueInOrder(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws FileNotFoundException, IOException {

		Object[] tuple;
		try (BufferedReader br = new BufferedReader(new FileReader("data/metadata.csv"))) {
			String line;
			int co = 0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(strTableName))
					co++;
			}
			tuple = new Object[Math.max(htblColNameValue.size(), co + 1)];
		}
		try (BufferedReader br = new BufferedReader(new FileReader("data/metadata.csv"))) {
			String line;
			tuple[0] = htblColNameValue.get("TouchDate");
			int idx = 1;

			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(strTableName)) {
					Object givenValue = htblColNameValue.get(values[1].substring(1));
					tuple[idx++] = givenValue;
				}
			}
			for (int i = 0; i < tuple.length; i++) {
				if (tuple[i] == null)
					tuple[i] = (Object) "empty cell";
			}
		}
		return tuple;
	}

	static void pushDown(int where, String strTableName, Hashtable<String, Object> htblColNameValue)
			throws FileNotFoundException, IOException {
		where--;
		Object[] tuple = new Object[htblColNameValue.size()];
		int pushed = 0;
		tuple = getValueInOrder(strTableName, htblColNameValue);
		File file = new File("docs/pages/");
		String[] paths = file.list();
		int counter = 0;
		String curPath = null;

		int remainingFlag = 0;
		int enteries = 0;

		paths = sortPaths(paths);

		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				curPath = paths[j];

				StringTokenizer st = new StringTokenizer(paths[j], "_");
				String num = "";
				while (st.hasMoreTokens())
					num = st.nextToken();
				counter = Math.max(counter, Integer.parseInt(num));

				Vector<Object> newV = new Vector();
				Vector<Object> oldV = getNumberOfTuples("docs/pages/" + curPath);

				if (remainingFlag == 1) {
					newV.add(tuple);
					remainingFlag = 0;
				}
				if (pushed == 0)
					if (enteries + oldV.size() < where) {
						enteries += oldV.size();
						continue;
					} else {
						int i = 0;
						while (true) {
							if (enteries + i == where) {
								newV.add(tuple);
								break;
							}
							newV.add(oldV.remove(0));
							i++;
						}
						pushed = 1;
					}
				newV.addAll(oldV);
				if (newV.size() > maxTuplesPerPage) {
					tuple = (Object[]) newV.remove(newV.size() - 1);
					remainingFlag = 1;
				} else {
					remainingFlag = 0;
				}
				try {
					FileOutputStream fileOut = new FileOutputStream("docs/pages/" + curPath);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(newV);
					out.close();
					fileOut.close();
				} catch (IOException i) {
					i.printStackTrace();
				}
			}
		}

		if (pushed == 0)
			remainingFlag = 1;
		if (remainingFlag == 1) {
			++counter;
			Vector<Object> v = new Vector();
			v.add(tuple);
			try {
				FileOutputStream fileOut = new FileOutputStream("docs/pages/" + strTableName + "_" + counter);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(v);
				out.close();
				fileOut.close();

			} catch (IOException i) {
				i.printStackTrace();
			}
		}

	}

	public static String[] sortPaths(String[] paths) {
		for (int i = 0; i < paths.length - 1; i++) {
			for (int j = i + 1; j < paths.length; j++) {
				StringTokenizer st = new StringTokenizer(paths[i], "_");
				String crr = "";
				while (st.hasMoreTokens()) {
					crr = st.nextToken();
				}
				int ii = Integer.parseInt(crr);
				st = new StringTokenizer(paths[j], "_");
				while (st.hasMoreTokens()) {
					crr = st.nextToken();
				}
				int jj = Integer.parseInt(crr);
				if (ii >= jj) {
					String temp = paths[i];
					paths[i] = paths[j];
					paths[j] = temp;
				}
			}
		}
		return paths;
	}

	public static void readTables(String strTableName) {
		File file = new File("docs/pages/");
		String[] paths = file.list();
		int counter = 0;
		String curPath = null;
		paths = sortPaths(paths);
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				curPath = paths[j];
				Vector<Object> curV = getNumberOfTuples("docs/pages/" + curPath);
				System.out.println("docs/pages/" + curPath);
				for (int i = 0; i < curV.size(); i++) {
					Object[] v = (Object[]) curV.get(i);
					for (Object o : v) {
						System.out.print(o.toString() + ", ");
					}
					System.out.println();
				}
				System.out.println("----------new page---------");
			}
		}
	}

	static Vector<Object> getNumberOfTuples(String className) {
		Vector<Object> v;
		try {
			FileInputStream fileIn = new FileInputStream(className);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			v = (Vector) in.readObject();
			in.close();
			fileIn.close();
			return v;
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("class not found");
			c.printStackTrace();
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// END OF MILESTONE 1////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	public static String retrieveBitmapIndex(String strTableName, String strColumnName, String strColumnValue) {
		String startsWithFolderName = strTableName + "_" + strColumnName + "_" + "index";
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].startsWith(startsWithFolderName)) {
				Vector<BitmapPair> v = Bitmap.getBitMapPair("docs/bitmaps/" + paths[i]);
				for (int j = 0; j < v.size(); j++) {
					BitmapPair bp = (BitmapPair) v.get(j);
					if (bp.value.equals(strColumnValue))
						return bp.bitmap;
				}
			}
		}
		return "";
	}

	public static String XOR(String columnValue1, String columnValue2) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < Math.min(columnValue1.length(), columnValue2.length()); i++) {
			int valueColumn1 = Integer.parseInt(columnValue1.charAt(i) + "");
			int valueColumn2 = Integer.parseInt(columnValue2.charAt(i) + "");
			if (((valueColumn1 + valueColumn2) & 1) == 1)
				result.append(1);
			else
				result.append(0);
		}
		if (result.length() == 0)
			for (int i = 0; i < Math.max(columnValue1.length(), Math.max(maxTuplesPerPage, columnValue2.length())); i++)
				result.append(0);
		return result.toString();
	}

	public static String OR(String columnValue1, String columnValue2) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < Math.min(columnValue1.length(), columnValue2.length()); i++) {
			int valueColumn1 = Integer.parseInt(columnValue1.charAt(i) + "");
			int valueColumn2 = Integer.parseInt(columnValue2.charAt(i) + "");
			if ((valueColumn1 + valueColumn2) > 0)
				result.append(1);
			else
				result.append(0);
		}
		if (result.length() == 0)
			for (int i = 0; i < Math.max(columnValue1.length(), Math.max(maxTuplesPerPage, columnValue2.length())); i++)
				result.append(0);
		return result.toString();
	}

	public static String NOTEQUAL(String strTableName, String strColumnName, String strColumnValue) {
		String bitmap = retrieveBitmapIndex(strTableName, strColumnName, strColumnValue);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < bitmap.length(); i++)
			if (bitmap.charAt(i) == '0')
				result.append(1);
			else
				result.append(0);
		return result.toString();
	}

	public static String AND(String columnValue1, String columnValue2) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < Math.min(columnValue1.length(), columnValue2.length()); i++) {
			int valueColumn1 = Integer.parseInt(columnValue1.charAt(i) + "");
			int valueColumn2 = Integer.parseInt(columnValue2.charAt(i) + "");
			if (valueColumn1 == 1 && valueColumn2 == 1)
				result.append(1);
			else
				result.append(0);
		}
		if (result.length() == 0)
			for (int i = 0; i < Math.max(columnValue1.length(), Math.max(maxTuplesPerPage, columnValue2.length())); i++)
				result.append(0);
		return result.toString();
	}

	public static String GREATERTHAN(String strTableName, String strColumnName, String strColumnValue) {
		String result = "";
		String startsWithFolderName = strTableName + "_" + strColumnName + "_" + "index";
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].startsWith(startsWithFolderName)) {
				Vector<BitmapPair> v = Bitmap.getBitMapPair("docs/bitmaps/" + paths[i]);
				for (int j = 0; j < v.size(); j++) {
					BitmapPair bp = (BitmapPair) v.get(j);
					if (bp.value.equals(strColumnValue)) {
						int length = bp.bitmap.length();
						if (result.length() == 0)
							for (int k = 0; k < length; k++)
								result += "0";
						for (int k = j + 1; k < v.size(); k++) {
							BitmapPair ORed = (BitmapPair) v.get(k);
							result = OR(result, ORed.bitmap);
						}
						for (int startHere = i + 1; startHere < paths.length; startHere++) {
							if (paths[startHere].startsWith(startsWithFolderName)) {
								Vector<BitmapPair> above = Bitmap.getBitMapPair("docs/bitmaps/" + paths[startHere]);
								for (int allBitmaps = 0; allBitmaps < above.size(); allBitmaps++) {
									BitmapPair bitmapPair = (BitmapPair) above.get(allBitmaps);
									result = OR(result, bitmapPair.bitmap);
								}
							}
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	public static String GREATERTHANOREQUAL(String strTableName, String strColumnName, String strColumnValue) {
		String result = "";
		String startsWithFolderName = strTableName + "_" + strColumnName + "_" + "index";
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].startsWith(startsWithFolderName)) {
				Vector<BitmapPair> v = Bitmap.getBitMapPair("docs/bitmaps/" + paths[i]);
				for (int j = 0; j < v.size(); j++) {
					BitmapPair bp = (BitmapPair) v.get(j);
					if (bp.value.equals(strColumnValue)) {
						int length = bp.bitmap.length();
						if (result.length() == 0)
							for (int k = 0; k < length; k++)
								result += "0";
						for (int k = j; k < v.size(); k++) {
							BitmapPair ORed = (BitmapPair) v.get(k);
							result = OR(result, ORed.bitmap);
						}
						for (int startHere = i + 1; startHere < paths.length; startHere++) {
							if (paths[startHere].startsWith(startsWithFolderName)) {
								Vector<BitmapPair> above = Bitmap.getBitMapPair("docs/bitmaps/" + paths[startHere]);
								for (int allBitmaps = 0; allBitmaps < above.size(); allBitmaps++) {
									BitmapPair bitmapPair = (BitmapPair) above.get(allBitmaps);
									result = OR(result, bitmapPair.bitmap);
								}
							}
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	public static String LESSTHAN(String strTableName, String strColumnName, String strColumnValue) {
		String result = "";
		String startsWithFolderName = strTableName + "_" + strColumnName + "_" + "index";
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].startsWith(startsWithFolderName)) {
				Vector<BitmapPair> v = Bitmap.getBitMapPair("docs/bitmaps/" + paths[i]);
				for (int j = 0; j < v.size(); j++) {
					BitmapPair bp = (BitmapPair) v.get(j);
					if (bp.value.equals(strColumnValue)) {
						int length = bp.bitmap.length();
						if (result.length() == 0)
							for (int k = 0; k < length; k++)
								result += "0";
						for (int k = j - 1; k >= 0; k--) {
							BitmapPair ORed = (BitmapPair) v.get(k);
							result = OR(result, ORed.bitmap);
						}
						for (int startHere = i - 1; startHere >= 0; startHere--) {
							if (paths[startHere].startsWith(startsWithFolderName)) {
								Vector<BitmapPair> below = Bitmap.getBitMapPair("docs/bitmaps/" + paths[startHere]);
								for (int allBitmaps = 0; allBitmaps < below.size(); allBitmaps++) {
									BitmapPair bitmapPair = (BitmapPair) below.get(allBitmaps);
									result = OR(result, bitmapPair.bitmap);
								}
							}
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	public static String LESSTHANOREQUAL(String strTableName, String strColumnName, String strColumnValue) {
		String result = "";
		String startsWithFolderName = strTableName + "_" + strColumnName + "_" + "index";
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].startsWith(startsWithFolderName)) {
				Vector<BitmapPair> v = Bitmap.getBitMapPair("docs/bitmaps/" + paths[i]);
				// System.out.println(paths[i]);
				for (int j = 0; j < v.size(); j++) {
					BitmapPair bp = (BitmapPair) v.get(j);
					if (bp.value.equals(strColumnValue)) {
						// okay so we should go up in this vector and do all previous pages again
						// begin from index J
						int length = bp.bitmap.length();
						if (result.length() == 0)
							for (int k = 0; k < length; k++)
								result += "0";
						for (int k = j; k >= 0; k--) {
							BitmapPair ORed = (BitmapPair) v.get(k);
							result = OR(result, ORed.bitmap);
						}
						for (int startHere = i - 1; startHere >= 0; startHere--) {
							if (paths[startHere].startsWith(startsWithFolderName)) {
								Vector<BitmapPair> below = Bitmap.getBitMapPair("docs/bitmaps/" + paths[startHere]);
								for (int allBitmaps = 0; allBitmaps < below.size(); allBitmaps++) {
									BitmapPair bitmapPair = (BitmapPair) below.get(allBitmaps);
									result = OR(result, bitmapPair.bitmap);
								}
							}
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	public static String ControlUnit(String strTableName, String strColumnName, String strColumnValue,
			String strOperator) {
		String result = "";
		switch (strOperator) {
		case ">":
			result = GREATERTHAN(strTableName, strColumnName, strColumnValue);
			break;
		case ">=":
			result = GREATERTHANOREQUAL(strTableName, strColumnName, strColumnValue);
			break;
		case "<":
			result = LESSTHAN(strTableName, strColumnName, strColumnValue);
			break;
		case "<=":
			result = LESSTHANOREQUAL(strTableName, strColumnName, strColumnValue);
			break;
		case "!=":
			result = NOTEQUAL(strTableName, strColumnName, strColumnValue);
			break;
		case "=":
			result = retrieveBitmapIndex(strTableName, strColumnName, strColumnValue);
			break;
		}
		return result;
	}

	public static String LESSTHANOREQUALNONINDEXED(String strTableName, String strColumnName, String strColumnValue)
			throws ParseException {
		// use .toLowerCase()
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		int idxStrKey = 0;
		boolean doubleType = false;
		boolean integerType = false;
		boolean dateType = false;
		boolean stringType = false;
		boolean booleanType = false;
		boolean found = false;
		String str = "java.lang.string";
		String integer = "java.lang.integer";
		String dbl = "java.lang.double";
		String date = "java.util.date";
		String bool = "java.lang.boolean";
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					idxStrKey++;
					if ((data.split(", ")[1]).toLowerCase().equals(strColumnName)) {
						if ((data.split(", ")[2]).toLowerCase().equals(str))
							stringType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(integer))
							integerType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(date))
							dateType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(dbl))
							doubleType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(bool))
							booleanType = true;
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					if (o[idxStrKey].toString().equals("empty cell")) {
						result.append(0);
						continue;
					}
					if (stringType || booleanType) {
						String objString = (String) o[idxStrKey];
						String valString = (String) strColumnValue;
						if (objString.compareTo(valString) <= 0 && !objString.equals("empty cell"))
							result.append(1);
						else
							result.append(0);
					} else if (dateType) {
						Date objDate = (Date) o[idxStrKey];
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date valDate = dateFormat.parse(strColumnValue);
						if (objDate.compareTo(valDate) <= 0)
							result.append(1);
						else
							result.append(0);

					} else if (integerType) {
						int objInt = Integer.parseInt((String) (o[idxStrKey] + ""));
						int valInt = Integer.parseInt(strColumnValue);
						if (objInt <= valInt)
							result.append(1);
						else
							result.append(0);
					} else if (doubleType) {
						double objDouble = Double.parseDouble((String) (o[idxStrKey] + ""));
						double valDouble = Double.parseDouble(strColumnValue);
						if (objDouble <= valDouble)
							result.append(1);
						else
							result.append(0);
					}
				}
			}
		}
		return result.toString();
	}

	public static String LESSTHANNONINDEXED(String strTableName, String strColumnName, String strColumnValue)
			throws ParseException {
		// use .toLowerCase()
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		int idxStrKey = 0;
		boolean doubleType = false;
		boolean integerType = false;
		boolean dateType = false;
		boolean stringType = false;
		boolean booleanType = false;
		boolean found = false;
		String str = "java.lang.string";
		String integer = "java.lang.integer";
		String dbl = "java.lang.double";
		String date = "java.util.date";
		String bool = "java.lang.boolean";
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					idxStrKey++;
					if ((data.split(", ")[1]).toLowerCase().equals(strColumnName)) {
						if ((data.split(", ")[2]).toLowerCase().equals(str))
							stringType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(integer))
							integerType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(date))
							dateType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(dbl))
							doubleType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(bool))
							booleanType = true;
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					if (o[idxStrKey].toString().equals("empty cell")) {
						result.append(0);
						continue;
					}
					if (stringType || booleanType) {
						String objString = (String) o[idxStrKey];
						String valString = (String) strColumnValue;
						if (objString.compareTo(valString) <= 0 && !objString.equals("empty cell"))
							result.append(1);
						else
							result.append(0);
					} else if (dateType) {
						Date objDate = (Date) o[idxStrKey];
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date valDate = dateFormat.parse(strColumnValue);
						if (objDate.compareTo(valDate) < 0)
							result.append(1);
						else
							result.append(0);

					} else if (integerType) {
						int objInt = Integer.parseInt((String) (o[idxStrKey] + ""));
						int valInt = Integer.parseInt(strColumnValue);
						if (objInt < valInt)
							result.append(1);
						else
							result.append(0);
					} else if (doubleType) {
						double objDouble = Double.parseDouble((String) (o[idxStrKey] + ""));
						double valDouble = Double.parseDouble(strColumnValue);
						if (objDouble < valDouble)
							result.append(1);
						else
							result.append(0);
					}
				}
			}
		}
		return result.toString();
	}

	public static String GREATERTHANOREQUALNONINDEXED(String strTableName, String strColumnName, String strColumnValue)
			throws ParseException {
		// use .toLowerCase()
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		int idxStrKey = 0;
		boolean doubleType = false;
		boolean integerType = false;
		boolean dateType = false;
		boolean stringType = false;
		boolean booleanType = false;
		boolean found = false;
		String str = "java.lang.string";
		String integer = "java.lang.integer";
		String dbl = "java.lang.double";
		String date = "java.util.date";
		String bool = "java.lang.boolean";
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					idxStrKey++;
					if ((data.split(", ")[1]).toLowerCase().equals(strColumnName)) {
						if ((data.split(", ")[2]).toLowerCase().equals(str))
							stringType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(integer))
							integerType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(date))
							dateType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(dbl))
							doubleType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(bool))
							booleanType = true;
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					if (o[idxStrKey].toString().equals("empty cell")) {
						result.append(0);
						continue;
					}
					if (stringType || booleanType) {
						String objString = (String) o[idxStrKey];
						String valString = (String) strColumnValue;
						if (objString.compareTo(valString) <= 0 && !objString.equals("empty cell"))
							result.append(1);
						else
							result.append(0);
					} else if (dateType) {
						Date objDate = (Date) o[idxStrKey];
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date valDate = dateFormat.parse(strColumnValue);
						if (objDate.compareTo(valDate) >= 0)
							result.append(1);
						else
							result.append(0);

					} else if (integerType) {
						int objInt = Integer.parseInt((String) (o[idxStrKey] + ""));
						int valInt = Integer.parseInt(strColumnValue);
						if (objInt >= valInt)
							result.append(1);
						else
							result.append(0);
					} else if (doubleType) {
						double objDouble = Double.parseDouble((String) (o[idxStrKey] + ""));
						double valDouble = Double.parseDouble(strColumnValue);
						if (objDouble >= valDouble)
							result.append(1);
						else
							result.append(0);
					}
				}
			}
		}
		return result.toString();
	}

	public static String GREATERTHANNONINDEXED(String strTableName, String strColumnName, String strColumnValue)
			throws ParseException {
		// use .toLowerCase()
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		int idxStrKey = 0;
		boolean doubleType = false;
		boolean integerType = false;
		boolean dateType = false;
		boolean stringType = false;
		boolean booleanType = false;
		boolean found = false;
		String str = "java.lang.string";
		String integer = "java.lang.integer";
		String dbl = "java.lang.double";
		String date = "java.util.date";
		String bool = "java.lang.boolean";
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					idxStrKey++;
					if ((data.split(", ")[1]).toLowerCase().equals(strColumnName)) {
						if ((data.split(", ")[2]).toLowerCase().equals(str))
							stringType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(integer))
							integerType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(date))
							dateType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(dbl))
							doubleType = true;
						if ((data.split(", ")[2]).toLowerCase().equals(bool))
							booleanType = true;
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					if (o[idxStrKey].toString().equals("empty cell")) {
						result.append(0);
						continue;
					}
					if (stringType || booleanType) {
						String objString = (String) o[idxStrKey];
						String valString = (String) strColumnValue;
						if (objString.compareTo(valString) <= 0 && !objString.equals("empty cell"))
							result.append(1);
						else
							result.append(0);
					} else if (dateType) {
						Date objDate = (Date) o[idxStrKey];
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date valDate = dateFormat.parse(strColumnValue);
						if (objDate.compareTo(valDate) > 0)
							result.append(1);
						else
							result.append(0);

					} else if (integerType) {
						int objInt = Integer.parseInt((String) (o[idxStrKey] + ""));
						int valInt = Integer.parseInt(strColumnValue);
						if (objInt > valInt)
							result.append(1);
						else
							result.append(0);
					} else if (doubleType) {
						double objDouble = Double.parseDouble((String) (o[idxStrKey] + ""));
						double valDouble = Double.parseDouble(strColumnValue);
						if (objDouble > valDouble)
							result.append(1);
						else
							result.append(0);
					}
				}
			}
		}
		return result.toString();
	}

	public static String EQUALNONINDEXED(String strTableName, String strColumnName, Object strColumnValue)
			throws ParseException {
		if (strColumnName.equals("TouchDate"))
			return "";
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = sortPaths(paths);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		int idxStrKey = 0;
		boolean found = false;
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					idxStrKey++;
					if ((data.split(", ")[1]).equals(strColumnName)) {
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					boolean equals = false;
					if (o[idxStrKey].toString().equals("empty cell")) {
						result.append(0);
						continue;
					}
					if (o[idxStrKey] instanceof String) {
						equals = ((String) (o[idxStrKey])).equals(strColumnValue);
					}
					if (o[idxStrKey] instanceof Double) {
						equals = ((Double) o[idxStrKey]) == Double.parseDouble(strColumnValue + "");
					}
					if (o[idxStrKey] instanceof Integer) {
						equals = ((Integer) o[idxStrKey]) == Integer.parseInt(strColumnValue + "");
					}
					if (o[idxStrKey] instanceof Boolean)
						equals = ((Boolean) o[idxStrKey]) == Boolean.parseBoolean(strColumnValue + "");
					if (o[idxStrKey] instanceof Date) {
						Date objDate = (Date) o[idxStrKey];
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date valDate = dateFormat.parse(strColumnValue + "");
						equals = objDate.compareTo(valDate) == 0;
					}
					if (equals)
						result.append(1);
					else
						result.append(0);
				}
			}
		}
		return result.toString();
	}

	public static String NOTEQUALNONINDEXED(String strTableName, String strColumnName, String strColumnValue)
			throws ParseException {
		String equal = EQUALNONINDEXED(strTableName, strColumnName, strColumnValue);
		StringBuilder notEqual = new StringBuilder();
		for (int i = 0; i < equal.length(); i++)
			if (equal.charAt(i) == '0')
				notEqual.append(1);
			else
				notEqual.append(0);
		return notEqual.toString();
	}

	public static String ControlUnitNonIndexed(String strTableName, String strColumnName, String strColumnValue,
			String strOperator) throws ParseException {
		String result = "";
		switch (strOperator) {
		case ">":
			result = GREATERTHANNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		case ">=":
			result = GREATERTHANOREQUALNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		case "<":
			result = LESSTHANNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		case "<=":
			result = LESSTHANOREQUALNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		case "!=":
			result = NOTEQUALNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		case "=":
			result = EQUALNONINDEXED(strTableName, strColumnName, strColumnValue);
			break;
		}
		return result;
	}

	public static String SQLOperation(SQLTerm[] arrSQLTerms, String[] strArrOperators) throws ParseException {
		String result = "";
		ArrayList<String> realTerms = turnSQLTermToBitmap(arrSQLTerms);
		ArrayList<String> realOperators = new ArrayList<>();
		for (int i = 0; i < strArrOperators.length; i++)
			realOperators.add(strArrOperators[i]);
		result = loopForOR(loopForXOR(loopForAND(bitSQLQuery(realTerms, realOperators)))).get(0);
		return result;
	}

	public static ArrayList<String> turnSQLTermToBitmap(SQLTerm[] arrSQLTerms) throws ParseException {
		ArrayList<String> bitmaps = new ArrayList<>();
		for (int i = 0; i < arrSQLTerms.length; i++) {
			String bitmapValue = ControlUnit(arrSQLTerms[i].strTableName, arrSQLTerms[i].strColumnName,
					arrSQLTerms[i].objValue + "", arrSQLTerms[i].strOperator);
			if (bitmapValue.length() == 0)
				bitmapValue = ControlUnitNonIndexed(arrSQLTerms[i].strTableName, arrSQLTerms[i].strColumnName,
						arrSQLTerms[i].objValue + "", arrSQLTerms[i].strOperator);
			bitmaps.add(bitmapValue);
		}
		return bitmaps;
	}

	public static Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strArrOperators) throws ParseException {
		String valid = SQLOperation(arrSQLTerms, strArrOperators);
		ArrayList<Object> returned = new ArrayList<>();
		File file = new File("docs/pages/");
		String[] paths = sortPaths(file.list());
		paths = sortPaths(paths);
		int idxOfIndex = 0;
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(arrSQLTerms[0].strTableName)) {
				Vector<Object> v = getNumberOfTuples("docs/pages/" + paths[j]);
				Object[] o;
				for (int i = 0; i < v.size(); i++) {
					o = (Object[]) (v.get(i));
					if (valid.charAt(idxOfIndex++) == '1')
						returned.add(o);
				}
			}
		}
		Iterator iterator = returned.iterator();
		return iterator;
	}

	public static ArrayList<String> reduceTerm(ArrayList<String> oldQuery, int prevVal1, int prevOper, int prevVal2,
			String newVal) {
		ArrayList<String> newQuery = new ArrayList<>();

		for (int i = 0; i < oldQuery.size(); i++) {

			if (i == prevVal1) {
				newQuery.add(newVal);
			}

			if (i != prevVal1 && i != prevOper && i != prevVal2) {
				newQuery.add(oldQuery.get(i));
			}
		}

		return newQuery;
	}

	public static ArrayList<String> loopForAND(ArrayList<String> Query) {

		for (int i = 0; i < Query.size(); i++) {
			if (Query.get(i).equals("AND")) {
				String val1 = Query.get(i - 1);
				String val2 = Query.get(i + 1);
				// call And func
				String newVal = AND(val1, val2);
				Query = reduceTerm(Query, i - 1, i, i + 1, newVal);
				i = 0;
			}
		}

		return Query;
	}

	public static ArrayList<String> loopForOR(ArrayList<String> Query) {

		for (int i = 0; i < Query.size(); i++) {
			if (Query.get(i).equals("OR")) {
				String val1 = Query.get(i - 1);
				String val2 = Query.get(i + 1);
				// call OR func
				String newVal = OR(val1, val2);
				Query = reduceTerm(Query, i - 1, i, i + 1, newVal);
				i = 0;
			}
		}

		return Query;
	}

	public static ArrayList<String> loopForXOR(ArrayList<String> Query) {

		for (int i = 0; i < Query.size(); i++) {
			if (Query.get(i).equals("XOR")) {
				String val1 = Query.get(i - 1);
				String val2 = Query.get(i + 1);
				// call XOR func
				String newVal = XOR(val1, val2);
				Query = reduceTerm(Query, i - 1, i, i + 1, newVal);
				i = 0;
			}
		}

		return Query;
	}

	public static ArrayList<String> bitSQLQuery(ArrayList<String> bits, ArrayList<String> opr) {
		ArrayList<String> result = new ArrayList<>();

		for (int i = 0; i < bits.size(); i++) {
			result.add(bits.get(i));
			if (i < opr.size()) {
				result.add(opr.get(i));
			}
		}

		return result;
	}

	public static void parentInsert(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws DBAppException, IOException, ParseException, ClassNotFoundException {

		String flag = getNameofIndexedCol(strTableName, htblColNameValue);
		if (flag.equals("")) {
			insertIntoTable(strTableName, htblColNameValue);
		} else {
			// use bitmaps;
		}
	}

	// names if col with bitmaps and we need it
	public static String getNameofIndexedCol(String tableName, Hashtable<String, Object> htblColNameValue) {
		String colName = "";
		String result = "";
		File meta = new File("data/metadata.csv");
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String s = inputStream.nextLine();
				if (s.split(", ")[0].equals(tableName) && s.split(", ")[4].equals("True")) {
					colName = s.split(", ")[1];
					if (htblColNameValue.containsKey(colName)) {
						result += colName + " ";
					}

				}
			}
			inputStream.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		return result;
	}

	public static String bitmapOfColumns(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws ParseException {
		Enumeration<String> enumerator = htblColNameValue.keys();
		String result = "";
		while (enumerator.hasMoreElements()) {
			String key = enumerator.nextElement();
			String bitmap = retrieveBitmapIndex(strTableName, key, htblColNameValue.get(key) + "");
			if (bitmap.length() == 0)
				bitmap = EQUALNONINDEXED(strTableName, key, htblColNameValue.get(key));
			result = bitmap;
			break;
		}

		while (enumerator.hasMoreElements()) {
			String key = enumerator.nextElement();
			String bitmap = retrieveBitmapIndex(strTableName, key, htblColNameValue.get(key) + "");
			if (bitmap.length() == 0)
				bitmap = EQUALNONINDEXED(strTableName, key, htblColNameValue.get(key) + "");
			result = AND(result, bitmap);
		}

		return result;
	}

	public static ArrayList<BitmapPair> readBitmap(String tableName, String colName) throws FileNotFoundException {
		String[] paths = new File("docs/bitmaps").list();
		ArrayList<BitmapPair> bitmapPairs = new ArrayList<>();
		paths = sortPaths(paths);
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(tableName) && splitted[1].equals(colName)) {
				Vector<BitmapPair> list = Bitmap.getBitMapPair("docs/bitmaps/" + path);
				for (BitmapPair pair : list) {
					bitmapPairs.add(new BitmapPair(pair.value, pair.bitmap));

				}

			}
		}
		return bitmapPairs;
	}

	public static int deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws ParseException, FileNotFoundException, IOException {
		String toBeDeleted = bitmapOfColumns(strTableName, htblColNameValue);
		ArrayList<Integer> pagesToBeDeletedFrom = getPagesOfBitmap(toBeDeleted, strTableName);
		Object tuple[] = new Object[htblColNameValue.size()];
		tuple = getValueInOrder(strTableName, htblColNameValue);
		ArrayList<Integer> cumSum = getPages(strTableName);
		int deleted = 0;

		for (int page : pagesToBeDeletedFrom) {
			int counter = page == 1 ? 0 : cumSum.get(page - 2);
			Vector<Object> v = getNumberOfTuples("docs/pages/" + strTableName + "_" + page);
			Object o[];
			for (int i = 0; i < v.size(); counter++) {
				o = (Object[]) v.get(i);
				boolean equals = true;
				for (int j = 1; j < o.length; j++) {
					if (!(o[j] == null) && !o[j].equals(tuple[j])
							&& !(tuple[j] instanceof String && tuple[j].equals(deprecated))) {
						if (o[j] instanceof String)
							equals = ((String) (o[j])).equals(tuple[j]);
						if (o[j] instanceof Double)
							equals = ((Double) o[j]) == Double.parseDouble(tuple[j] + "");
						if (o[j] instanceof Integer)
							equals = ((Integer) o[j]) == Integer.parseInt(tuple[j] + "");
						if (o[j] instanceof Boolean)
							equals = ((Boolean) o[j]) == Boolean.parseBoolean(tuple[j] + "");
						if (o[j] instanceof Date) {
							SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
									Locale.ENGLISH);
							equals = ((Date) o[j]).equals(dateFormat.parse(tuple[j] + ""));
						}
					}
				}
				if (equals) {
					v.remove(i);
					deleted++;
					Bitmap.updateOnDelete(counter--, strTableName);
				} else
					i++;
				File deletedFile = new File("docs/pages/" + strTableName + "_" + page);
				deletedFile.delete();
				if (v.size() > 0) {
					try {
						FileOutputStream fileOut = new FileOutputStream("docs/pages/" + strTableName + "_" + page);
						ObjectOutputStream out = new ObjectOutputStream(fileOut);
						out.writeObject(v);
						out.close();
						fileOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return deleted;
	}

	public static void updateTable(String strTableName, String strKey, Hashtable<String, Object> htblColNameValue)
			throws FileNotFoundException, DBAppException, IOException, ParseException, ClassNotFoundException {
		Hashtable passedToDelete = new Hashtable();
		validateEntry(strTableName, htblColNameValue, false);
		String metaFile = "data/metadata.csv";
		File meta = new File(metaFile);
		boolean found = false;
		try {
			Scanner sc = new Scanner(meta);
			while (sc.hasNext() && !found) {
				String data = sc.nextLine();
				String x[] = data.split("_");
				if (x[0].startsWith(strTableName)) {
					if ((data.split(", ")[3]).equals("True")) {
						passedToDelete.put(data.split(", ")[1], new String(strKey));
						found = true;
						break;
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int deletedCount = deleteFromTable(strTableName, passedToDelete);
		while (deletedCount-- > 0)
			insertIntoTable(strTableName, htblColNameValue);
	}

	static ArrayList<Integer> getPages(String strTableName) {
		File dir = new File("docs/pages/");
		String[] paths = DBApp.sortPaths(dir.list());
		ArrayList<Integer> ans = new ArrayList();
		int sum = 0;
		int i = 1;
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(strTableName)) {
				int idx = Integer.parseInt(splitted[1]);
				while (i < idx) {
					ans.add(sum);
					i++;
				}
				Vector<Object> v = DBApp.getNumberOfTuples("docs/pages/" + path);
				sum += v.size();
				ans.add(sum);
				i++;
			}
		}
		return ans;
	}

	static ArrayList<Integer> getPagesOfBitmap(String bitmap, String tableName) {
		TreeSet<Integer> set = new TreeSet();
		ArrayList<Integer> pages = getPages(tableName);

		for (int i = 0, j = 0; i < bitmap.length(); i++) {
			if (bitmap.charAt(i) == '1') {
				while (i + 1 > pages.get(j))
					j++;
				set.add(j + 1);
			}
		}
		ArrayList<Integer> ans = new ArrayList<>();
		for (int x : set)
			ans.add(x);
		return ans;
	}

	static int getIndex(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws FileNotFoundException, IOException, ParseException {
		String fileNameDefined = "data/metadata.csv";
		int whichClusCol = 0;
		File meta = new File(fileNameDefined);
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String data = inputStream.nextLine();
				if (data.split(", ")[0].equals(strTableName)) {
					if ((data.split(", ")[3]).equals("False")) {
						whichClusCol++;
					} else {
						whichClusCol++;
						break;
					}
				}
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int index = 0;
		File file = new File("docs/bitmaps/");
		String[] paths = file.list();
		String last = null;
		Object[] tuple = new Object[htblColNameValue.size()];
		tuple = getValueInOrder(strTableName, htblColNameValue);
		/////////////// sorting the paths///////////////////////////
		paths = sortPaths(paths);
		///////////////////////////////////////////////////
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(strTableName)
					&& splitted[1].equals(Bitmap.getNameofColumn(strTableName, whichClusCol - 1))) {
				String colName = Bitmap.getNameofColumn(strTableName, whichClusCol - 1);
				String type = Bitmap.getType(strTableName, colName);
				Vector<BitmapPair> list = Bitmap.getBitMapPair("docs/bitmaps/" + path);
				BitmapPair pair = list.get(list.size() - 1);
				String v = pair.value;
				if (Bitmap.isInteger(v)) {
					if (new Integer(Integer.parseInt(v)).compareTo((Integer) tuple[whichClusCol]) < 0) {
						continue;
					} else {
						int x = binarySearch(list, tuple[whichClusCol]);
						if (x == -1) {
							break;
						} else {
							return x;
						}
					}
				} else {
					if (Bitmap.isBoolean(v)) {
						if ((v.equals("false") && ((String) tuple[whichClusCol]).equals("true"))) {
							continue;
						} else {
							int x = binarySearch(list, tuple[whichClusCol]);
							if (x == -1) {
								break;
							} else {
								return x;
							}
						}
					} else {
						if (Bitmap.isDouble(v)) {
							Double tmp = Double.parseDouble(tuple[whichClusCol].toString());
							if ((Double.parseDouble(v)) < tmp) {
								continue;
							} else {
								int x = binarySearch(list, tuple[whichClusCol]);
								if (x == -1) {
									break;
								} else {
									return x;
								}
							}

						} else {
							if (type.equals("Date")) {
								SimpleDateFormat formatter6 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
										Locale.ENGLISH);
								Date v1 = formatter6.parse(v), tmp = formatter6.parse(tuple[whichClusCol].toString());
								if (v1.compareTo(tmp) < 0) {
									continue;
								} else {
									int x = binarySearch(list, tuple[whichClusCol]);
									if (x == -1) {
										break;
									} else {
										return x;
									}
								}
							} else {
								if (v instanceof java.lang.String) {
									if (((String) v).compareTo((String) tuple[whichClusCol]) < 0) {
										continue;
									} else {
										int x = binarySearch(list, tuple[whichClusCol]);
										if (x == -1) {
											break;
										} else {
											return x;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		file = new File("docs/pages/");
		paths = file.list();
		paths = sortPaths(paths);
		for (String path : paths) {
			for (int j = 0; j < paths.length; j++) {
				String[] x = paths[j].split("_");
				if (x[0].equals(strTableName)) {
					last = paths[j];
					Vector<Object> v = getNumberOfTuples("docs/pages/" + last);
					Object[] o;
					for (int i = 0; i < v.size(); i++) {
						o = (Object[]) (v.get(i));
						if (o[whichClusCol] instanceof java.lang.Integer) {
							if (((Integer) o[whichClusCol]).compareTo(((Integer) tuple[whichClusCol])) >= 0) {
								return index;
							}
						} else {
							if (o[whichClusCol] instanceof java.lang.String) {
								if (((String) o[whichClusCol]).compareTo((String) tuple[whichClusCol]) >= 0) {
									return index;
								}
							} else {
								if (o[whichClusCol] instanceof java.lang.Double) {
									if (((Double) o[whichClusCol]).compareTo((Double) tuple[whichClusCol]) >= 0) {
										return index;
									}
								} else {
									if (o[whichClusCol] instanceof Date) {
										if (((Date) o[whichClusCol]).compareTo((Date) tuple[whichClusCol]) >= 0) {
											return index;
										}
									} else {
										if (o[whichClusCol] instanceof java.lang.Boolean) {
											if (((Boolean) o[whichClusCol])
													.compareTo((Boolean) tuple[whichClusCol]) >= 0) {
												return index;
											}
										}
									}
								}
							}
						}
						index++;
					}
				}
			}
			return index;
		}
		return index;
	}

	public static int binarySearch(Vector<BitmapPair> v, Object o) throws ParseException {
		if (o instanceof java.lang.Integer) {
			int i = 0;
			int j = (v.size() - 1);
			int result = 0;

			while (i <= j) {

				int insert = Integer.parseInt(o.toString());
				BitmapPair tmp2 = v.get(i + j >> 1);
				int tmp = Integer.parseInt(tmp2.value);
				if (tmp >= insert) {
					result = (i + j) / 2;
					j = (i + j) / 2 - 1;

				} else {
					i = ((i + j) / 2) + 1;
				}
			}

			return v.get(result).bitmap.indexOf('1');
		} else {
			if (o instanceof java.lang.String) {
				int i = 0;
				int j = (v.size() - 1);
				int result = 0;
				while (i <= j) {
					if (v.get((i + j) / 2).value.compareTo((String) o) >= 0) {
						result = (i + j) / 2;
						j = (i + j) / 2 - 1;
					} else {
						i = ((i + j) / 2) + 1;
					}
				}
				return v.get(result).bitmap.indexOf('1');
			} else {
				if (o instanceof java.lang.Double) {
					int i = 0;
					int j = (v.size() - 1);
					int result = 0;
					while (i <= j) {
						if (Double.parseDouble(v.get((i + j) / 2).value) >= (Double) o) {
							result = (i + j) / 2;
							j = (i + j) / 2 - 1;
						} else {
							i = ((i + j) / 2) + 1;
						}
					}
					return v.get(result).bitmap.indexOf('1');
				} else {
					if (o instanceof Date) {
						int i = 0;
						SimpleDateFormat formatter6 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
								Locale.ENGLISH);
						Date insert = formatter6.parse(o.toString());
						int j = (v.size() - 1);
						int result = 0;
						while (i <= j) {
							Date tmp = formatter6.parse(v.get((i + j) / 2).value);
							if (tmp.compareTo(insert) >= 0) {
								result = (i + j) / 2;
								j = (i + j) / 2 - 1;
							} else {
								i = ((i + j) / 2) + 1;
							}
						}
						return v.get(result).bitmap.indexOf('1');
					} else {
						if (o instanceof java.lang.Boolean) {
							int i = 0;
							int j = (v.size() - 1);
							int result = 0;
							while (i <= j) {
								if (v.get((i + j) / 2).value.equals("true")
										|| (v.get((i + j) / 2).value.equals("false") && ((String) o).equals("false"))) {
									result = (i + j) / 2;
									j = (i + j) / 2 - 1;
								} else {
									i = ((i + j) / 2) + 1;
								}
							}
							return v.get(result).bitmap.indexOf('1');
						}
					}
				}
			}
		}
		return -1;
	}

	public static void main(String[] args) throws DBAppException, IOException, ParseException, ClassNotFoundException {
		// Initialize Maximum Values
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream("config/DBApp.properties");
		properties.load(fis);
		maxTuplesPerPage = Integer.parseInt(properties.getProperty("MaximumRowsCountinPage"));
		Bitmap.maxTuplesPerIndex = Integer.parseInt(properties.getProperty("BitmapSize"));
	}

	static class Pair {
		String path;
		int idx;

		Pair(String a, int b) {
			path = a;
			idx = b;
		}

		public String toString() {
			return "Path: " + path + "Index: " + idx + "\n";
		}
	}

	public static Bitmap createBitmapIndex(String tableName, String colName) throws IOException, ParseException {
		return new Bitmap(tableName, colName);
	}
}
