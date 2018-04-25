package edu.unh.cs.treccar.proj.pagerank;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

 public abstract class PageRankDriver 
{
	protected final double alpha;
	protected Graph g;
	protected Set<Node> nodeSet;
	protected double numOfNodes;
	protected double numOfEdges;
	protected double initialRank ;
	private String outFile;
	
	public PageRankDriver(String filePath,String outFilePath, double a)
	{
		System.out.println("Reading Graph file.....");
		g = new Graph(filePath);
		System.out.println("Done");
		//System.out.println(g);
		outFile = outFilePath;
		nodeSet = g.getAllNodes();
		alpha = a;
		numOfNodes = g.getNumberOfNodes();
		numOfEdges = g.getNumberOfEdges();
		initialRank = 1/numOfNodes ;
	}
	protected void updateScore()
	{
		for(Node node : nodeSet)
			node.setCurrentScore(node.getNewScore());
	}
	protected boolean isConverged()
	{
		for(Node n : nodeSet)
			if(Math.abs(n.getCurrentScore()-n.getNewScore())>0.000000001)
				return false;
		return true;
	}
	protected void sortScores() throws IOException
	{
		Map<String,Double> scores = new LinkedHashMap<String,Double>();
		Map<String, Double> sortedMap = new LinkedHashMap<String,Double>();
		double sum=0.0d;
		
		for(Node node : nodeSet)
			sum += node.getCurrentScore();

		for(Node node : nodeSet)
			scores.put(node.getNodeId(), (node.getCurrentScore()/sum));
			
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>((Collection<? extends Entry<String, Double>>) scores.entrySet());
	    Collections.sort(entries,new CustomizedHashMap());
	    
	    for (Map.Entry<String, Double> entry : entries) 
	    	sortedMap.put(entry.getKey(), entry.getValue());
	    
	    printScores(sortedMap);
	            	
	}
	private void printScores(Map<String,Double> scores) throws IOException
	{
		FileWriter fw = new FileWriter(outFile, true);
		int j = 1;
		System.out.println("*******************Page Ranks********************");
	    System.out.println("Rank\tPageID\t\tScore");
	      
	    for(String s: scores.keySet())
	    {
	    	String fileString = " "+j+" "+s+" "+scores.get(s);
	    	fw.write(fileString+"\n");
	        System.out.println(j+"\t"+s+"\t\t"+scores.get(s));
	        j++;
	    }
	    fw.close();
	}
	public abstract void calculate() throws IOException;
}
class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
{

	@Override
	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
	{
		return -o1.getValue().compareTo(o2.getValue());
	}

}

