package edu.unh.cs.treccar.proj.qe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;


public class Query 
{
	private static String INDEX_DIR ;
	private static String OUTPUT_DIR ;
	private static String CBOR_OUTLINE_FILE ;
	private static String OUT_FILE ;
	private static String STOP_FILE;
	private static String WORD_2_VEC_FILE;
	private static String QE_METHOD;
	private static String CANDIDATE_SET_METHOD;
	private static int TOP_SEARCH;
	private static int TOP_FEEDBACK;
	private static int TOP_TERMS;
	private static ArrayList<Data.Page> pagelist;
	private static ArrayList<String> paraID;
	private static HashMap<String, ArrayList<Double>> word2vec;
	private static IndexSearcher is;
	private static Analyzer analyzer;
	private static Similarity similarity;
	private static List<String> tokens;
	private static ArrayList<String> stopWords;
	
	public final static class Search
	{
		public Search(String dir, String out_dir, String outline_file, String out_file, String stopFilePath, String word2vecFile ,int topSearch, int topFeedback, int topTerms, String qe_method, String cs_method, Analyzer a, Similarity s)
		{
			Query.INDEX_DIR = dir;
			Query.OUTPUT_DIR = out_dir;
			Query.CBOR_OUTLINE_FILE = outline_file;
			Query.OUT_FILE = out_file;
			Query.STOP_FILE = stopFilePath;
			Query.WORD_2_VEC_FILE = word2vecFile;
			Query.QE_METHOD = qe_method;
			Query.CANDIDATE_SET_METHOD = cs_method;
			Query.TOP_SEARCH = topSearch;
			Query.TOP_FEEDBACK = topFeedback;
			Query.TOP_TERMS = topTerms;
			Query.pagelist = getPageListFromPath(CBOR_OUTLINE_FILE);
			Query.paraID = new ArrayList<String>();
			Query.analyzer = a;
			Query.similarity = s;
			Query.is = new Index.Setup(INDEX_DIR, "parabody",a ,s).getSearcher();
			Query.tokens = new ArrayList<>(128);
			Query.stopWords = new ArrayList<String>();
			Query.word2vec = new HashMap<String, ArrayList<Double>>();
			if(QE_METHOD.equalsIgnoreCase("KNN"))
				System.out.println("Using KNN for QE");
			else
				System.out.println("Using RM3 for QE");
			
			try 
			{
				getStopWords();
				if(QE_METHOD.equalsIgnoreCase("KNN"))
				{
					getWordToVec();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		private void getWordToVec() 
		{
			System.out.println("Getting word vectors");
			BufferedReader reader = null;
			String line;
			int i;
			try 
			{
				reader = new BufferedReader(new FileReader(new File(WORD_2_VEC_FILE)));
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			
			try 
			{
				while((line = reader.readLine()) != null)
				{
					String[] words = line.split(" ");
					ArrayList<Double> vector = new ArrayList<Double>();
					for(i = 1; i < words.length; i++)
					{
						vector.add(Double.parseDouble(words[i]));
					}
					word2vec.put(words[0], vector);
				}
				reader.close();
			} 
			catch (NumberFormatException | IOException e) 
			{
				e.printStackTrace();
			}
			System.out.println("Done");
		}
		public void getStopWords() throws IOException
		{
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(new File(STOP_FILE)));
			while((line = reader.readLine()) != null )
				stopWords.add(line);
			reader.close();
		}
		public void rankParas(BooleanQuery qString, String qID, int n) throws IOException, ParseException 
		{
			TopDocs tds = Index.Search.searchIndex(qString,n);
			String originalQuery = qString.toString("parabody");
			String topTerms = "";
			//System.out.println("original query:"+originalQuery);
			if(QE_METHOD.equalsIgnoreCase("RM3"))
			{
				//System.out.println("Using RM3 for QE");
				Query.RM3Expand ob = new Query.RM3Expand(tds, TOP_TERMS);
				topTerms = ob.expand();
			}
			else if(QE_METHOD.equalsIgnoreCase("KNN"))
			{
				//System.out.println("Using KNN for QE");
				Query.KNNExpand ob = new Query.KNNExpand(tds,TOP_TERMS);
				topTerms = ob.expand(originalQuery);
			}
			//String topTerms = ob.expand(originalQuery);
			BooleanQuery query = toQuery(originalQuery,topTerms);
			TopDocs tds1 = Index.Search.searchIndex(query,n);
			ScoreDoc[] retDocs1 = tds1.scoreDocs;
			createRunFile(qID,tds1,retDocs1);
			//System.out.println("Done:"+query.toString("parabody"));
		}
		private BooleanQuery toQuery(String originalQuery, String topTerms) throws IOException
		{
			String newQuery = "";
			String[] terms = topTerms.split(" ");
			for(String s : terms)
				if(!originalQuery.contains(s))
					newQuery += " " + s;
			
			ArrayList<TermQuery> originalTerms = getQueryTerms(originalQuery);
			ArrayList<TermQuery> newTerms = getQueryTerms(newQuery);
			BooleanQuery.Builder query = new BooleanQuery.Builder();
			for(TermQuery tq : originalTerms)
				query.add(tq,BooleanClause.Occur.MUST);
			
			for(TermQuery tq : newTerms)
				query.add(tq,BooleanClause.Occur.SHOULD);
			
			return query.build();
		}
		private ArrayList<TermQuery> getQueryTerms(String queryStr) throws IOException
		{
			ArrayList<TermQuery> terms = new ArrayList<TermQuery>();
			TokenStream tokenStream = analyzer.tokenStream("parabody", new StringReader(queryStr));
	        tokenStream.reset();
	        tokens.clear();
	        while (tokenStream.incrementToken()) 
	        {
	            final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
	            tokens.add(token);
	        }
	        tokenStream.end();
	        tokenStream.close();
	        
	        for (String token : tokens) 
	        {
	            terms.add(new TermQuery(new Term("parabody", token)));
	        }
	        return terms;
		}
		public BooleanQuery toQuery(String queryStr) throws IOException 
		{

	        TokenStream tokenStream = analyzer.tokenStream("parabody", new StringReader(queryStr));
	        tokenStream.reset();
	        tokens.clear();
	        while (tokenStream.incrementToken()) 
	        {
	            final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
	            tokens.add(token);
	        }
	        tokenStream.end();
	        tokenStream.close();
	        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
	        for (String token : tokens) 
	        {
	            booleanQuery.add(new TermQuery(new Term("parabody", token)), BooleanClause.Occur.SHOULD);
	        }
	        return booleanQuery.build();
	    }
		private void createRunFile(String queryID, TopDocs tds, ScoreDoc[] retDocs)throws IOException
		{
			ArrayList<String> runStrings = new ArrayList<String>();
			Document d;
			String runFileString;
			String outFilePath = OUTPUT_DIR+"/"+OUT_FILE;
			
			for (int i = 0; i < retDocs.length; i++) 
			{
				d = is.doc(retDocs[i].doc);
				String pID = d.getField("paraid").stringValue();
				
				/*runFile string format: $queryId Q0 $paragraphId $rank $score $name*/
				
				runFileString = queryID+" Q0 "+pID+" "+i+" "+tds.scoreDocs[i].score+" "+CANDIDATE_SET_METHOD+"+"+QE_METHOD;
				if(!paraID.contains(pID))
				{
					paraID.add(pID);
					runStrings.add(runFileString);
				}
			}
			
			FileWriter fw = new FileWriter(outFilePath, true);
			for(String runString:runStrings)
				fw.write(runString+"\n");
			fw.close();
		}	
		private ArrayList<Data.Page> getPageListFromPath(String path)
		{
			ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
			try 
			{
				FileInputStream fis = new FileInputStream(new File(path));
				for(Data.Page page: DeserializeData.iterableAnnotations(fis))
					pageList.add(page);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			return pageList;
		}
		private String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) 
		{
	        StringBuilder queryStr = new StringBuilder();
	        queryStr.append(page.getPageName());
	        for (Data.Section section: sectionPath)
	        {
	        	if(!section.getHeading().contains("/"))
	        		queryStr.append(" ").append(section.getHeading());
	        }
	        return queryStr.toString();
	    }
		public void search(String mode)
		{
			switch(mode)
			{
			case "page-title" : searchPageTitles();
			break;
			
			case "section-path" : searchSectionHeadings();
			break;
			
			case "top-level-sections" : searchTopLevelSections();
			break;
			
			default: System.out.println("*******************************Wrong mode! See usage!****************************");
			System.exit(0);
			}
		}
		private void searchTopLevelSections()
		{
			String qString,qID;
			BooleanQuery query;
			try
			{
				for(Data.Page page:pagelist)
				{	
					//System.out.println("Page:"+page.getPageName());
					qString = page.getPageName();
					query = toQuery(qString);
					qID = page.getPageId();
					rankParas(query,qID, TOP_SEARCH);
					List<Data.Section> childSections = page.getChildSections();
					for(Data.Section child : childSections)
					{
		                qString = child.getHeading();
		                query = toQuery(qString);
		                qID = page.getPageId()+"/"+child.getHeadingId();
		                rankParas(query,qID, TOP_SEARCH);
					}
					//System.out.println("\n" + StringUtils.repeat("=", 128) + "\n");
					System.out.println("Done page:"+page.getPageName());
				}
			} 
			catch ( IOException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		public void searchPageTitles()
		{
			try
			{
				for(Data.Page page:pagelist)
				{
					String qString = buildSectionQueryStr(page, Collections.<Data.Section>emptyList());
					BooleanQuery query = toQuery(qString);
					String qID = page.getPageId();
					rankParas(query,qID, TOP_SEARCH);
					//System.out.println("\n" + StringUtils.repeat("=", 128) + "\n");
					System.out.println("Done page:"+page.getPageName());
				}
			}
			catch (IOException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		public void searchSectionHeadings()
		{
			try
			{
				for(Data.Page page:pagelist)
				{	
					for (List<Data.Section> sectionPath : page.flatSectionPaths()) 
					{
		                String qString = buildSectionQueryStr(page, sectionPath);
		                BooleanQuery query = toQuery(qString);
		                String qID = Data.sectionPathId(page.getPageId(), sectionPath);
		                rankParas(query,qID, TOP_SEARCH);
						System.out.println("\n" + StringUtils.repeat("=", 128) + "\n");
		        
		            }
				}
			} 
			catch ( IOException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
	}
	public static class Expand
	{
		protected TopDocs topDocs;
		protected ScoreDoc[] scores;
		protected ArrayList<String> words;
		protected ArrayList<String> processedWords;
		
		public Expand(TopDocs tds) throws IOException
		{
			topDocs = tds;
			scores = tds.scoreDocs;
			words = getAllWords();
			Preprocess p = new Preprocess(words);
			processedWords = p.getProcessedText();
		}
		private ArrayList<String> getAllWords() throws IOException
		{
			ArrayList<String> words = new ArrayList<String>();
			for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
			{
				Document d = is.doc(scores[i].doc);
				String text = d.getField("parabody").stringValue();
				String[] w = text.split(" ");
				for(String s : w)
				words.add(s);
			}
			return words;
		}
		protected String getTopWords(Map<String, Float> map, int k) throws IOException
		{
			Map<String, Float>  sortedMap = new LinkedHashMap<String,Float>();
			String topTerms = "";
			int count = 0;
			List<Map.Entry<String, Float>> entries = new ArrayList<Map.Entry<String, Float>>((Collection<? extends Entry<String, Float>>) map.entrySet());
		    Collections.sort(entries,new CustomizedHashMap());
		    
		    for (Map.Entry<String, Float> entry : entries) 
		    	sortedMap.put(entry.getKey(), entry.getValue());
		    
		    for(String s : sortedMap.keySet())
		    {
		    	topTerms += " "+s;
		    	count++;
		    	if(count == k)
		    		break;
		    }
		    return topTerms;
			
		}
		private class CustomizedHashMap implements Comparator<Map.Entry<String, Float>> 
		{

			@Override
			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) 
			{
				return -o1.getValue().compareTo(o2.getValue());
			}

		}
	}
	public final static class RM3Expand extends Expand
	{
		private int TOP;
		public RM3Expand(TopDocs tds, int top) throws IOException
		{
			super(tds);
			TOP = top;
		}
		private float getSumOfScores()
		{
			float max = getMaxScore();
			float s = 0;
			for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
				s += Math.exp(topDocs.scoreDocs[i].score - max);
			s = (float) (max + Math.log(s));
			return s;
		}
		private float getMaxScore()
		{
			float max = topDocs.scoreDocs[0].score;
			for(int i = 1; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
			{
				if( topDocs.scoreDocs[i].score > max )
					max = topDocs.scoreDocs[i].score;
			}
			return max;
		}
		private float prob_D_given_Q(int doc)
		{
			float sum = getSumOfScores();;
			return ( topDocs.scoreDocs[doc].score / sum );
		}
		private float prob_W_given_D(String word, String text)
		{
			int numOfWords = findNumOfWords(text);
			int freqOfWord = findFreqOfWord(word, text);
			
			return (float)freqOfWord / numOfWords;
		}
		private int findNumOfWords(String text)
		{
			return text.split(" ").length;
		}
		private int findFreqOfWord(String word, String text)
		{
			String[] words = text.split(" ");
			int count = 0;
			for(String s : words)
				if(s.equalsIgnoreCase(word))
					count++;
			return count;
		}
		public String expand() throws IOException
		{
			Document d;
			String text, topTerms;
			float p_W_given_Q;
			float p_W_given_D;
			float p_D_given_Q;
			
			Map<String, Float> wordToProbMap = new LinkedHashMap<String, Float>();

			for(String word: processedWords)
			{
				p_W_given_Q = 0;
				for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
				{
					d = is.doc(scores[i].doc);
					text = d.getField("parabody").stringValue();
					p_D_given_Q = prob_D_given_Q(i);	
					p_W_given_D = prob_W_given_D(word, text);
					p_W_given_Q += p_W_given_D * p_D_given_Q;
				}
				wordToProbMap.put(word, p_W_given_Q);
			}
			topTerms = getTopWords(wordToProbMap, TOP);
			return topTerms;
		}
	}
	public final static class KNNExpand extends Expand
	{
		private int K;
		
		public KNNExpand(TopDocs tds,int k) throws IOException
		{
			super(tds);
			K = k;
		}
		public String expand(String query) throws IOException
		{
			String[] qTerms = query.split(" ");
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = getQueryVector(qTerms);
			LinkedHashMap<String,ArrayList<Double>> candidateMap = getCandidateSet(qTerms);
			HashMap<String, Float> scores = new HashMap<String, Float>();
			float sum = 0.0f;
			String topTerms;
			
			for(String t : candidateMap.keySet())
			{
				ArrayList<Double> vector = candidateMap.get(t);
				for(String q : queryVectorMap.keySet())
				{
					if(vector != null && queryVectorMap.get(q) != null)
						sum += findSimilarity(vector, queryVectorMap.get(q));
				}
				scores.put(t, (sum / qTerms.length));
			}
			topTerms = getTopWords(scores, K);
			return topTerms;
		}
		private double findSimilarity(ArrayList<Double> vec1, ArrayList<Double> vec2)
		{
			if(vec1.size() != vec2.size())
			{
				System.out.println("Vectors of different length! Cannot find similarity!");
				System.exit(0);
			}
			int i, size = vec1.size();
			double score, numerator = 0.0d, denominator = 0.0d;
			for(i = 0; i < size; i++)
			{
				numerator += vec1.get(i) * vec2.get(i);
			}
			denominator = getNorm(vec1) * getNorm(vec2);
			score = numerator / denominator;
			return score;
		}
		private double getNorm(ArrayList<Double> vector)
		{
			double norm = 0.0d;
			for(int i = 0; i < vector.size(); i++)
			{
				norm += vector.get(i) * vector.get(i);
			}
			norm = Math.sqrt(norm);
			return norm;
		}
		private LinkedHashMap<String,ArrayList<Double>> getQueryVector(String[] terms)
		{
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = new LinkedHashMap<String,ArrayList<Double>>();
			for(String s : terms)
				queryVectorMap.put(s, word2vec.get(s));
			return queryVectorMap;
		}
		private LinkedHashMap<String,ArrayList<Double>> getCandidateSet(String[] terms)
		{
			LinkedHashMap<String,ArrayList<Double>> candidateMap = new LinkedHashMap<String,ArrayList<Double>>();
			for(String s : terms)
			{
				LinkedHashMap<String,ArrayList<Double>> neighbours = getNearestNeighbours(s);
				if(neighbours != null)
				{
					for( String s1 : neighbours.keySet() )
						candidateMap.put(s1, neighbours.get(s1));
				}
			}
			if(!candidateMap.isEmpty())
				return candidateMap;
			return null;
		}
		private LinkedHashMap<String,ArrayList<Double>> getNearestNeighbours(String s)
		{
			HashMap<String, Double> distanceMap = new HashMap<String, Double>();
			HashMap<String, Double> sortedDistanceMap = new HashMap<String, Double>();
			LinkedHashMap<String, ArrayList<Double>> neighbours = new LinkedHashMap<String, ArrayList<Double>>();
			ArrayList<Double> queryVector = word2vec.get(s);
			if(queryVector != null)
			{
				int count = 0;
				
				for(String str : processedWords)
				{
					if(!str.equals(s) && word2vec.containsKey(str))
					{
						ArrayList<Double> wordVector = word2vec.get(str);
						double distance = getDistance(queryVector, wordVector);
						distanceMap.put(str, distance);
					}
				}
				List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>((Collection<? extends Entry<String, Double>>) distanceMap.entrySet());
			    Collections.sort(entries,new CustomizedHashMap());
			    
			    for (Map.Entry<String, Double> entry : entries) 
			    	sortedDistanceMap.put(entry.getKey(), entry.getValue());
			    
			    for(String str : sortedDistanceMap.keySet())
			    {
			    	neighbours.put(str,word2vec.get(str));
			    	count++;
			    	if(count == K)
			    		break;
			    }
			    return neighbours;
			}
			else
			return null;
		}
		private double getDistance(ArrayList<Double> vec1, ArrayList<Double> vec2)
		{
			if(vec1.size() != vec2.size())
			{
				System.out.println("Vectors of different length! Cannot find distance!");
				System.exit(0);
			}
			int i , size = vec1.size();
			double sum = 0.0d;
			for(i =0; i < size; i++)
			{
				sum += Math.pow((vec1.get(i) - vec2.get(i)), 2);
			}
			return Math.sqrt(sum);
		}
		private class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
		{

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
			{
				return o1.getValue().compareTo(o2.getValue());
			}

		}
	}
	public final static class Preprocess
	{
		private ArrayList<String> processedWords;
		private ArrayList<String> words;
		
		public Preprocess(ArrayList<String> text)
		{
			words = lowerCaseWords(text);
			processedWords = removeStopWords(words);
		}
		private ArrayList<String> lowerCaseWords(ArrayList<String> words)
		{
			ArrayList<String> newWords = new ArrayList<String>();
			for(String s : words)
				newWords.add(s.replaceAll("[^a-zA-Z ]", "").toLowerCase());
			return newWords;
		}
		private ArrayList<String> removeStopWords(ArrayList<String> words)
		{
			ArrayList<String> newWords = new ArrayList<String>();
			for( String s : words )
				if(!stopWords.contains(s))
					newWords.add(s);
			return newWords;
		}
		public ArrayList<String> getProcessedText()
		{
			return processedWords;
		}
	}
	public static void main(String[]  args)
	{
		Similarity sim = null;

		String dir = args[0];
		String out_dir = args[1];
		String outline_file = args[2];
		String out_file = args[3];
		String stopFilePath = args[4];
		String word2vecFile = args[5];
		int topSearch = Integer.parseInt(args[6]);
		int topFeedback = Integer.parseInt(args[7]);
		int topTerms = Integer.parseInt(args[8]);
		String qe_method = args[9];
		String cs_method = args[10];

		
		if(cs_method.equals("BM25"))
		{
			System.out.println("Using BM25 for candidate set generation");
			sim = new BM25Similarity();
		}
		else if(cs_method.equals("LM-DS"))
		{
			System.out.println("Using LM-DS for candidate set generation");
			sim = new LMDirichletSimilarity();
		}
		else if(cs_method.equals("LM-JM"))
		{
			System.out.println("Using LM-JM for candidate set generation");
			float lambda = Float.parseFloat(args[11]);
			sim = new LMJelinekMercerSimilarity(lambda);
		}
		else
		{
			System.out.println("Wrong similarity metric");
			System.exit(0);
		}
		Query.Search ob = new Query.Search(dir, out_dir, outline_file, out_file, stopFilePath, word2vecFile, topSearch, topFeedback, topTerms, qe_method, cs_method, new StandardAnalyzer(), sim);
		ob.searchTopLevelSections();
		//ob.searchPageTitles();
	}
}

