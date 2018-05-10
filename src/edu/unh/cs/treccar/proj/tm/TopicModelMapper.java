package edu.unh.cs.treccar.proj.tm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.wordnet.SynonymMap;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.unh.cs.treccar.proj.sum.SummaryMapper;
import edu.unh.cs.treccar.proj.util.DataUtilities;

public class TopicModelMapper {
	
	private final static double ALPHA_SUM = 1.0;
	private final static double BETA = 0.01;
	private final static int ITERATIONS = 1000;
	private final static int NUM_THREADS_TOPIC_MODEL = 5;
	private final static int ITERATIONS_INFERENCER = 100;
	private final static int THINNING_INFERENCER = 1;
	private final static int BURNIN_INFERENCER = 5;
	
	public void map(Properties p, String candSetFilePath, String runfileOutPath, int expandMode, int parallel){
		try {
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(candSetFilePath);
			/*
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getGTMapQrels(
					p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
			*/
			
			HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
					p.getProperty("data-dir")+"/"+p.getProperty("outline"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(runfileOutPath)));
			//ArrayList<String> results = new ArrayList<String>();
			IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(p.getProperty("index-dir")).toPath()))));
			Analyzer analyzer = new StandardAnalyzer();
			//HashMap<String, HashMap<String, Double>> rankResult = new HashMap<String, HashMap<String, Double>>();
			boolean isParallel = false;
			if(parallel>0)
				isParallel = true;
			StreamSupport.stream(pageSecMap.keySet().spliterator(), isParallel).forEach(page -> { 
				try {
					HashMap<String, HashMap<String, Double>> rankResult = new HashMap<String, HashMap<String, Double>>();
					QueryParser qp = new QueryParser("paraid", analyzer);
					ArrayList<String> paraIDsInPage = pageParaMap.get(page);
					ArrayList<String> secIDsInPage = pageSecMap.get(page);
					HashMap<String, String> paraIDTextMap = new HashMap<String, String>();
					for(String paraID:paraIDsInPage) {
						String paraText = is.doc(is.search(qp.parse(paraID), 1).scoreDocs[0].doc).get("parabody");
						paraIDTextMap.put(paraID, paraText);
					}
					InstanceList paraIList = this.convertParasToIList(paraIDTextMap);
					InstanceList secIList = null;
					if(expandMode==0)
						secIList = this.convertSecIDsToIList(secIDsInPage);
					else if(expandMode==1)
						secIList = this.convertSecIDsToExpandedIList(secIDsInPage);
					ParallelTopicModel model = new ParallelTopicModel(secIDsInPage.size(), ALPHA_SUM, BETA);
					model.addInstances(paraIList);
					//model.addInstances(secIList);
					model.setNumThreads(NUM_THREADS_TOPIC_MODEL);
					model.setNumIterations(ITERATIONS);
					model.estimate();
					rankResult = this.rankUsingLDA(paraIList, secIList, model);
					for(String sec:rankResult.keySet()) {
						for(String para:rankResult.get(sec).keySet()) {
							//System.out.println(q+" "+para+" "+rankResult.get(q).get(para));
							bw.write(sec+" Q0 "+para+" 0 "+rankResult.get(sec).get(para)+" TOPIC-MODEL\n");
						}
					}
					System.out.println(page+" done");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			/*
			for(String s:results)
				bw.write(s);
			
			for(String sec:rankResult.keySet()) {
				for(String para:rankResult.get(sec).keySet()) {
					//System.out.println(q+" "+para+" "+rankResult.get(q).get(para));
					bw.write(sec+" Q0 "+para+" 0 "+rankResult.get(sec).get(para)+" TOPIC-MODEL\n");
				}
			}
			*/
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private HashMap<String, HashMap<String, Double>> rankUsingLDA(InstanceList paraIList, InstanceList secIList, ParallelTopicModel model) throws Exception {
		HashMap<String, HashMap<String, Double>> runMap = new HashMap<String, HashMap<String, Double>>();
		TopicInferencer inf = model.getInferencer();
		double[][] queryTopicProbMatrix = new double[secIList.size()][model.getNumTopics()];
		double[][] paraTopicProbMatrix = new double[paraIList.size()][model.getNumTopics()];
		for(int i=0; i<secIList.size(); i++){
			Instance queryIns = secIList.get(i);
			queryTopicProbMatrix[i] = inf.getSampledDistribution(queryIns, ITERATIONS_INFERENCER, THINNING_INFERENCER, BURNIN_INFERENCER);
		}
		if(paraIList.size()!=model.getData().size())
			throw new Exception("paralist size and lda topic assignment size dont match!");
		for(int j=0; j<paraIList.size(); j++){
			// Following if block ensures that we are picking the correct topic dist for current para instance
			if(paraIList.get(j).getName()!=model.getData().get(j).instance.getName())
				throw new Exception("paraIList indices are not following the same order as lda instances");
			paraTopicProbMatrix[j] = model.getTopicProbabilities(j);
		}
		for(int i=0; i<secIList.size(); i++) {
			for(int j=0; j<paraIList.size(); j++) {
				if(runMap.containsKey(secIList.get(i).getName().toString()))
					runMap.get(secIList.get(i).getName().toString()).put(paraIList.get(j).getName().toString(), 1/this.getKLdiv(queryTopicProbMatrix[i], paraTopicProbMatrix[j]));
				else {
					HashMap<String, Double> newRanks = new HashMap<String, Double>();
					newRanks.put(paraIList.get(j).getName().toString(), 1/this.getKLdiv(queryTopicProbMatrix[i], paraTopicProbMatrix[j]));
					runMap.put(secIList.get(i).getName().toString(), newRanks);
				}
			}
		}
		return runMap;
	}
	
	private double getKLdiv(double[] p, double[] q){
		double result = 0;
		for(int i=0; i<p.length; i++){
			if(q[i]<0.0000001 || p[i]<0.0000001){
				continue;
			}
			result+=p[i]*Math.log(p[i]/q[i]);
		}
		return result;
	}
	
	private InstanceList convertParasToIList(HashMap<String, String> paraTexts) {
		InstanceList iListPara = new InstanceList(TopicModelMapper.buildPipeForLDA());
		for(String paraID:paraTexts.keySet()) {
			Instance paraIns = new Instance(paraTexts.get(paraID), null, paraID, paraTexts.get(paraID));
			iListPara.addThruPipe(paraIns);
		}
		return iListPara;
	}
	/*
	private InstanceList convertSecIDsToIList(ArrayList<String> secIDs) {
		InstanceList iListSec = new InstanceList(TopicModelMapper.buildPipeForLDA());
		for(String secID:secIDs) {
			Instance secIns = null;
			String secIDText = secID.toLowerCase().replaceAll("%20", " ");
			if(secIDText.contains("/")) {
				String[] secTokens = secIDText.split("/");
				secIns = new Instance(secTokens[0].split(":")[1]+" "+secTokens[secTokens.length-1], null, secID, secID);
			}
			else
				secIns = new Instance(secIDText.split(":")[1], null, secID, secID);
			iListSec.addThruPipe(secIns);
		}
		return iListSec;
	}
	*/
	private InstanceList convertSecIDsToIList(ArrayList<String> secIDs) {
		InstanceList iListSec = new InstanceList(TopicModelMapper.buildPipeForLDA());
		for(String secID:secIDs) {
			Instance secIns = null;
			String secIDText = secID.toLowerCase().replaceAll("%20", " ");
			if(secIDText.contains("/")) {
				String[] secTokens = secIDText.split("/");
				secIns = new Instance(secTokens[0].split(":")[1]+" "+secTokens[secTokens.length-1], null, secID, secID);
			}
			else
				secIns = new Instance(secIDText.split(":")[1], null, secID, secID);
			iListSec.addThruPipe(secIns);
		}
		return iListSec;
}
	
	private InstanceList convertSecIDsToExpandedIList(ArrayList<String> secIDs) throws FileNotFoundException, IOException {
		InstanceList iListSec = new InstanceList(TopicModelMapper.buildPipeForLDA());
		SynonymMap syn = new SynonymMap(new FileInputStream("prolog/wn_s.pl"));
		for(String secID:secIDs) {
			Instance secIns = null;
			String secIDText = secID.toLowerCase().replaceAll("%20", " ");
			String[] secTokens = secIDText.replaceAll("/", " ").split(" ");
			secTokens[0] = secTokens[0].split(":")[1];
			secIDText = secTokens[0];
			for(String token:secTokens) {
				secIDText+=" "+token;
				String[] synonyms = syn.getSynonyms(token);
				/*
				System.out.print("\nToken: "+token+" Synonyms: ");
				for(String s:synonyms) {
					System.out.print(s+" ");
				}
				System.out.println();
				System.out.print("Token: "+token+" Expanded: ");
				*/
				int count = 0;
				for(String s:synonyms) {
					secIDText+=" "+s;
					//System.out.print(s+" ");
					count++;
				}
				//System.out.println();
			}
			System.out.println("\nSECID-Text: "+secIDText);
			secIns = new Instance(secIDText, null, secID, secID);
			iListSec.addThruPipe(secIns);
		}
		return iListSec;
	}
	
	public static Pipe buildPipeForLDA() {
		ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());
        
        return (new SerialPipes(pipeList));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			TopicModelMapper tmm = new TopicModelMapper();
			tmm.map(p, "/home/sumanta/Documents/Mongoose-data/Mongoose-results/comb-top200-laura-cand-train-page-run", "/home/sumanta/Documents/Mongoose-data/Mongoose-results/topic-model-expanded-sec-train-run", 1, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
