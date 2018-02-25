package carHypertextGraph;

import java.util.List;

public class Node 
{
	private String nodeID;
	private int nodeNumber;
	private List<String> entityList;
	
	public Node(String nodeID, int nodeNumber, List<String> entityList)
	{
		this.nodeID = nodeID;
		this.nodeNumber = nodeNumber;
		this.entityList = entityList;
	}
	public String getNodeId()
	{
		return this.nodeID;
	}
	public int getNodeNumber()
	{
		return this.nodeNumber;
	}
	public List<String> getEntityList()
	{
		return this.entityList;
	}

}
