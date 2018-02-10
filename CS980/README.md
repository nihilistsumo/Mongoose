# CS980
Repository for CS 980 Data Science class at UNH

## To install and run this code:  
Build the project using maven as: mvn clean install

## Instructions
There are two options with which you can run this software. Using option -b builds the index whereas using option -s searches the index. When using option -s, there are two modes in which the search can be done. Use option -p after -s to search the index for page queries and use option -s after -s to search the index for section queries.
### When using the -b option 
java -jar $jar file$ -b $path to index directory$ $path to directory containing paragrapgh cbor file$ 
### When using -s option
java -jar $jar file$ -s -p $path to index directory$ $path to output directory$ $path to cbor outline file$ $name of paragragh run file$ $top how many results$ 

java -jar $jar file$ -s -s $path to index directory$ $path to output directory$ $path to cbor outline file$ $name of section run file$ $top how many results$ 
