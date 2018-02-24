package carHypertextGraph;

public class Node 
{
	private String nodeID;
	private int nodeNumber;
	
	public Node(String nodeID, int nodeNumber)
	{
		this.nodeID = nodeID;
		this.nodeNumber = nodeNumber;
	}
	public String getNodeId()
	{
		return this.nodeID;
	}
	public int getNodeNumber()
	{
		return this.nodeNumber;
	}

}
