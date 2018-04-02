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

3. Change permission of the run.sh script:
```
chmod 755 run.sh
```

4. Run the installation script:
```
./run.sh
```

5. Run Mongoose (with default options):
```
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Usage
You can change various parameters and options through project.properties files located inside Mongoose directory. Following are some of the important option/variable names and their descriptions:
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
