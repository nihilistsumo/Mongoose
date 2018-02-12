package pageRank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class PageRank 
{
	private final double alpha = 0.3;
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
		
		while(i<=200)
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
		System.out.println("converged after"+" "+i+" "+"iterations");
		for(Node node : nodeSet)
			System.out.println(node.getNodeId()+" "+node.getCurrentScore());
		
	}
	public static void main(String args[])throws IOException
	{
		String file = args[0];
		PageRank p = new PageRank(file);
		p.calculate();
	}
}
