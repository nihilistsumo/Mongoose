#!/bin/bash

cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
jardir=/home/sumanta/git/Mongoose-basic-fix/target
rlib=~/Softwares
level=hierarchical

for m in bm25 bool classic lmds
do
	echo "Starting $m"
	for fold in {0..4}
	do
		echo "Fold $fold of $m"
		java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -r fold-$fold-train.pages.cbor-outlines.cbor $cvdir/fold$fold-$m-run hier $m 500
	done
	echo "Finished $m"
done

mkdir $cvdir/runs
mkdir $cvdir/models
mkdir $cvdir/comb-runs
cp $cvdir/*-run $cvdir/runs/
rm $cvdir/*-run

for fold in {0..4}
do
	mkdir $cvdir/runs/tmp-leave$fold
	cp $cvdir/runs/*-run $cvdir/runs/tmp-leave$fold/
	rm $cvdir/runs/tmp-leave$fold/fold$fold-*-run
	for m in bm25 bool classic lmds
	do
		cat $cvdir/runs/tmp-leave$fold/*-$m-run > $cvdir/runs/tmp-leave$fold/comb-$m-run
		rm $cvdir/runs/tmp-leave$fold/fold*-$m-run
	done
done	

for fold in {0..4}
do
	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb $cvdir/runs/tmp-leave$fold $cvdir/runs/tmp-leave$fold/fet-file fold-$fold-train.pages.cbor-$level.qrels
	java -jar $rlib/RankLib-2.1-patched.jar -train $cvdir/runs/tmp-leave$fold/fet-file -ranker 4 -metric2t MAP -save $cvdir/models/fold$fold-rlib-model
	rm $cvdir/runs/tmp-leave$fold/fet-file
	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmbrun $cvdir/runs/tmp-leave$fold $cvdir/models/fold$fold-rlib-model $cvdir/comb-runs/fold-$fold-comb-run
done

cat $cvdir/comb-runs/* > $cvdir/comb-runs/cv-comb-run
rm $cvdir/comb-runs/fold*-run

echo "Cross validation complete"