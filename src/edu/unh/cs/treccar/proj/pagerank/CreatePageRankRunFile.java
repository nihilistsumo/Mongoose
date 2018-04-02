package edu.unh.cs.treccar.proj.pagerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * Class to read the PageRank results and the run file results and get a combined run file
 * Uses score = ( 0.8 * score1 ) + ( 0.2 * score2 )
 * @author Shubham Chatterjee
 *
 */

public class CreatePageRankRunFile 
{
	/**
	 * PageRank file
	 */
	private String PAGE_RANK_FILE;
	/**
	 * Run file from a method
	 */
	private String RUN_FILE;
	/**
	 * PageRank run file
	 */
	private String PAGE_RANK_RUN_FILE;
	private LinkedHashMap<String,HashMap<String, Double>> rankings;
	private LinkedHashMap<String,Double> pageRank;
	
	public CreatePageRankRunFile(String s1, String s2, String s3) throws IOException
	{
		PAGE_RANK_FILE = s1;
		RUN_FILE = s2;
		PAGE_RANK_RUN_FILE = s3;
		rankings = new LinkedHashMap<String,HashMap<String, Double>>();
		pageRank = new LinkedHashMap<String,Double>();
		getRankings();
		getPageRankScore();
		createRunFile();
	}
	/**
	 * Read the rankings from the run file
	 */
	private void getRankings()
	{
		System.out.println("Getting rankings from run file...");
		BufferedReader reader = null;
		String line = "",s1 = "",s2 = "",s3 = "";
		double score;
		HashMap<String, Double> paraScore = new HashMap<String, Double>();
		try 
		{
			reader = new BufferedReader(new FileReader(new File(RUN_FILE)));

            while((line = reader.readLine()) != null) 
            {
            	s3 = s1;
            	s1 = line.split(" ")[0];
                s2 = line.split(" ")[2];
                score = Double.parseDouble(line.split(" ")[4]);
            	if(!s1.equals(s3))
            	{
            		if(!s3.equals(""))
            		{
            			rankings.put(s3, paraScore);
            		}
            		paraScore = new HashMap<String, Double>(); 
            	}
                paraScore.put(s2, score);
            }
            reader.close();
            System.out.println("Done");
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
	}
	/**
	 * Read the page rank scores
	 * @throws IOException
	 */
	private void getPageRankScore() throws IOException
	{
		System.out.println("Getting pagerank scores...");
		BufferedReader reader = new BufferedReader(new FileReader(new File(PAGE_RANK_FILE)));
		String line, paraId;
		double score;
		while((line = reader.readLine()) != null) 
        {
			line = line.trim();
			paraId = line.split(" ")[1];
			score = Double.parseDouble(line.split(" ")[2]);
			pageRank.put(paraId, score);
        }
		reader.close();
		System.out.println("Done");
	}
	/**
	 * Create the page rank run file
	 * @throws IOException
	 */
	private void createRunFile() throws IOException
	{
		System.out.println("Creating pagerank run file...");
		HashMap<String, Double> paraScore = new HashMap<String, Double>();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();
		HashMap<String, Double> sortedScoreMap = new HashMap<String, Double>();
		for(String query : rankings.keySet())
		{
			paraScore = rankings.get(query);
			for(String paraId : paraScore.keySet())
			{
				double score1 = paraScore.get(paraId);
				double score2 = pageRank.containsKey(paraId) ? pageRank.get(paraId) : 0.00001;
				double score = (0.8 * score1) + (0.2 * score2);
				scoreMap.put(paraId, score);
			}
			sortedScoreMap = sortScores(scoreMap);
			createRunFile(query,sortedScoreMap);
		}
		System.out.println("Done");
	}
	/**
	 * Helper method to create page rank file
	 * @param queryID
	 * @param scoreMap
	 * @throws IOException
	 */
	private void createRunFile(String queryID, HashMap<String, Double> scoreMap) throws IOException
	{
		FileWriter writer = new FileWriter(PAGE_RANK_RUN_FILE,true);
		String runFileString;
		int rank = 0;
		for(String pID : scoreMap.keySet()) 
		{
			/*runFile string format: $queryId Q0 $paragraphId $rank $score $name*/
			runFileString = queryID+" Q0 "+pID+" "+rank+" "+scoreMap.get(pID)+" "+"LM-JM+KNN+PAGERANK";
			rank++;
			writer.write(runFileString+"\n");
		}
		writer.close();
	}

	private HashMap<String, Double> sortScores(Map<String, Double> map) throws IOException
	{
		HashMap<String, Double>  sortedMap = new LinkedHashMap<String,Double>();
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>((Collection<? extends Entry<String, Double>>) map.entrySet());
	    Collections.sort(entries,new CustomizedHashMap());
	    
	    for (Map.Entry<String, Double> entry : entries) 
	    	sortedMap.put(entry.getKey(), entry.getValue());
	    
	    return sortedMap;
		
	}
	private class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
	{

		@Override
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
		{
			return -o1.getValue().compareTo(o2.getValue());
		}

	}
	public static void main(String[] args) throws IOException
	{
		String page_rank_file = args[0];
		String run_file = args[1];
		String page_rank_run_file = args[2];
		new CreatePageRankRunFile(page_rank_file, run_file, page_rank_run_file);
	}

}
