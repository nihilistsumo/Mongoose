package carHypertextGraph;

/**
 * 
 * @author Shubham Chatterjee
 * Class representing an edge of a graph. Each edge has two nodes, a source and destination, which are paragraphs.
 * Each node will have a number and id representing it. 
 */
public class Edge 
{
	private int sourceNum;
	private int destinationNum;
	private String sourceId;
	private String destinationId;
	
	public Edge(int sourceNum, String sourceId, int destinationNum, String destinationId)
	{
		this.sourceNum = sourceNum;
		this.sourceId = sourceId;
		this.destinationNum = destinationNum;
		this.destinationId = destinationId;
	}
	
	public int getSourceNumber()
	{
		return this.sourceNum;
	}
	public String getSourceId()
	{
		return this.sourceId;
	}
	public int getDestinationNumber()
	{
		return this.destinationNum;
	}
	public String getDestinationId()
	{
		return this.destinationId;
	}
}
