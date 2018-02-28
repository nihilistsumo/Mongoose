package treccar;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class GraphTraversalShortestPath {

	public static void main(String[] args) throws FileNotFoundException {
    	Scanner scanner = new Scanner(new FileReader(args[0]));
            
    		//HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
    		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
    		// HashMap<Integer, ArrayList<Integer>> hashMap2 = new HashMap<Integer, ArrayList<Integer>>();
    		Map<Integer,Double> map = new HashMap<Integer,Double>();
              
    		int edges = 0;
            while (scanner.hasNextLine()) {
            	String[] columns = scanner.nextLine().split("\\s+");
            	ArrayList<String> list = new ArrayList<String>();
            	for (int i = 1; i < columns.length; i++) {
            		list.add(columns[i]);
            		edges += 1;
            	}            	
            	hashMap.put(columns[0], list);         
            }               
            //System.out.println(hashMap);
    		for(HashMap.Entry<String, List<String>> entry1: hashMap.entrySet()) {
     	       String key1 = entry1.getKey();
     	       for(HashMap.Entry<String, List<String>> entry2: hashMap.entrySet()) {
     	    	   String key2 = entry2.getKey();
     	    	   String path = "";
     	    	   PriorityQueue<String> queue=new PriorityQueue<String>(); 
     	    	   queue.add(key1);
     	    	   while (!key1.isEmpty() && queue.front() != key2)
     	    	   {
     	    		   int v1 = queue.front();
     	    		   path = path + v1 + " ";
     	    		   queue.poll();
     	    		   Object[] graph;
					   int total = ((HashMap<String, List<String>>) graph[v1]).size();
     	    		   for (int j = 0;j < total;j++)
     	    		   {
     	    			   map<Integer, list<Integer>>.iterator it2 = graph.find(((Object) graph[v1]).front());
     	    			   if (graph[v1].front() == key2)
     	    			   {
     	    				   while (!queue.isEmpty())
     	    				   {
     	    					   queue.remove();
     	    				   }
	    			  		queue.push(graph[v1].front());
	    			  		total = 0;
	    			  		}
	    			  		else if (it2 == graph.length)
	    			  		{
	    			  			   ((Object) graph[v1]).pollFirst();
	    			  		}
	    			  		else
	    			  		{
	    			  			   queue.push(graph[v1].front());
	    			  			   graph[v1].pollFirst();
	    			  		}
	    			  	}
		    		  }
		    		  if (queue.front() == key2)
		    		  {
		    		  System.out.println(key1);
		    		  System.out.println(key2);
		    		  System.out.println(path);
		    		  }
		    		else
		    		{
		    		  System.out.print("path doesn't exist.");
		    		}
		    }
	}
	}
}	