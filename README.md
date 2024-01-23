#### Description 
Database engine with support for a Bitmap index and the following functionalities
- creating tables
- inserting tuples
- deleting tuples
- searching for tuples
- creating a bitmap index
- searching using bitmap index
- using SQLTerms


#### SQLTerm operators

| Operator Inside SQLTerm | Operator between SQLTerm |
|-------------------------|--------------------------|
| >                       | AND                      |
| >=                      | OR                       |
| <                       | XOR                      |
| <=                      |                          |
| !=                      |                          |
| =                       |                          |

#### Example for code that creates table and does operations on it

```
String strTableName = "Student";
Hashtable htblColNameType = new Hashtable( );
htblColNameType.put("id", "java.lang.Integer");
htblColNameType.put("name", "java.lang.String");
htblColNameType.put("gpa", "java.lang.double");
createTable( strTableName, "id", htblColNameType );
createBitmapIndex( strTableName, "gpa" );
Hashtable htblColNameValue = new Hashtable( );
htblColNameValue.put("id", new Integer( 2343432 ));
htblColNameValue.put("name", new String("Ahmed Noor" ) );
htblColNameValue.put("gpa", new Double( 0.95 ) );
insertIntoTable( strTableName , htblColNameValue );
htblColNameValue.clear( );
htblColNameValue.put("id", new Integer( 453455 ));
htblColNameValue.put("name", new String("Ahmed Noor" ) );
htblColNameValue.put("gpa", new Double( 0.95 ) );
insertIntoTable( strTableName , htblColNameValue );
htblColNameValue.clear( );
htblColNameValue.put("id", new Integer( 5674567 ));
htblColNameValue.put("name", new String("Dalia Noor" ) );
htblColNameValue.put("gpa", new Double( 1.25 ) );
insertIntoTable( strTableName , htblColNameValue );
htblColNameValue.clear( );
htblColNameValue.put("id", new Integer( 23498 ));
htblColNameValue.put("name", new String("John Noor" ) );
htblColNameValue.put("gpa", new Double( 1.5 ) );
insertIntoTable( strTableName , htblColNameValue );
htblColNameValue.clear( );
htblColNameValue.put("id", new Integer( 78452 ));
htblColNameValue.put("name", new String("Zaky Noor" ) );
htblColNameValue.put("gpa", new Double( 0.88 ) );
insertIntoTable( strTableName , htblColNameValue );
SQLTerm[] arrSQLTerms;
arrSQLTerms = new SQLTerm[2];
arrSQLTerms[0]._strTableName = "Student";
arrSQLTerms[0]._strColumnName= "name";
arrSQLTerms[0]._strOperator = "=";
arrSQLTerms[0]._objValue = "John Noor";
arrSQLTerms[1]._strTableName = "Student";
arrSQLTerms[1]._strColumnName= "gpa";
arrSQLTerms[1]._strOperator = "=";
arrSQLTerms[1]._objValue = new Double( 1.5 );
String[]strarrOperators = new String[1];
strarrOperators[0] = "OR";
// select * from Student where name = “John Noor” or gpa = 1.5;
Iterator resultSet = selectFromTable(arrSQLTerms , strarrOperators); 
```

#### Disclaimer
The commits/LOC do not reflect the contribution done by each member of this repo due to not using Github properly at that time. All the contributors and the collaborators in this repository worked together to deliver the final product.
