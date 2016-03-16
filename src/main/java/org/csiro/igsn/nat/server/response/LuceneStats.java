package org.csiro.igsn.nat.server.response;

import java.util.Hashtable;

public class LuceneStats {
	
	private String statsGroup;
	
	private String displayName;
	
	private String type;
	
	private Hashtable<String, StatInfo> statsTable;
	
	
	
	public  LuceneStats(String statsGroup,String displayName,String type){
		this.statsGroup = statsGroup;
		this.setDisplayName(displayName);
		this.setType(type);
		
		statsTable = new Hashtable<String, StatInfo>();
	}
	
	
	
	public void put(String stats, String value, Integer count){
		statsTable.put(stats, new StatInfo(value,count));
		
	}
	
	public boolean contains(String stats){
		return statsTable.contains(stats);
	}
	
	public Hashtable<String, StatInfo> getStatsTable(){
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}

	private class StatInfo{
		private String value;
		private Integer count;
		
		public StatInfo(String value, Integer count){
			this.setValue(value);
			this.setCount(count);
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		
	}

}
