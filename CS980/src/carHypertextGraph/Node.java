package carHypertextGraph;

/**
 * @author Shubham Chatterjee
 * Class representing a Node in a graph.
 * Each node has a node id which is the paragraph id of the paragraph which the node is representing,
 * a node number which is the number of the node in the graph, and an entity list which is a list 
 * of entities which the paragraph points to.
 * 
 */
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
