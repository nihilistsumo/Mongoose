package edu.unh.cs.treccar.proj.entlinkqe;

import java.nio.file.Path;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.io.*;
import java.util.*;

/*
 * @author Ajesh Vijayaragavan
 * Graph clustering using Breadth first Traversal
 * 
 */

public class graphClustering {
	
	public static void main(String[] args) throws IOException {
		
		 //String graph = args[0];
		 Scanner scanner = new Scanner(new FileReader(args[0]));
		 HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
		 
		 int edges = 0;
         while (scanner.hasNextLine()) 
         {
         	String[] columns = scanner.nextLine().split("\\s+");
         	ArrayList<String> list = new ArrayList<String>();
         	for (int i = 1; i < columns.length; i++) {
         		list.add(columns[i]);
         		edges += 1;
         	}
         	
         	hashMap.put(columns[0], list);      
         }

/*
public static void breadthFirstTraversal(HashMap<String, List<String>> hashMap2) 
{
    Dequeue<edges> myQ = new LinkedList<edges>();
    myQ.add(node);
    while(!myQ.empty()) {
        String current = myQ.getFirst();
        current.visited = true;
        List<String> edges1 = hashMap2.getvalue(current);
        for (String neighbor : edges) 
        {
            if (!neighbor.visited) 
            {
                myQ.addLast(neighbor);
            }
        }
    }
}*/
	}
}