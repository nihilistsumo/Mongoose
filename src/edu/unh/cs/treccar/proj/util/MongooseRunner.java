package edu.unh.cs.treccar.proj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import edu.unh.cs.treccar.proj.rank.LuceneRanker;
import edu.unh.cs.treccar.proj.rlib.RLibFileWriterForCluster;
import edu.unh.cs.treccar.proj.sum.SummaryMapper;

public class MongooseRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(new File("project.properties")));
			MongooseHelper mh = new MongooseHelper(prop, args[0]);
			
			//To process similarity data between para pair
			// -p candidate-run-file-path parasim-out-file-path
			if(args[0].equalsIgnoreCase("-p")) {
				String candSetFilePath = args[1];
				String simDataOutFilePath = args[2];
				mh.saveParaSimilarityData(mh.processParaPairData(DataUtilities.getPageParaMapFromRunfile(candSetFilePath)), simDataOutFilePath);
			}
			//To create rlib feature file from hier qrels and simdata file
			// -rl simdata-path rlib-fet-file-out-path 
			else if(args[0].equalsIgnoreCase("-rl")) {
				RLibFileWriterForCluster rlib = new RLibFileWriterForCluster(mh);
				//rlib.processParaForRlib();
				String simdataPath = args[1];
				String rlibFetFileOutPath = args[2];
				rlib.writeFeatureFile(simdataPath, rlibFetFileOutPath);
			}
			//HACsim clustering
			// -hacsim candidate-run-file-path parasim-out-file-path rlib-model-path cluster-out-path
			else if(args[0].equalsIgnoreCase("-hacsim")) {
				String candSetFilePath = args[1];
				String simDataOutFilePath = args[2];
				String rlibModelPath = args[3];
				String clusterOutPath = args[4];
				mh.runHACSimClustering(candSetFilePath, simDataOutFilePath, rlibModelPath, clusterOutPath);
			}
			/*
			for(String cmd:prop.getProperty("mode").split("-")){
				if(cmd.equalsIgnoreCase("ir")){
					LuceneRanker lr = new LuceneRanker(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+prop.getProperty("parafile"), 
							prop.getProperty("data-dir")+"/"+prop.getProperty("outline"), prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"), 
							Integer.parseInt(prop.getProperty("no-ret")), "index");
					lr.doRanking();
				}
				else if(cmd.equalsIgnoreCase("r")){
					LuceneRanker lr = new LuceneRanker(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+prop.getProperty("parafile"), 
							prop.getProperty("data-dir")+"/"+prop.getProperty("outline"), prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"), 
							Integer.parseInt(prop.getProperty("no-ret")), "no-index");
					lr.doRanking();
				}
				else if(cmd.equalsIgnoreCase("p")){
					mh.saveParaSimilarityData(mh.processParaPairData(DataUtilities.getPageParaMapFromRunfile(
							prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"))),
							prop.getProperty("out-dir")+"/"+prop.getProperty("sim-data-out"));
				}
				else if(cmd.equalsIgnoreCase("l")){
					RLibFileWriterForCluster rlib = new RLibFileWriterForCluster(mh);
					rlib.processParaForRlib();
					rlib.writeFeatureFile();
				}
				else if(cmd.equalsIgnoreCase("c")){
					mh.runHACSimClustering();
				}
				else if(cmd.equalsIgnoreCase("cw")){
					mh.runHACW2VClustering();
				}
				else if(cmd.equalsIgnoreCase("km")){
					mh.runKMeansW2VClustering();
				}
				else if(cmd.equalsIgnoreCase("pm")){
					mh.runParaMapper();
				}
				else if(cmd.equalsIgnoreCase("cm")){
					mh.runClusteringMeasure();
				}
				else if(cmd.equalsIgnoreCase("cb")){
					mh.convertClusterDataToText();
				}
				else if(cmd.equalsIgnoreCase("pt")){
					mh.runPlainTextExtractor();
				}
				else if(cmd.equalsIgnoreCase("cmb")){
					mh.combineRunfilesForRLib();
				}
				else if(cmd.equalsIgnoreCase("sm")){
					SummaryMapper sm = new SummaryMapper(prop);
					sm.map(prop);
				}
				else if(cmd.equalsIgnoreCase("qe")){
					Similarity sim = null;
					if(prop.getProperty("cs-method").equals("BM25"))
					{
						System.out.println("Using BM25 for candidate set generation");
						sim = new BM25Similarity();
					}
					else if(prop.getProperty("cs-method").equals("LM-DS"))
					{
						System.out.println("Using LM-DS for candidate set generation");
						sim = new LMDirichletSimilarity();
					}
					else if(prop.getProperty("cs-method").equals("LM-JM"))
					{
						System.out.println("Using LM-JM for candidate set generation");
						float lambda = Float.parseFloat(args[11]);
						sim = new LMJelinekMercerSimilarity(lambda);
					}
					else
					{
						System.out.println("Using BM25 as default for candidate set generation");
						sim = new BM25Similarity();
					}
					mh.runQueryExpand(prop, new StandardAnalyzer(), sim);
				}
				else if(cmd.equalsIgnoreCase("prc")){
					mh.runPRC(prop.getProperty("out-dir")+"/"+prop.getProperty("cluster-out"), prop.getProperty("index-dir"), 
							prop.getProperty("curl-path"), prop.getProperty("out-dir")+"/"+prop.getProperty("paramap-run"));
				}
			}
			*/
		} catch (IOException | ParseException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}