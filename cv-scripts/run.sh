#!/bin/bash

trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
#trecdir=/home/sk1105/sumanta/trec-methods/benchmarkY1/benchmarkY1-train
cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
#cvdir=/home/sk1105/sumanta/trec-methods/cv-results
jardir=/home/sumanta/git/Mongoose/target
#jardir=/home/sk1105/sumanta/trec-methods/Mongoose/target
jarfile=trec-car-methods-0.9-jar-with-dependencies.jar
class=edu.unh.cs.lucene.TrecCarLuceneQuery

#indexes
paraindex=../paragraph-corpus-paragraph-index
pageindex=../paragraph-corpus-page-index
entindex=../paragraph-corpus-entity-index
aspindex=../paragraph-corpus-aspect-index

# Generating run files
echo "Generating run files"

# paragraph with paragraph.lucene index
type=paragraph
level=page
for querylevel in title all
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $paraindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done
level=section
for querylevel in sectionPath all subtree title leafheading interior
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $paraindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done


# page with page.lucene index
type=page
level=page
for querylevel in title all
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $pageindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done
level=section
for querylevel in sectionPath all subtree title leafheading interior
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $pageindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done


# entity with entity.lucene index
type=entity
level=page
for querylevel in title all
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $entindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done
level=section
for querylevel in sectionPath all subtree title leafheading interior
do
	for retmodel in bm25 ql
	do
		for expmodel in none rm ecm ecm-rm
		do
			for analyzer in std english
			do
				for fold in {0..4}
				do
					java -cp $jardir/$jarfile $class $type $level run $trecdir/fold-$fold-train.pages.cbor-outlines.cbor $entindex $cvdir/fold-$fold-$type-$level-$querylevel-$retmodel-$expmodel-$analyzer-run \
					$querylevel $retmodel $expmodel $analyzer 1000
				done
			done
		done
	done
done