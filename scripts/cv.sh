#!/bin/bash

trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
#trecdir=/home/sk1105/sumanta/cs980assign/benchmarkY1/benchmarkY1-train
cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
#cvdir=/home/sk1105/sumanta/mongoose-cv/cv-results
jardir=/home/sumanta/git/Mongoose/target
#jardir=/home/sk1105/sumanta/mongoose-cv/Mongoose/target
rlib=~/Softwares
#rlib=/home/sk1105/sumanta

# Generating run files

#for m in bm25 bool classic lmds
#do
#	echo "Starting $m"
#	for fold in {0..4}
#	do
#		echo "Fold $fold of $m"
#		java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -r fold-$fold-train.pages.cbor-outlines.cbor $cvdir/fold$fold-$m-run hier $m 500
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

for fold in {0..4}
do
	mkdir $cvdir/runs/tmp-leave$fold
	cp $cvdir/runs/*-run $cvdir/runs/tmp-leave$fold/
	rm $cvdir/runs/tmp-leave$fold/fold$fold-*-run
	for f in {0..4}
	do
		if [ ! $f -eq $fold ]
		then
			cat $trecdir/fold-$f-train.pages.cbor-$level.qrels >> $cvdir/qrels/leave$fold.qrels
		fi
	done
	for m in bm25 bool classic lmds
	do
		cat $cvdir/runs/tmp-leave$fold/*-$m-run >> $cvdir/runs/tmp-leave$fold/comb-$m-run
		rm $cvdir/runs/tmp-leave$fold/fold*-$m-run
	done
done

# Ranklib	

for fold in {0..4}
do
	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb $cvdir/runs/tmp-leave$fold $cvdir/runs/tmp-leave$fold/fet-file $cvdir/qrels/leave$fold.qrels
	java -jar $rlib/RankLib-2.1-patched.jar -train $cvdir/runs/tmp-leave$fold/fet-file -ranker 4 -metric2t MAP -save $cvdir/models/fold$fold-rlib-model
	rm $cvdir/runs/tmp-leave$fold/fet-file
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
