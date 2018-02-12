package pageRank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class Graph 
{
	private LinkedHashMap<Node,ArrayList<Node>> adjacencyList;
	
	public Graph( String filePath )
	{
		adjacencyList = new LinkedHashMap<Node,ArrayList<Node>>();
		makeAdjacencyList(filePath);
	}
	private void makeAdjacencyList(String filePath)
	{
		BufferedReader br = null;
		String line="";
		try 
		{
            br = new BufferedReader(new FileReader(filePath));

            while((line = br.readLine()) != null) 
            {
                String[] nodes = line.split("\\s+");
                ArrayList<Node> neighbours = new ArrayList<Node>(); 
                Node node = new Node(nodes[0]);
                for(int i =1;i<nodes.length;i++)
                	neighbours.add(new Node(nodes[i]));
                adjacencyList.put(node, neighbours); 
            }   
            br.close();  
        }
        catch(FileNotFoundException e) 
		{
            System.out.println("Unable to open file '" +filePath + "'");                
        }
        catch(IOException e) 
		{
            System.out.println("Error reading file '"+ filePath + "'");                  
        }	
		
	}
	public LinkedHashMap<Node,ArrayList<Node>> getAdjacencyList()
	{
		return this.adjacencyList;
	}
	public int getNumberOfOutlinks(Node node)
	{
		return getNeighbours(node).size();
	}
	public Set<Node> getAllNodes()
	{
		return this.adjacencyList.keySet();
	}
	public ArrayList<Node> getNeighbours(Node node)
	{
		return this.adjacencyList.get(node);
	}
	public int getNumberOfNodes()
	{
		return adjacencyList.size();
	}
	public int getNumberOfEdges()
	{
		Set<Node> nodes = getAllNodes();
		int s = 0;
		for(Node node : nodes)
			s += getNumberOfOutlinks(node);
		return s;
	}
	@Override
	public String toString()
	{
		String str = "";
		for(Node node1 : adjacencyList.keySet())
		{
			str += node1.getNodeId()+":";
			ArrayList<Node> neighbours = adjacencyList.get(node1);
			for(Node node2 : neighbours)
				str += " "+node2.getNodeId();
			str += "\n";
		}
		return str;
	}
		
}
