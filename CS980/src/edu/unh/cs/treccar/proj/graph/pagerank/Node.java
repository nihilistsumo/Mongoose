package edu.unh.cs.treccar.proj.graph.pagerank;


public class Node 
{
	private String nodeID;
	private double newScore;
	private double currentScore;
	
	public Node(String nodeID)
	{
		this.nodeID = nodeID;
	}
	public String getNodeId()
	{
		return this.nodeID;
	}
	public double getNewScore()
	{
		return this.newScore;
	}
	public void setNewScore(double score)
	{
		this.newScore = score;
	}
	public double getCurrentScore()
	{
		return this.currentScore;
	}
	public void setCurrentScore(double score)
	{
		this.currentScore = score;
	}

}
