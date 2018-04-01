#!/bin/bash
#echo "Cloning Git Repository...."
#git clone https://github.com/nihilistsumo/Mongoose.git
#echo "Done"
#cd Mongoose
mvn package
echo "Done compiling and packaging"
#cd ..
echo "Adding necessary configuration files and wordnet database..."
cp /home/mong/similarity.conf /home/mong/jawjaw.conf /home/mong/wnjpn.db .
jar uf target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar similarity.conf jawjaw.conf wnjpn.db
rm similarity.conf jawjaw.conf wnjpn.db
echo "Done"
#cd Mongoose
mkdir ../mongoose-results
echo "Now running code..."
#java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar
