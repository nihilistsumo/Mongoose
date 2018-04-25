package edu.unh.cs.treccar.proj.pagerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class PersonalisedPageRank extends PageRankDriver
{
	ArrayList<String> seedSet;
	public PersonalisedPageRank (String filePath,String outFilePath, String runFile, double alpha)
	{
		super(filePath,outFilePath, alpha);
		seedSet = getSeedSet(runFile);
	}
	private ArrayList<String> getSeedSet(String runFile)
	{
		System.out.println("Getting seed set");
		ArrayList<String> seed = new ArrayList<String>();
		int count = 0;
		BufferedReader reader = null;
		String line;
		try 
		{
			reader = new BufferedReader(new FileReader(new File(runFile)));
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
				String paraId = words[2];
				seed.add(paraId);
				count++;
				if(count == 10)
					break;
			}
			reader.close();
		} 
		catch (NumberFormatException | IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Done");
		return seed;
	}
	public void calculate() throws IOException
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
				if(seedSet.contains(node1.getNodeId()))
					nodeScore =  (alpha/seedSet.size())  + ( (1-alpha) * nodeScore );
				else
					nodeScore =  (1-alpha) * nodeScore;
				node1.setNewScore(nodeScore);
			}
			i++;
		}
		System.out.println("Converged after"+" "+i+" "+"iterations");
		sortScores();
	}
	public static void main(String[]  args) throws IOException
	{
		System.out.println("Personalised PageRank Algorithm on CAR Graph of top paragraphs of each page as nodes");
		String graphFile = args[0];
		String outFile = args[1];
		String runFile = args[2];
		double alpha = Double.parseDouble(args[3]);
		PersonalisedPageRank p = new PersonalisedPageRank(graphFile, outFile, runFile, alpha);
		p.calculate();
		
	}
}

