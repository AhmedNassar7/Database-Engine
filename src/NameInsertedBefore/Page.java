package NameInsertedBefore;

import java.io.*;
import java.util.*;

public class Page implements Serializable {
	Vector<Object[]> rows;

	public static void main(String[] args) {
		try {
			FileInputStream fileIn = new FileInputStream("docs/pages/Student_1");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Vector<Object[]> x = (Vector<Object[]>) in.readObject();
			for (int i = 0; i < x.size(); i++) {
				Object[] y = x.get(i);
				for (Object obj : y)
					System.out.print(obj + " ");
				System.out.println();
			}
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return;
		}

	}
}