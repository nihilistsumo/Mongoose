package edu.unh.cs.treccar.proj.graph.pagerank;

import java.util.ArrayList;


public class PageRank extends PageRankDriver
{
	public PageRank(String filePath,double alpha)
	{
		super(filePath,alpha);
	}
	public void calculate()
	{
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
}
