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

import edu.unh.cs.treccar.proj.qe.QueryIndex;
import edu.unh.cs.treccar.proj.rank.LuceneRanker;
import edu.unh.cs.treccar.proj.rlib.RLibFileWriterForCluster;
import edu.unh.cs.treccar.proj.sum.SummaryMapper;
import edu.unh.cs.treccar.proj.tm.TopicModelMapper;

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
			//HACw2v clustering
			// -hacwv candidate-run-file-path cluster-out-path
			else if(args[0].equalsIgnoreCase("-hacwv")){
				String candSetFilePath = args[1];
				String clusterOutPath = args[2];
				mh.runHACW2VClustering(candSetFilePath, clusterOutPath);
			}
			//KMw2v clustering
			// -kmwv candidate-run-file-path cluster-out-path
			else if(args[0].equalsIgnoreCase("-kmwv")){
				String candSetFilePath = args[1];
				String clusterOutPath = args[2];
				mh.runKMeansW2VClustering(candSetFilePath, clusterOutPath);
			}
			//Evaluate clustering
			// -cm cluster-file-path
			else if(args[0].equalsIgnoreCase("-cm")){
				String clusterFilePath = args[1];
				mh.runClusteringMeasure(clusterFilePath);
			}
			
			else if(args[0].equalsIgnoreCase("-pm")){
				String clusterFilePath = args[1];
				String outputRunfilePath = args[2];
				mh.runParaMapper(clusterFilePath, outputRunfilePath);
			}
			//Combine run files to produce rlib feature file
			// -cmb runfiles-directory-path fet-file-output-path page-level?true/false
			else if(args[0].equalsIgnoreCase("-cmb")){
				String runfilesDir = args[1];
				String outputFetFilePath = args[2];
				boolean pageLevel = false;
				if(args[3].startsWith("T") || args[3].startsWith("t"))
					pageLevel = true;
				mh.combineRunfilesForRLib(runfilesDir, outputFetFilePath, pageLevel);
			}
			//Combine run files to produce rank file using trained Rlib model
			// -cmbrun folder path to run files, filepath to rlib model, filepath to output runfile
			else if(args[0].equalsIgnoreCase("-cmbrun")){ 
				String runfilesDir = args[1];
				String rlibModelPath = args[2];
				String outputRunfilePath = args[3];
				CombineRunFilesUsingRlibModel cmb = new CombineRunFilesUsingRlibModel();
				cmb.writeRunFile(prop, runfilesDir, rlibModelPath, outputRunfilePath);
			}
			//Produces hierarchical runfiles using summarizer
			// -sm candidate-run-file-path output-run-file-path
			else if(args[0].equalsIgnoreCase("-sm")){
				String candSetFilePath = args[1];
				String outputRunfilePath = args[2];
				SummaryMapper sm = new SummaryMapper(prop);
				sm.map(prop, candSetFilePath, outputRunfilePath);
			}
			//Produces hierarchical runfiles using LDA Topic Model
			// -tm candidate-run-file-path output-run-file-path mode parallel>0
			else if(args[0].equalsIgnoreCase("-tm")){
				String candSetFilePath = args[1];
				String outputRunfilePath = args[2];
				int mode = Integer.parseInt(args[3]);
				int parallel = Integer.parseInt(args[4]);
				TopicModelMapper tmm = new TopicModelMapper();
				tmm.map(prop, candSetFilePath, outputRunfilePath, mode, parallel);
			}
			//Query expand methods
			// -qe output-dir-path output-runfilename
			else if(args[0].equalsIgnoreCase("-qe")){
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
				mh.runQueryExpand(prop, args[1], args[2], new StandardAnalyzer(), sim);
			}
			// -bm25 outputdir outputfile 200 mode method 0.1 
			else if(args[0].equalsIgnoreCase("-bm25")){
				mh.runBM25Ret(prop, args[1], args[2], Integer.parseInt(args[3]), args[4], args[5], Float.parseFloat(args[6]));
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