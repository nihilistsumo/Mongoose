# Mongoose
A class project for CS980

The purpose of this project is to solve the TREC-CAR paragraph task.

Follow the **Installation**  instructions to **install** and **Usage** instructions to **run** the prototype.

## Installation
1. Clone this project:
```
git clone https://github.com/nihilistsumo/Mongoose.git
```

2. Change to Mongoose directory:
```
cd Mongoose
```

3. Change permission of the scripts:
```
chmod 755 install.sh run.sh
```

4. Run the installation script:
```
./install.sh
```
Now run ./run.sh to test the project as described in the Usage section below.

## Usage

1. Running the script **run.sh** tests 7 variations with default settings of our project on train dataset and reports the treceval results. 

2. However, if you wish to run a single method with custom parameters, you have to run the generated jar file **Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar** with command line arguments and changing the appropriate variables in the **project.properties** file. (See details below)

3. The results are stored in the directory **mongoose-results** which is created in the same directory as the one in which you intall the  prototype.


### Available methods, options and their corresponding arguments for running jar file:

1. -p calculates wordnet similarity between all paragraph pairs

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p path-to-candidate-set-pagerun-file path-to-output-file
```
This will calculate wordnet similarity between all the paragraph pairs inside each page mentioned in the candidate set and save the data as a serialized object in a file.

2. -hacsim runs HAC with wordnet similarity

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacsim path-to-candidate-set-pagerun-file path-to-parapair-similarity-data path-to-rlib-trained-model-file path-to-output-file
```
This will cluster paragraphs for each page in candidate set using HAC with wordnet similarity and using the optimized weight vector for each similarity from the rlib model file and save the results.

3. -hacwv runs HAC with word2vec cosine similarity

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacwv path-to-candidate-set-pagerun-file path-to-output-file
```
This will cluster paragraphs for each page in candidate set using HAC with word2vec feature vectors obtained from glove pretrained model and save the results.

4. -kmwv runs KMeans with word2vec feature vector

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -kmwv path-to-candidate-set-pagerun-file path-to-output-file
```
This will cluster paragraphs for each page in candidate set using KMeans with word2vec feature vectors obtained from glove pretrained model and save the results.

5. -cm calculate Adjusted RAND index of clusters

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm path-to-cluster-output-file
```
6. -pm map clusters to sections using word2vec feature vector

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm path-to-cluster-output-file path-to-output-runfile
```
This will map paragraphs in clusters in a page to the sections using word2vec features and save the results in a runfile format.

7. -sm map paragraphs in the candidate set to sections using paragraph summarization

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -sm path-to-candidate-set-pagerun-file path-to-output-runfile
```
This will map paragraphs in candidate set of a page to the sections using paragraph summaries and save the results in a runfile format.

8. -tm map paragraphs in the candidate set to sections using LDA topic model

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -tm path-to-candidate-set-pagerun-file path-to-output-runfile mode
```
9. -cmb generate Ranklib feature file out of run files and qrels file

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb path-to-runfiles-to-be-combined path-to-output-feature-file pagelevel?false/true
```

10. -cmbrun generate combined runfile from Rlib model and individual run files

```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmbrun folder-path-to-run-files filepath-to-rlib-model filepath-to-output-runfile
```

##Options in project.properties

Various parameters and options for **_project.properties_** file located inside Mongoose directory: 
```
1. data-dir=path to benchmarkY1train/test directory

2. index-dir=path to lucene index directory

3. parafile=paragraph cbor filename

4. outline=outline cbor filename for train/test

5. art-qrels=article.qrels filename

6. top-qrels=top-level.qrels filename

7. hier-qrels=hierarchical.qrels filename

8. no-ret=number of paragraphs retrieved during candidate generation

9. func=space separated options for wordnet similairties used for clustering. valid options ji - Jiang-Conrath, pat - Path, wu - Wu-Palmer, lin - Lin

10. log-file=not used

11. show-msg=to enable verbose output (yes/no)

12. threads=number of threads for multithreading tasks

13. use-default-poolsize=whether to use default thread pool size (yes/no)

14. glove-file=pretrained glove file to be used

15. plaintextpara=not used

16. parapair-qrels=not used

17. cluster-out-txt=not used

18. sum-method=summarization method, currently only valid option: tfidf

19. sum-map-method=mapping method after summarization, valid options: tfidf/wn

20. curl-path=path to curl script: scripts/curl_command.sh

21. stopfile=filename storing the stopwords

22. qe-method=query expansion method

23. cs-method=BM25

24. search-mode = section-path

```

For example: mode=-qe-sm (default) will first generate candidate set using query expansion and then using that generate run file for top-level sections using summarization methods.

Following options are available for query expansion ( option: "qe-method" in project.properties) :

1. RM3 = Relevance Model 3 (default)

2. KNN-PRF = Simple KNN with Pseudo-Relevance Feedback (PRF)

3. KNN-INC = Incremental KNN with PRF

4. KNN-EXT = KNN with extended query set with PRF

For example, if you want to run query expansion with option (3), then change the value of the field "qe-method" in the properties file to "KNN-INC".

Each of the above query expansion methods can work on either page level, section level, or top-level section queries (option: "search-mode" in project.properties). The available modes are:

1. page-title: Searches for page titles as queries (default)

2. section-path : Searches for section headings as queries

3. toplevel-sections : Searches for toplevel sections as queries

For example, if you want to run query expansion on section headings as queries, then change the value of the field "search-mode" in the properties file to "section-path".

Each of the above query expansion methods works with PRF which uses one of the following methods to retrieve a candidate set of top paragrapghs (option: "cs-method" in project.properties) :

1. BM25 : Standard BM25 retrieval model (default)

2. LM-DS : Standard Language Model with Dirichlet Smoothing

3. LM-JM : Standard Language Model with Jelenik-Mercer Smoothing 

For example, if you want to generate candidate set for PRF using LM-DS, then change the value of the field "cs-method" in the properties file to "LM-DS". 

In summary, suppose you want to run the variation: "Candidate generation using LM-DS with PRF, query expansion with simple KNN on top-level sections" then you need to make the following changes to the properties file:

qe-method = KNN-PRF

cs-method = LM-DS

search-mode = section-path 
