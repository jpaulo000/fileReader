#CSV File Reader
This is a simple program with java swing interface to read files such as CSV 
(although it also works with .txt and any other with a header file).

##MainWindow.java
All the user interface remains here

##CSVReader.java
There are two main methods, readHeader and readContent.
* <b>readHeader</b> will get the first line in the file, use its content to show the available fields to handle
* <b>readContent</b> will read the content of the file to find out how many nulls are there in each field and 
the number of values in the selected fields. Also it will write its result into a file with input file name plus "_output.txt".
