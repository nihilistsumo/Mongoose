#!/bin/bash
echo "Running Mongoose"
pagerunfilename=comb-top200-laura-cand-train
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ../../cand-sets/$pagerunfilename -page-run mongoose-results/simpara-$pagerunfilename
