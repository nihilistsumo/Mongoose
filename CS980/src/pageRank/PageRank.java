package pageRank;

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


public class PageRank 
{
	private final double alpha = 0.15;
	Graph g;
	Set<Node> nodeSet;
	
	public PageRank(String filePath)
	{
		g = new Graph(filePath);
		System.out.println(g);
		nodeSet = g.getAllNodes();
	}
	private void updateScore()
	{
		for(Node node : nodeSet)
			node.setCurrentScore(node.getNewScore());
	}
	private boolean isConverged()
	{
		for(Node n : nodeSet)
			if(Math.abs(n.getCurrentScore()-n.getNewScore())>0.000000001)
				return false;
		return true;
	}
	public void calculate()throws IOException
	{
		Set<Node> nodeSet = g.getAllNodes();
		double numOfNodes = g.getNumberOfNodes();
		double numOfEdges = g.getNumberOfEdges();
		double initialRank = 1/numOfNodes ;
		System.out.println("Number of nodes ="+numOfNodes);
		System.out.println("Number of edges ="+numOfEdges);
		System.out.println("Initial rank of each node ="+initialRank);
		double nodeScore;
		int outLinks = 0;
		int i = 1;
	
		
		/*Initialize*/
		for (Node node : nodeSet)
			node.setNewScore(initialRank);
		
		while(!isConverged())
		{
			updateScore();
			for(Node node1 : nodeSet)
			{
				nodeScore = 0.0;
				for(Node node2 : nodeSet)
				{
					ArrayList<Node> neighbours = g.getNeighbours(node2);
					outLinks = neighbours.size();
					if(outLinks != 0)
					{
						for(Node n : neighbours)
						{
							if(n.getNodeId().equals(node1.getNodeId()))
							{
								nodeScore = nodeScore + node2.getCurrentScore()/outLinks;
								break;
							}
						}
					}
				}
				nodeScore =  (alpha/numOfNodes)  + ( (1-alpha) * nodeScore );
				node1.setNewScore(nodeScore);
			}
			i++;
		}
		System.out.println("Converged after"+" "+i+" "+"iterations");
		sortScores();
	}
	public static void main(String args[])throws IOException
	{
		String file = args[0];
		PageRank p = new PageRank(file);
		p.calculate();
	}
	private void sortScores()
	{
		Map<String,Double> scores = new LinkedHashMap<String,Double>();
		Map<String, Double> sortedMap = new LinkedHashMap<String,Double>();
		int j=1; double sum=0.0d;
		
		for(Node node : nodeSet)
			sum += node.getCurrentScore();

		for(Node node : nodeSet)
			scores.put(node.getNodeId(), (node.getCurrentScore()/sum));
			
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>((Collection<? extends Entry<String, Double>>) scores.entrySet());
	    Collections.sort(entries,new CustomizedHashMap());
	    
	    for (Map.Entry<String, Double> entry : entries) 
	    	sortedMap.put(entry.getKey(), entry.getValue());
	    
	    System.out.println("*******************Page Ranks********************");
	    System.out.println("Rank\tPageID\t\tScore");
	      
	    for(String s: sortedMap.keySet())
	    {
	        System.out.println(j+"\t"+s+"\t\t"+sortedMap.get(s));
	        j++;
	        if(j>10 || j>g.getNumberOfNodes())
	        	break;
	    }
	            	
	}
}
class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
{

	@Override
	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
	{
		return -o1.getValue().compareTo(o2.getValue());
	}

}
