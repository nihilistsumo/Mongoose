package carHypertextGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class Graph 
{
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private ArrayList<Term> adjMatrix;
	private HashMap<String, Data.Paragraph> paraToIDMap;
	private HashMap<Integer,String> numToIdMap;
	private HashMap<String,Integer> outlinks;
	
	public Graph(String cborParaFilePath, String paraRunFilePath)
	{
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		adjMatrix = new ArrayList<Term>();
		paraToIDMap = new HashMap<String, Data.Paragraph>();
		numToIdMap = new HashMap<Integer,String>();
		outlinks = new HashMap<String,Integer>();
		makeParaToIDMap(cborParaFilePath);
		getNodeSet(paraRunFilePath);
		makeAdjacencySparseMatrix();
		makeNumToIdMap();
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
		for(Node node : nodes)
			numToIdMap.put(node.getNodeNumber(), node.getNodeId());
	}
	private void makeAdjacencySparseMatrix()
	{
		int count, u, v, num = 0;
		Term t; 
		Edge e;
		Data.Paragraph para1, para2;
		List<String> list1, list2;
		adjMatrix.add(new Term());
		for(Node node1 : nodes)
		{
			count = 0;
			u = node1.getNodeNumber();
			para1 = paraToIDMap.get(node1.getNodeId());
			System.out.println("para1="+para1);
			list1 = para1.getEntitiesOnly();
			for(Node node2 : nodes)
			{
				para2 = paraToIDMap.get(node2.getNodeId());
				System.out.println("para2="+para2);
				list2 = para2.getEntitiesOnly();
				v = node2.getNodeNumber();
				if(isCommon(list1,list2))
				{
					System.out.println("adding an edge");
					t = new Term(u, v, 1);
					e = new Edge(u,node1.getNodeId(),v,node2.getNodeId());
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
	private void makeParaToIDMap(String cborParaFilePath)
	{
		try 
		{
			for(Data.Paragraph para : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
				paraToIDMap.put(para.getParaId(),para);
		} 
		catch (CborRuntimeException e) 
		{
			e.printStackTrace();
		} 
		catch (CborFileTypeException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	private void getNodeSet(String paraRunFilePath)
	{
		BufferedReader br = null;
		int num = 0;
		try 
		{
			br = new BufferedReader(new FileReader(paraRunFilePath));
			String line;
			try 
			{
				while((line = br.readLine()) != null)
				{
					String s = line.split(" ")[2];
					nodes.add(new Node(s,num));
					num++;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
}
