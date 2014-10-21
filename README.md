The project is just a small implementation of a Java iterator that iterates over the lines of contained in a text file. The _BufferedReader_ class can be used to easily retrieve the lines in a file using the _readLine_ method, however it doesn't return any information about the number of line-delimiters that have been skipped when reading a line.

The iterator returns a _Line_ object, that contains the content of the read line along with the total amount of characters read. Like the _BufferedReader_ class, it does this using a buffered approach to limit the amount of reads from the disk.
