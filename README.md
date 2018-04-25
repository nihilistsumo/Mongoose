# Mongoose
A class project for CS980

The purpose of this project is to solve the TREC-CAR paragraph task.

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

## Usage
Running the script **run.sh** tests 7 variations with default settings of our project on train dataset and reports the treceval results. However if you wish to run a single method with custom parameters, you have to run the generated jar file **Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar** with command line arguments. Following is a detailed description of the same.

Available methods, options and their corresponding arguments:

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

Also you can change various parameters and options through **_project.properties_** files located inside Mongoose directory.
```
data-dir=path to benchmarkY1train/test directory

out-dir=path to output directory where Mongoose outputs will be stored

index-dir=path to lucene index directory

parafile=paragraph cbor filename

outline=outline cbor filename

art-qrels=article.qrels filename

top-qrels=top-level.qrels filename

hier-qrels=hierarchical.qrels filename

trec-runfile=article.qrels compatible run filename

paramap-run=top-level/hierarchical.qrels compatible filename

mode=this controls which modules of Mongoose are to be run and in which order
```
Following options are available for mode

-ir = index then rank, -r = only rank, -p = process parapair similarity data, 
-l = rlib fet file writing, -c = hac sim cluster, -pm = para mapper, -cm = measure clustering
-km = kmeans clustering, -cw = hac word2vec cluster, -cb = convert cluster data to plain text file
-pt = plain text extractor, -cmb = combine run files and create a feature file for RLib,
-sm = summary mapper, -qe = candidate generation using query expansion

For example: mode=-qe-sm (default) will first generate candidate set using query expansion and then using that generate run file for top-level sections using summarization methods.

Following options are available for query expansion ( option: "qe-method" in project.properties) :

RM3 = Relevance Model 3 (default)

KNN-PRF = KNN with Pseudo-Relevance Feedback (PRF)

KNN-INC = Incremental KNN with PRF

KNN-EXT = KNN with extended query set with PRF

Each of the above query expansion methods can work on either page level, section level, or top-level section queries (option: "search-mode" in project.properties). The available modes are:

page-title: Searches for page titles as queries (default)

section-path : Searches for section headings as queries

toplevel-sections : Searches for toplevel sections as queries

## Examples

The default properties generates the candidate set using BM25 and Query Exoansion using RM3 with Pseudo-Relevance Feedback(PRF) and maps paragraphs to section headings using text summarization. If you want to change these settings, for example, Candidate generation with RM3+BM25 and text summarizing with word2vec, change the option "sum-map-method" to "w2v". Also don't forget to change the name of the output file for top level sections in the option "paramap-run" and for the candidate set in the option "trec-run-file" to your preferred name,  otherwise you will overwrite any previous run files. This would generate two run files: one for the candidate set generation and other for the top level sections. You can then evaluate them using trec_eval. If you want to do: Candidate set generation with KNN+BM25 and text summarizing with wordnet. Change the "qe-method" to "KNN-PRF". Also change names of output files as described above. This would again produce run files as descibed above which you can evaluate.  
