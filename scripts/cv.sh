#!/bin/bash

rlib=~/Softwares
#rlib=/home/sk1105/sumanta

trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
#trecdir=/home/sk1105/sumanta/trec-methods/benchmarkY1/benchmarkY1-train
cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
#cvdir=/home/sk1105/sumanta/trec-methods/cv-results
jardir=/home/sumanta/git/Mongoose/target
#jardir=/home/sk1105/sumanta/trec-methods/Mongoose/target
jarfile=trec-car-methods-0.9-jar-with-dependencies.jar
class=edu.unh.cs.lucene.TrecCarLuceneQuery
qrelslevel=hierarchical

#indexes
paraindex=../paragraph-corpus-paragraph-index
pageindex=../paragraph-corpus-page-index
entindex=../paragraph-corpus-entity-index
aspindex=../paragraph-corpus-aspect-index

# Generating run files

#for m in bm25 bool classic lmds
#do
#	echo "Starting $m"
#	for fold in {0..4}
#	do
#		echo "Fold $leavefold of $m"
#		java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -r fold-$leavefold-train.pages.cbor-outlines.cbor $cvdir/fold$leavefold-$m-run hier $m 500
#	done
#	echo "Finished $m"
#done

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
			for expmodel in none rm ecm ecm-rm
			do
				for analyzer in std english
				do
					cat $cvdir/runs/tmp-leave$leavefold/*-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run >> $cvdir/runs/tmp-leave$leavefold/comb-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run
				done
			done
		done
	done
	
	#for m in bm25 bool classic lmds
	#do
		#cat $cvdir/runs/tmp-leave$leavefold/*-$m-run >> $cvdir/runs/tmp-leave$leavefold/comb-$m-run
		#rm $cvdir/runs/tmp-leave$leavefold/fold*-$m-run
	#done
done

# Ranklib	

for leavefold in {0..4}
do
	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb $cvdir/runs/tmp-leave$leavefold $cvdir/runs/tmp-leave$leavefold/fet-file $cvdir/qrels/leave$leavefold.qrels
	java -jar $rlib/RankLib-2.1-patched.jar -train $cvdir/runs/tmp-leave$leavefold/fet-file -ranker 4 -metric2t MAP -save $cvdir/models/fold$leavefold-rlib-model
	rm $cvdir/runs/tmp-leave$leavefold/fet-file
done

# Combining runfiles using rlib models

for leave in {0..4}
do
	mkdir $cvdir/tmp-runs$leave
	cp $cvdir/runs/fold$leave-*-run $cvdir/tmp-runs$leave
	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmbrun $cvdir/tmp-runs$leave $cvdir/models/fold$leave-rlib-model $cvdir/comb-runs/fold-$leave-comb-run
	rm -r $cvdir/tmp-runs$leave
done

cat $cvdir/comb-runs/* >> $cvdir/comb-runs/cv-comb-run
rm $cvdir/comb-runs/fold*-run

java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmboptw $cvdir/models

echo "Cross validation complete"
