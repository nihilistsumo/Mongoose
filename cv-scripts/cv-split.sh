#!/bin/bash

#rlib=~/Softwares
rlib=/home/sk1105/sumanta

#trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
trecdir=/home/sk1105/sumanta/trec-methods/benchmarkY1/benchmarkY1-train
#cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
cvdir=/home/sk1105/sumanta/trec-methods/cv-results-passage/cv-results-new
#jardir=/home/sumanta/git/Mongoose/target
jardir=/home/sk1105/sumanta/trec-methods/Mongoose/target
jarfile=trec-car-methods-0.9-jar-with-dependencies.jar
class=edu.unh.cs.lucene.TrecCarLuceneQuery
qrelslevel=hierarchical


# Creating directories to store stuffs

mkdir $cvdir/runs
mkdir $cvdir/qrels
mkdir $cvdir/models
mkdir $cvdir/comb-runs
cp $cvdir/*-run $cvdir/runs/
rm $cvdir/*-run

# Creating leave one qrels and run files

for leavefold in {0..4}
do
	mkdir $cvdir/runs/tmp-leave$leavefold
	cp $cvdir/runs/*-run $cvdir/runs/tmp-leave$leavefold/
	rm $cvdir/runs/tmp-leave$leavefold/fold-$leavefold-*-run
	for f in {0..4}
	do
		if [ ! $f -eq $leavefold ]
		then
			cat $trecdir/fold-$f-train.pages.cbor-$qrelslevel.qrels >> $cvdir/qrels/leave$leavefold.qrels
		fi
	done
	
	type=paragraph
	level=section
	for querylevel in sectionPath all subtree title leafheading interior
	do
		for retmodel in bm25 ql
		do
			for expmodel in none rm
			do
				cat $cvdir/runs/tmp-leave$leavefold/*-$type-$level-$querylevel-$retmodel-$expmodel-std-run >> $cvdir/runs/tmp-leave$leavefold/comb-$type-$level-$querylevel-$retmodel-$expmodel-std-run
			done
		done
	done
	rm $cvdir/runs/tmp-leave$leavefold/fold-*-run
done
