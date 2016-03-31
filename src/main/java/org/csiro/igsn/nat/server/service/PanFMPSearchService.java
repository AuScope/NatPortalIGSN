package org.csiro.igsn.nat.server.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.nat.server.response.LuceneStats;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;

public abstract class PanFMPSearchService {
	
	//VT: this should probably be in an object rather than 3 arrays however this just makes it easier to init the config.
	private static final String [] filterKeys = {"materialType","sampleType","samplingFeature","curators","sampleCollector","searchText"};
	private static final String [] filterKeysDisplayName = {"Material Type","Sample Type","SamplingFeature","Curators","Sample Collector","Text Search"};
	private static final String [] filterKeysType = {"List","List","List","Combo","Combo","Text"};

	public void getAllStats(ArrayList<LuceneStats> result) throws IOException{
		//VT: Stats already populated
		if(result.size()==filterKeys.length){
			for(LuceneStats stat: result){
				populatedStat(stat);				
			}
			return;
		}
		
		//VT: First entry into populating the stats
		for(int i=0;i < filterKeys.length;i++){
			LuceneStats stat = new LuceneStats(filterKeys[i],filterKeysDisplayName[i],filterKeysType[i]);
			populatedStat(stat);
			result.add(stat);
		}
		
	}
	
	
	public void populatedStat(LuceneStats result) throws IOException{
		
		Directory dir = new MMapDirectory(new File(this.getLuceneDir()));
		IndexReader ir = IndexReader.open(dir);
	
		if(result.getType().equals("Text")){
			return;
		}
		
		TermEnum termEnum=ir.terms(new Term(result.getStatsGroup()));
		
		extractStats(termEnum,result);
					
	}
	
	private void extractStats(TermEnum termEnum,LuceneStats luceneStats) throws IOException{
		
		if(termEnum.term().field().equalsIgnoreCase(luceneStats.getStatsGroup())){
			if(luceneStats.getStatsTable().containsKey(extractSummaryFromIdentifier(termEnum.term().text()))){
				luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), luceneStats.getStatsTable().get(extractSummaryFromIdentifier(termEnum.term().text())).getCount() + termEnum.docFreq());
			}else{
				luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), termEnum.docFreq());
			}
		}
		
		while(termEnum.next() && termEnum.term().field().equalsIgnoreCase(luceneStats.getStatsGroup())){
			if(luceneStats.getStatsTable().containsKey(extractSummaryFromIdentifier(termEnum.term().text()))){
				luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), luceneStats.getStatsTable().get(extractSummaryFromIdentifier(termEnum.term().text())).getCount() + termEnum.docFreq());
			}else{
				luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), termEnum.docFreq());
			}
		}

		
	}
	
	private String extractSummaryFromIdentifier(String identifer){
		String [] tokens = identifer.split("/");
		return tokens[tokens.length-1];
	}
	
	
	public abstract String getLuceneDir();
	
	
	public abstract void search(String igsn, String sampleName,String [] materialTypes,String curators,String sampleCollector, String [] sampleTypes,String [] samplingFeatureTypes,
			String searchText, Double [] latitudeBound, Double [] longitudeBound,Integer pageNumber, Integer pageSize,List<SampleSummaryResponse> responses) throws Exception;
	
	
	public abstract Samples search(String igsn) throws Exception;
	

}
