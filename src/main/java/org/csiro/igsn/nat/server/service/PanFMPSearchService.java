package org.csiro.igsn.nat.server.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.nat.server.response.LuceneStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchResultList;
import de.pangaea.metadataportal.search.SearchService;

@Service
public class PanFMPSearchService {
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_LOCATION']}")
	private String PANFMP_CONFIG_FILE_LOCATION;
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_INDEX']}")
	private String PANFMP_CONFIG_FILE_INDEX;
	
	@Value("#{configProperties['PANFMP_CONFIG_LUCENCE_DIR']}")
	private String PANFMP_CONFIG_LUCENCE_DIR;
	
	//VT: this should probably be in an object rather than 3 arrays however this just makes it easier to init the config.
	private static final String [] filterKeys = {"materialType","sampleType","samplingFeature","curators","sampleCollector","searchText"};
	private static final String [] filterKeysDisplayName = {"Material Type","Sample Type","SamplingFeature","Curators","Sample Collector","Text Search"};
	private static final String [] filterKeysType = {"List","List","List","Combo","Combo","Text"};
	

	public List<Samples> search(String igsn, String sampleName,String [] materialTypes,String curators,String sampleCollector, String [] sampleTypes,String [] samplingFeatureTypes,
			String searchText, Double [] latitudeBound, Double [] longitudeBound,Integer pageNumber, Integer pageSize,MutableInt resultCount) throws Exception {
		
		SearchService service = new SearchService(PANFMP_CONFIG_FILE_LOCATION, PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery or = service.newBooleanQuery();
		BooleanQuery and = service.newBooleanQuery();
		BooleanQuery textQuery = service.newBooleanQuery();
		
		BooleanQuery bq = service.newBooleanQuery();
		
		if(searchText != null && !searchText.isEmpty()){
			textQuery.add(service.newTextQuery("sampleName", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("sampleNumber", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
			textQuery.add(service.newTextQuery("description", searchText + "*"), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		if(sampleName != null){
			and.add(service.newTextQuery("sampleName", sampleName), org.apache.lucene.search.BooleanClause.Occur.MUST);
		}
		if(igsn != null){
			and.add(service.newTextQuery("sampleNumber", igsn), org.apache.lucene.search.BooleanClause.Occur.MUST);
		}

		if(curators != null){
			and.add(service.newTextQuery("curators", curators), org.apache.lucene.search.BooleanClause.Occur.MUST);
		}
		
		if(sampleCollector != null){
			and.add(service.newTextQuery("sampleCollector", sampleCollector), org.apache.lucene.search.BooleanClause.Occur.MUST);
		}
		
		if(latitudeBound.length==2 && longitudeBound.length==2){			
			and.add(service.newNumericRangeQuery("latitude", latitudeBound[0], latitudeBound[1]), org.apache.lucene.search.BooleanClause.Occur.MUST);
			and.add(service.newNumericRangeQuery("longtitude", longitudeBound[0], longitudeBound[1]), org.apache.lucene.search.BooleanClause.Occur.MUST);			
		}
		
		
		for(String materialType:materialTypes){
			or.add(service.newTextQuery("materialType", URLDecoder.decode(materialType, "UTF-8")), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		for(String sampleType:sampleTypes){
			or.add(service.newTextQuery("sampleType", URLDecoder.decode(sampleType, "UTF-8")), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		
		for(String samplingFeatureType :samplingFeatureTypes){
			or.add(service.newTextQuery("samplingFeature", URLDecoder.decode(samplingFeatureType, "UTF-8") ), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
		}
		
		if(and.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(and,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}
		if(or.getClauses().length!=0){
			bq.add(new org.apache.lucene.search.BooleanClause(or,org.apache.lucene.search.BooleanClause.Occur.MUST));
		}
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
		resultCount.setValue(list.size());

		ArrayList<Samples> samples = new ArrayList<Samples>();

		for (SearchResultItem item : page) {
			JAXBContext jaxbContext;
			try {
				jaxbContext = JAXBContext.newInstance(Samples.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Samples sample = (Samples) jaxbUnmarshaller.unmarshal(new StringReader(item.getXml()));
				samples.add(sample);
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return samples;

	}
	
	public Samples search(String igsn) throws Exception {
		SearchService service=new SearchService(PANFMP_CONFIG_FILE_LOCATION, PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("sampleNumber",igsn), org.apache.lucene.search.BooleanClause.Occur.MUST);

		SearchResultCollectorImpl searchResultCollectorImpl = new SearchResultCollectorImpl();		
		service.search(searchResultCollectorImpl, bq);
		//VT:There will always ever only  be one since we are searching by igsn
		return searchResultCollectorImpl.getSamples().get(0);
	}
	
	public List<LuceneStats> getAllStats() throws IOException{
		ArrayList<LuceneStats> result = new ArrayList<LuceneStats>();
		for(int i=0;i < filterKeys.length;i++){
			result.add(getStats(filterKeys[i],filterKeysDisplayName[i],filterKeysType[i]));
		}
		return result;
	}
	
	public LuceneStats getStats(String statsgroup,String displayName,String displayType) throws IOException{
		if(displayType.equals("Text")){
			return  new LuceneStats(statsgroup,displayName,displayType);
		}
		Directory dir = new MMapDirectory(new File(PANFMP_CONFIG_LUCENCE_DIR));
		IndexReader ir = IndexReader.open(dir);
		LuceneStats result = new LuceneStats(statsgroup,displayName,displayType);
		
		TermEnum termEnum=ir.terms(new Term(statsgroup));
		
		extractStats(termEnum,result);
		
		return result;
	
	}
	
	private void extractStats(TermEnum termEnum,LuceneStats luceneStats) throws IOException{
		
		if(termEnum.term().field().equalsIgnoreCase(luceneStats.getStatsGroup())){
			luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), termEnum.docFreq());
		}
		
		while(termEnum.next() && termEnum.term().field().equalsIgnoreCase(luceneStats.getStatsGroup())){
			luceneStats.put(extractSummaryFromIdentifier(termEnum.term().text()),termEnum.term().text(), termEnum.docFreq());
		}

		
	}
	
	private String extractSummaryFromIdentifier(String identifer){
		String [] tokens = identifer.split("/");
		return tokens[tokens.length-1];
	}
	

	
	
	
	
	private class SearchResultCollectorImpl implements SearchResultCollector{
		
		ArrayList<Samples> samples=new ArrayList<Samples>();

		@Override
		 public boolean collect(SearchResultItem item) {
			   
			JAXBContext jaxbContext;
			try {
				jaxbContext = JAXBContext.newInstance(Samples.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Samples sample = (Samples) jaxbUnmarshaller.unmarshal(new StringReader(item.getXml()));
				this.samples.add(sample); 
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		     return true; // return false to stop collecting results
		   }
		
		public List<Samples> getSamples(){
			return this.samples;
		}
		
	}

	
	

}
