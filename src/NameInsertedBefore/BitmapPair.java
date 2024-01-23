package NameInsertedBefore;

import java.io.Serializable;

public class BitmapPair implements Serializable {
	String value, bitmap;

	BitmapPair(String a, String b) {
		value = a;
		bitmap = b;
	}

	public String toString() {
		return "Value: " + value + " Bitmap: " + bitmap + '\n';
	}
}