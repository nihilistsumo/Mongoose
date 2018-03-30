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
			MongooseHelper mh = new MongooseHelper(prop);
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
					if(args[10].equals("BM25"))
					{
						System.out.println("Using BM25 for candidate set generation");
						sim = new BM25Similarity();
					}
					else if(args[10].equals("LM-DS"))
					{
						System.out.println("Using LM-DS for candidate set generation");
						sim = new LMDirichletSimilarity();
					}
					else if(args[10].equals("LM-JM"))
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
					mh.runQueryExpand(args[0], args[1], args[2], args[3], args[4], args[5], 
							Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), 
							args[9], args[10], new StandardAnalyzer(), sim);
				}
				else if(cmd.equalsIgnoreCase("prc")){
					mh.runPRC(prop.getProperty("out-dir")+"/"+prop.getProperty("cluster-out"), prop.getProperty("index-dir"), 
							prop.getProperty("curl-path"));
				}
			}
		} catch (IOException | ParseException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
