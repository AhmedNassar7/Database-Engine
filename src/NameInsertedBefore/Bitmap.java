package NameInsertedBefore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Bitmap implements Serializable {

	static int maxTuplesPerIndex = 2;
	Vector<BitmapPair> list;

	Bitmap(String tableName, String colName) throws IOException, ParseException {
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream("config/DBApp.properties");
		properties.load(fis);
		maxTuplesPerIndex = Integer.parseInt(properties.getProperty("BitmapSize"));
		int counter = 0;
		list = new Vector();
		ArrayList<String> uniqueValues = getUniqueValues(tableName, colName);
		uniqueValues = sortValues(uniqueValues, tableName, colName);
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = DBApp.sortPaths(paths);
		int y = getIndexofColumn(tableName, colName);
		for (String value : uniqueValues) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < paths.length; j++) {
				String[] n = paths[j].split("_");
				// if path start with the tableName will search for the value of unique value of
				// loop i
				if (n[0].equals(tableName)) {
					Vector<Object> v = DBApp.getNumberOfTuples("docs/pages/" + paths[j]);
					for (int k = 0; k < v.size(); k++) {
						Object[] oneRow = (Object[]) v.get(k);
						// should be add into the bit map and the page number will be n[1]
						sb.append(oneRow[y].toString().equals(value) ? "1" : "0");

					}
					// deleted padding
					// for (int i = v.size(); i < DBApp.maxTuplesPerPage; i++)
					// sb.append("0");
					// sb.append("-");
				}
			}

			list.add(new BitmapPair(value, sb.toString()));
			if (list.size() == maxTuplesPerIndex) {
				writeCompressed(list, "docs/bitmaps/" + tableName + "_" + colName + "_" + "index" + "_" + counter++);
				list.clear();
			}
		}
		updateMetaBitMap(tableName, colName);
		if (list.isEmpty())
			return;
		writeCompressed(list, "docs/bitmaps/" + tableName + "_" + colName + "_" + "index" + "_" + counter);

	}

	static void writeCompressed(Vector<BitmapPair> vector, String fileName) {
		try {
			File file2 = new File(fileName);
			FileOutputStream out2 = new FileOutputStream(file2);
			GZIPOutputStream gzipStream = new GZIPOutputStream(out2);
			BufferedOutputStream bout = new BufferedOutputStream(gzipStream);
			ObjectOutputStream compressedOutput = new ObjectOutputStream(bout);
			compressedOutput.writeObject(vector);
			compressedOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static ArrayList<String> sortValues(ArrayList<String> values, String tableName, String colName)
			throws ParseException {
		String type = getType(tableName, colName);
		if (type.equals("String")) {
			Collections.sort(values);
			return values;
		}

		if (type.equals("Integer")) {
			Collections.sort(values, (x, y) -> Integer.compare(Integer.parseInt(x), Integer.parseInt(y)));
			return values;
		}

		if (type.equals("Double")) {
			Collections.sort(values, (x, y) -> Double.compare(Double.parseDouble(x), Double.parseDouble(y)));
			return values;
		}

		if (type.equals("Boolean")) {
			Collections.sort(values, (x, y) -> Boolean.compare(new Boolean(x), new Boolean(y)));
			return values;
		}

		else {
			SimpleDateFormat formatter6 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

			ArrayList<Date> dates = new ArrayList();
			for (String s : values) {
				dates.add(formatter6.parse(s));

			}
			Collections.sort(dates);
			values.clear();
			for (Date x : dates)
				values.add(x.toString());

			return values;
		}

	}

	public static void updateMetaBitMap(String tableName, String colName) throws IOException {
		File meta = new File("data/metadata.csv");
		ArrayList<String[]> newMetaData = new ArrayList();
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String s = inputStream.nextLine();

				String[] splitted = s.split(", ");
				if (s.split(", ")[0].equals(tableName) && s.split(", ")[1].equals(colName))
					splitted[4] = "True";
				newMetaData.add(splitted);

			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		File dir = new File("data/metadata.csv");
		FileWriter fileWriter = new FileWriter(dir);

		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter out = new PrintWriter(bw);
		for (String[] line : newMetaData) {
			for (int i = 0; i < line.length; i++) {
				out.print(line[i] + (i + 1 == line.length ? "" : ", "));
			}
			out.println();
		}
		out.flush();
		out.close();
		fileWriter.close();
	}

	public static ArrayList<String> getUniqueValues(String strTableName, String columnName) throws IOException {
		ArrayList<String> result = new ArrayList<>();
		int location = getIndexofColumn(strTableName, columnName);
		File file = new File("docs/pages/");
		String[] paths = file.list();
		paths = DBApp.sortPaths(paths);
		for (int j = 0; j < paths.length; j++) {
			String[] x = paths[j].split("_");
			if (x[0].equals(strTableName)) {

				Vector<Object> v = DBApp.getNumberOfTuples("docs/pages/" + paths[j]);
				// getting every page and compare every column
				for (int i = 0; i < v.size(); i++) {
					Object[] oneRow = (Object[]) v.get(i);
					boolean fl1 = false;
					for (int k = 0; k < result.size(); k++) {
						if ((oneRow[location] + "").equals(result.get(k)))
							fl1 = true;
					}
					if (!fl1 && !oneRow[location].equals("empty cell"))
						result.add(oneRow[location] + "");

				}

			}
		}
		return result;

	}

	public static int getIndexofColumn(String tableName, String colName) {
		int result = 0;
		File meta = new File("data/metadata.csv");
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String s = inputStream.nextLine();
				if (s.split(", ")[0].equals(tableName)) {
					if ((s.split(", ")[1]).equals(colName)) {
						result++;
						inputStream.close();
						return result;
					} else {
						result++;

					}
				}
			}
			inputStream.close();
			return -1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}

	// assuming parameters are 1-indexed
	public static int getBitPosition(int pageNumber, int posInPage) {
		return (pageNumber - 1) * DBApp.maxTuplesPerPage + posInPage - 1;

	}

	public static void updateOnDelete(int position, String tableName) throws IOException {
		String[] paths = new File("docs/bitmaps").list();
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(tableName)) {
				Vector<BitmapPair> list = getBitMapPair("docs/bitmaps/" + path);
				for (BitmapPair pair : list) {
					String bitmap = pair.bitmap;
					pair.bitmap = bitmap.substring(0, position) + bitmap.substring(position + 1);
				}
				writeCompressed(list, "docs/bitmaps/" + path);
			}
		}
	}

	static Vector<BitmapPair> getBitMapPair(String className) {
		Vector<BitmapPair> v;
		try {
			ObjectInputStream compressedInput = new ObjectInputStream(
					new BufferedInputStream(new GZIPInputStream(new FileInputStream(className))));
			v = (Vector<BitmapPair>) compressedInput.readObject();
			compressedInput.close();
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

	public static String getType(String tableName, String colName) {
		try (BufferedReader br = new BufferedReader(new FileReader("data/metadata.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(", ");

				if (values[0].equals(tableName) && values[1].equals(colName)) {

					if (values[2].equals("java.lang.String"))
						return "String";
					if (values[2].equals("java.lang.Integer"))
						return "Integer";
					if (values[2].equals("java.lang.Double"))
						return "Double";
					if (values[2].equals("java.lang.Boolean"))
						return "Boolean";

					else
						return "Date";

				}
			}
		} catch (Exception e) {

			System.out.println(e);
			return "";
		}
		return "";
	}

	public static void readBitmap(String tableName, String colName) throws FileNotFoundException {
		String[] paths = new File("docs/bitmaps").list();
		paths = DBApp.sortPaths(paths);
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(tableName) && splitted[1].equals(colName)) {

				Vector<BitmapPair> list = getBitMapPair("docs/bitmaps/" + path);

				for (BitmapPair pair : list) {
					System.out.println(pair.value + " " + pair.bitmap);

				}

			}
		}
	}

	public static ArrayList<Integer> getIndexOfIndexed(String tableName) {
		int result = 0;
		File meta = new File("data/metadata.csv");
		ArrayList<Integer> l = new ArrayList<Integer>();
		Boolean b = true;
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String s = inputStream.nextLine();
				if (s.split(", ")[0].equals(tableName)) {
					if ((s.split(", ")[4]).equals("True")) {
						l.add(result);
						result++;
					} else {
						result++;
					}
				}
			}
			inputStream.close();
			return l;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return l;
	}

	public static String getNameofColumn(String tableName, int colNumber) {
		File meta = new File("data/metadata.csv");
		int counter = 0;
		try {
			Scanner inputStream = new Scanner(meta);
			while (inputStream.hasNextLine()) {
				String s = inputStream.nextLine();
				String[] splitted = s.split(", ");
				if (!splitted[0].equals(tableName))
					continue;
				if (counter == colNumber) {
					inputStream.close();
					return s.split(", ")[1];
				}
				counter++;
			}
			inputStream.close();
			return "not found";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "not found";
	}

	public static boolean isInteger(String s) {
		try {
			Integer t = Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isDouble(String s) {
		try {
			Double t = Double.parseDouble(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isBoolean(String s) {
		if (s.equals("true") || s.equals("false")) {
			return true;
		}
		return false;
	}

	public static boolean isDate(String s) {
		try {
			Date t = new SimpleDateFormat("dd/MM/yyyy").parse(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static String[] sortPaths(String[] paths) {
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

	static Vector<BitmapPair> getNumberOfMaps(String className) throws IOException, ClassNotFoundException {
		Vector<BitmapPair> v;
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

	static void pushDown(String tableName, String colName, int index, BitmapPair m)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		String[] paths = new File("docs/bitmaps").list();
		String curPath = null;
		int pushed = 0;
		int counter = 0;
		int remainingFlag = 0;
		int enteries = 0;
		paths = sortPaths(paths);
		for (String path : paths) {
			String[] x = path.split("_");
			if (x[0].equals(tableName) && x[1].equals(colName)) {
				Vector<BitmapPair> list = getBitMapPair("docs/bitmaps/" + path);
				curPath = path;
				StringTokenizer st = new StringTokenizer(path, "_");
				String num = "";
				while (st.hasMoreTokens())
					num = st.nextToken();
				counter = Math.max(counter, Integer.parseInt(num));
				Vector<BitmapPair> newV = new Vector();
				Vector<BitmapPair> oldV = getBitMapPair("docs/bitmaps/" + curPath);
				if (remainingFlag == 1) {
					newV.add(m);
					remainingFlag = 0;
				}
				if (pushed == 0)
					if (enteries + oldV.size() < index) {
						enteries += oldV.size();
						continue;
					} else {
						int i = 0;
						while (true) {
							if (enteries + i == index) {
								newV.add(m);
								break;
							}
							newV.add(oldV.remove(0));
							i++;
						}
						pushed = 1;
					}
				newV.addAll(oldV);
				if (newV.size() > maxTuplesPerIndex) {
					m = (BitmapPair) newV.remove(newV.size() - 1);
					remainingFlag = 1;
				} else {
					remainingFlag = 0;
				}
				writeCompressed(newV, "docs/bitmaps/" + curPath);
			}
		}
		if (pushed == 0)
			remainingFlag = 1;
		if (remainingFlag == 1) {
			++counter;
			Vector<BitmapPair> v = new Vector();
			v.add(m);
			writeCompressed(v, "docs/bitmaps/" + tableName + "_" + colName + "_" + "index" + "_" + counter);
		}
	}

	public static void insertBitmap(BitmapPair newBitmap, String tableName, String colName)
			throws FileNotFoundException, IOException, ParseException, ClassNotFoundException {
		int loc = getIndex(tableName, colName, newBitmap.value);
		pushDown(tableName, colName, loc, newBitmap);
	}

	public static void updateOnInsert(int location, String tableName, Hashtable<String, Object> htblColNameValue)
			throws IOException, ParseException, ClassNotFoundException {
		ArrayList<Integer> whichIsIndexed = getIndexOfIndexed(tableName);
		Object[] tuple = new Object[htblColNameValue.size()];
		tuple = DBApp.getValueInOrder(tableName, htblColNameValue);
		String[] paths = new File("docs/bitmaps").list();
		paths = sortPaths(paths);
		boolean caught = false;
		int len = 0;
		for (int i = 0; i < whichIsIndexed.size(); i++) {
			caught = false;
			for (String path : paths) {
				String[] splitted = path.split("_");
				if (splitted[0].equals(tableName)
						&& splitted[1].equals(getNameofColumn(tableName, whichIsIndexed.get(i)))) {
					Vector<BitmapPair> list = getBitMapPair("docs/bitmaps/" + path);
					for (BitmapPair pair : list) {
						String bitmap = pair.bitmap;
						len = bitmap.length();
						// String before = bitmap.substring(0, start), after =
						// bitmap.substring(end, bitmap.length());
						String x = bitmap.substring(0, location), y = bitmap.substring(location, bitmap.length());
						String s = "";
						if ((pair.value.equals((tuple[whichIsIndexed.get(i) + 1]).toString()))) {
							s = x + "1" + y;
							caught = true;
						} else {
							s = x + "0" + y;
						}
						pair.bitmap = s;
					}
					writeCompressed(list, "docs/bitmaps/" + path);
				}
			}
			if (!caught) {
				caught = false;
				String s = "";
				for (int j = 0; j < location; j++) {
					s += "0";
				}
				s += "1";
				for (int j = location; j < len; j++) {
					s += "0";
				}
				if (tuple[whichIsIndexed.get(i) + 1].equals("empty cell")) {
					continue;
				}
				BitmapPair newBitmap = new BitmapPair(tuple[whichIsIndexed.get(i) + 1] + "", s);
				insertBitmap(newBitmap, tableName, getNameofColumn(tableName, whichIsIndexed.get(i)));
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int getIndex(String tableName, String colName, String value) throws ParseException {
		String[] paths = new File("docs/bitmaps").list();
		paths = sortPaths(paths);
		String type = getType(tableName, colName);
		int index = 0;
		for (String path : paths) {
			String[] splitted = path.split("_");
			if (splitted[0].equals(tableName) && splitted[1].equals(colName)) {
				Vector<BitmapPair> list = getBitMapPair("docs/bitmaps/" + path);
				for (BitmapPair pair : list) {
					String v = pair.value;
					if (isInteger(v)) {
						if (Integer.parseInt(v) >= Integer.parseInt(value)) {
							return index;
						}
					} else {
						if (isBoolean(v)) {
							if ((v.equals("false") && value.equals("false")) || v.equals("true")) {
								return index;
							}
						} else {
							if (isDouble(v)) {
								if ((Double.parseDouble(v)) >= (Double.parseDouble(value))) {
									return index;
								}
							} else {
								if (type.equals("Date")) {
									SimpleDateFormat formatter6 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
											Locale.ENGLISH);
									Date v1 = formatter6.parse(v), tmp = formatter6.parse(value);
									if (v1.compareTo(tmp) >= 0) {
										return index;
									}
								} else {
									if (v instanceof java.lang.String) {
										if (((String) v).compareTo((String) value) >= 0) {
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

}
