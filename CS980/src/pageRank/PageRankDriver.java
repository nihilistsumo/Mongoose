package pageRank;

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
	
	public PageRankDriver(String filePath,double a)
	{
		g = new Graph(filePath);
		System.out.println(g);
		nodeSet = g.getAllNodes();
		alpha = a;
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
	protected void sortScores()
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
	    
	    showScores(sortedMap);
	            	
	}
	private void showScores(Map<String,Double> scores)
	{
		int j = 1;
		System.out.println("*******************Page Ranks********************");
	    System.out.println("Rank\tPageID\t\tScore");
	      
	    for(String s: scores.keySet())
	    {
	        System.out.println(j+"\t"+s+"\t\t"+scores.get(s));
	        j++;
	        if(j>10 || j>g.getNumberOfNodes())
	        	break;
	    }
	}
	public abstract void calculate();
}
class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
{

	@Override
	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
	{
		return -o1.getValue().compareTo(o2.getValue());
	}

}

