# CS980
Repository for CS 980 Data Science class at UNH

## To install and run this code:  
1. Build the project using maven as: mvn clean install
2. Download the TREC-CAR version 2.0 datasets from http://trec-car.cs.unh.edu/datareleases/ 
    
    2.1. paragraphCorpus  (as corpus)
    
    2.2. benchmarkY1train (as queries)
    
3. Create directories for storing the index and the outputs. 

## Usage Options
-u: Display usage
-b: Build Index");
-sp: Search Index for Page queries
-ss: Search Index for Section queries
-pr: Run PageRank Algorithm on a Graph
-ppr: Run PersonalisedPageRank Algorithm on a Graph

## Usage Syntax 
java -jar $jar file$ -b $path to index directory$ $path to directory containing paragrapgh cbor file$

java -jar $jar file$ -sp $path to index directory$ $path to output directory$" "$path to cbor outline file$ $name of paragragh run file$ $top how many results$

java -jar $jar file$ -ss $path to index directory$ $path to output directory$" "$path to cbor outline file$ $name of section run file$ $top how many results$

java -jar $jar file$ -pr $path to graph file$ $value of random jump (alpha)$

java -jar $jar file$ -ppr $path to graph file$ $value of random jump (alpha)$ $size of seed set$ $seed values$

