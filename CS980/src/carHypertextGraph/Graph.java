package carHypertextGraph;

import org.apache.lucene.document.Document;
import org.mapdb.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Graph 
{
	private DB db1,db2;
	private  ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private ArrayList<Term> adjMatrix;
	private HTreeMap<Integer,String> numToIdMap;
	private HTreeMap<String,Integer> outlinks;	
	
	public Graph(ArrayList<Document> documents)
	{
		db1 = DBMaker.fileDB("graph1.db").fileMmapEnable().transactionEnable().make();
		db2 = DBMaker.fileDB("graph2.db").fileMmapEnable().transactionEnable().make();
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		adjMatrix = new ArrayList<Term>();
		numToIdMap = db1.hashMap("num_to_id_map", Serializer.INTEGER, Serializer.STRING).counterEnable().create();
		outlinks = db2.hashMap("outlinks", Serializer.STRING, Serializer.INTEGER).counterEnable().create();
		makeNodeSet(documents);
		makeAdjacencySparseMatrix();
		makeNumToIdMap();
	}
	private  void makeNodeSet(ArrayList<Document> documents)
	{
		System.out.println("Adding a node to node set of graph");
		String pID, entity;
		int nodeNumber = 0;
		String[] entityArray;
		List<String> entityList;
		
		for(Document d : documents)
		{
			pID = d.getField("paraid").stringValue();
			entity = d.getField("paraentity").stringValue();
			entityArray = entity.split(" ");
			entityList = Arrays.asList(entityArray);
			nodes.add(new Node(pID, nodeNumber,entityList));
			nodeNumber++;
		}
	}	
	public ArrayList<Node> getNodeSet()
	{
		return this.nodes;
	}
	public ArrayList<Edge> getEdgeSet()
	{
		return this.edges;
	}
	public ArrayList<Term> getAdjacencySparseMatrix()
	{
		return this.adjMatrix;
	}
	public int getNumberOfNodes()
	{
		return this.nodes.size();
	}
	public int getNumberOfEdges()
	{
		return this.edges.size();
	}
	public int getNumberOfOutlinks(Node node)
	{
		return outlinks.get(node.getNodeId());
	}
	public int getNumberOfOutlinks(String str)
	{
		return outlinks.get(str);
	}
	public String getSourceNodeId(int source)
	{
		String id = "";
		for(Edge e : edges)
		{
			if(e.getSourceNumber() == source)
			{
				id = e.getSourceId();
				break;
			}
		}
		return id;
	}
	public String getDestinationNodeId(int dest)
	{
		String id = "";
		for(Edge e : edges)
		{
			if(e.getDestinationNumber() == dest)
			{
				id = e.getDestinationId();
				break;
			}
		}
		return id;
	}
	public ArrayList<Term> getTransitionSparseMatrix()
	{
		int row, col;
		double val;
		 ArrayList<Term> transition = new ArrayList<Term>();
		for(Term term : adjMatrix)
		{
			row = term.getRowIndex();
			col = term.getColumnIndex();
			val = 1.0 / getNumberOfOutlinks(getSourceNodeId(row));
			Term newTerm = new Term(row, col, val);
			transition.add(newTerm);
		}
		return transition;
	}
	public String getNodeId(int number)
	{
		return numToIdMap.get(number);
	}
	private void makeNumToIdMap()
	{
		System.out.println("making numToIDMap");
		for(Node node : nodes)
		{
			System.out.println(node.getNodeNumber()+" "+node.getNodeId());
			numToIdMap.put(node.getNodeNumber(), node.getNodeId());
		}
	}
	private void makeAdjacencySparseMatrix()
	{
		int count, u, v, num = 0;
		Term t; 
		Edge e;
		String para1, para2;
		List<String> list1, list2;
		adjMatrix.add(new Term());
		for(Node node1 : nodes)
		{
			count = 0;
			u = node1.getNodeNumber();
			para1 =  node1.getNodeId();
			list1 = node1.getEntityList();
			System.out.println("para1="+para1);
		
			for(Node node2 : nodes)
			{
				v = node2.getNodeNumber();
				para2 =  node2.getNodeId();
				list2 = node2.getEntityList();
				System.out.println("para2="+para2);
				if(isCommon(list1,list2))
				{
					System.out.println("adding an edge");
					t = new Term(u, v, 1);
					e = new Edge(u,para1,v,para2);
					adjMatrix.add(t);
					edges.add(e);
					num++;
					count++;
				}
			}
			System.out.println("added"+" "+count+" "+"edges for"+" "+para1);
			outlinks.put(node1.getNodeId(), count);
		}
		adjMatrix.add(0,new Term(nodes.size(), nodes.size(), num));
	}
	
	private boolean isCommon(List<String> list1, List<String> list2)
	{
		boolean flag = false;
		for(String s1 : list1)
			for(String s2 : list2)
				if(s1.equalsIgnoreCase(s2))
				{
					flag = true;
					break;
				}
		return flag;
	}
}
