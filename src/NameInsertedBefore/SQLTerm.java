package NameInsertedBefore;

public class SQLTerm {
	String strTableName;
	String strColumnName;
	String strOperator;
	Object objValue;

	public SQLTerm() {

	}

	public SQLTerm(String strTableName, String strColumnName, String strOperator, Object objValue) {
		this.strTableName = strTableName;
		this.strColumnName = strColumnName;
		this.strOperator = strOperator;
		this.objValue = objValue;
	}
}
