package org.csiro.igsn.nat.server.response;

import java.util.Hashtable;

public class LuceneStats {
	
	private String statsGroup;
	
	private Hashtable<String, Integer> statsTable;
	
	
	
	public  LuceneStats(String statsGroup){
		this.statsGroup = statsGroup;
		statsTable = new Hashtable<String, Integer>();
	}
	
	public void add(String stats, Integer count){
		statsTable.put(stats, statsTable.get(stats) + count);
	}
	
	public void put(String stats, Integer count){
		statsTable.put(stats, count);
		
	}
	
	public boolean contains(String stats){
		return statsTable.contains(stats);
	}
	
	public Hashtable<String, Integer> getStatsTable(){
		return statsTable;
	}

	public String getStatsGroup() {
		// TODO Auto-generated method stub
		return statsGroup;
	}
	
	
	public String toString(){
		String s = this.statsGroup  + ":" + statsTable;
		return s;
				
	}

}
