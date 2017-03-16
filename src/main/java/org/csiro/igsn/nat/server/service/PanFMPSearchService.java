package org.csiro.igsn.nat.server.service;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.csiro.igsn.jaxb.oai.bindings.igsn.Resource;
import org.csiro.igsn.nat.server.response.LuceneStats;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;

import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchResultList;
import de.pangaea.metadataportal.search.SearchService;

public abstract class PanFMPSearchService {
	
	//VT: this should probably be in an object rather than 3 arrays however this just makes it easier to init the config.
	private static final String [] filterKeys = {"material","resourceType","searchText"};
	private static final String [] filterKeysDisplayName = {"Material","Resource Type","Text Search"};
	private static final String [] filterKeysType = {"List","List","Text"};

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
	
	public abstract Resource search(String igsn) throws Exception;
	
	public abstract String getStoreLocation();
	
	public abstract String getStoreIndex();
	
	public abstract void createSummaryResponse(List<SearchResultItem> searchResultItems,int size,List<SampleSummaryResponse> responses);
	
	
	public void search(String []resourceType,String [] material,String searchText, Double [] latitudeBound, Double [] longitudeBound,Integer pageNumber, Integer pageSize,List<SampleSummaryResponse> responses) throws Exception{
		
		SearchService service = new SearchService(getStoreLocation(), getStoreIndex()); 
		BooleanQuery latlngQuery = service.newBooleanQuery();
		BooleanQuery resourceTypeQuery = service.newBooleanQuery();
		BooleanQuery materialTypeQuery = service.newBooleanQuery();
		BooleanQuery textQuery = service.newBooleanQuery();
		
		BooleanQuery bq = service.newBooleanQuery();
		
		if(searchText != null && !searchText.isEmpty()){
			textQuery.add(service.newTextQuery("title",searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("identifierId",searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("identifier",searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("alternateidentifier", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);			
			textQuery.add(service.newTextQuery("contributorIdentifier", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("contributorName",  searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);			
			textQuery.add(service.newTextQuery("supplementalMetaData",   searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("description",  searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("relatedResource",searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("collector", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("collectorAffiliation", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);			
		}
		
//		if(sampleName != null){
//			and.add(service.newTextQuery("sampleName", sampleName), org.apache.lucene.search.BooleanClause.Occur.MUST);
//		}

		
		if(latitudeBound.length==2 && longitudeBound.length==2){			
			latlngQuery.add(service.newNumericRangeQuery("latitude", latitudeBound[0], latitudeBound[1]), org.apache.lucene.search.BooleanClause.Occur.MUST);
			latlngQuery.add(service.newNumericRangeQuery("longtitude", longitudeBound[0], longitudeBound[1]), org.apache.lucene.search.BooleanClause.Occur.MUST);			
		}
		
		
		for(String materialType:material){
			materialTypeQuery.add(service.newTextQuery("material", URLDecoder.decode(materialType, "UTF-8")), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		for(String resource:resourceType){
			resourceTypeQuery.add(service.newTextQuery("resourceType", URLDecoder.decode(resource, "UTF-8")), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		
	
		
		if(latlngQuery.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(latlngQuery,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}
		if(resourceTypeQuery.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(resourceTypeQuery,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}
		if(materialTypeQuery.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(materialTypeQuery,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}
//		if(or.getClauses().length!=0){
//			bq.add(new org.apache.lucene.search.BooleanClause(or,org.apache.lucene.search.BooleanClause.Occur.MUST));
//		}
		if(textQuery.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(textQuery,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}

		// create a Sort, if you want standard sorting by relevance use
		// sort=null
		Sort sort = service.newSort(service.newFieldBasedSort("timestamp", true));
		// start search
		SearchResultList list = null;
		if(bq.getClauses().length==0){			
			list = service.search(service.newMatchAllDocsQuery(), sort);						
		}else{
			list = service.search(bq, sort);
		}
		//VT:We can tranverse through the list and collect stats via item.getFields().get("curators") however it won't be efficient if we dealing with 20k results
		// print search results (start is item to start with, count is number of
		// results)
		int start = (pageNumber-1) * pageSize, count = pageSize;

		List<SearchResultItem> page = list.subList(Math.min(start, list.size()), Math.min(start + count, list.size()));	
		
		createSummaryResponse(page,list.size(),responses);
		
	};
	
	
	
	

}
