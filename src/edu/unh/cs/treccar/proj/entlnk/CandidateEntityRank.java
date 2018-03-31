package edu.unh.cs.treccar.proj.entlnk;

import java.util.ArrayList;
import java.util.Arrays;

import org.mapdb.HTreeMap;

public class CandidateEntityRank 
{
	private static HTreeMap<String,String> dictionary = CandidateEntityGeneration.getDictionary();
	private static ArrayList<Integer> count = new ArrayList<Integer>();
	private static ArrayList<Double> popularity = new ArrayList<Double>();
	
	public static String rank(String entity, ArrayList<String> candidate)
	{
		int sum = 0;
		double p;
		String tp;
		double [] pop;
		String[] cand;
		
		for(String s : candidate)
			count.add(getCount(entity, s));
		
		for(int i : count)
			sum += i;
		
		for(int i = 0; i < candidate.size(); i++)
		{
			p = count.get(i) / sum;
			popularity.add(p);
		}
		pop = popularity.stream().mapToDouble(Double::doubleValue).toArray();
		cand = candidate.toArray(new String[candidate.size()]);
		for(int i = 0; i < pop.length - 1; i++)
		{
			for(int j = 0; j < pop.length - i - 1; j++)
			{
				if(pop[j] > pop[j+1])
				{
					tp = cand[j];
					cand[j] = cand[j+1];
					cand[j+1] = tp;
				}
			}
		}
		return cand[0];
	}
	private static int getCount(String e, String str)
	{
		ArrayList<String> list = null;
		int count = 0;
		String st;
		for(Object s : dictionary.keySet())
		{
			if(e.equalsIgnoreCase((String) s))
			{
				st = dictionary.get(s);
				list = new ArrayList<String>( Arrays.asList(st.split("\\s+")));
				if(list.contains(str))
					count++;
			}
		}
		return count;
	}

}
