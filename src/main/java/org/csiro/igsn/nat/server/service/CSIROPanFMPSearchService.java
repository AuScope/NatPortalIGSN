package org.csiro.igsn.nat.server.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.search.BooleanQuery;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchService;

@Service
public class CSIROPanFMPSearchService extends PanFMPSearchService{
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_LOCATION']}")
	private String PANFMP_CONFIG_FILE_LOCATION;
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_INDEX']}")
	private String PANFMP_CONFIG_FILE_INDEX;
	
	@Value("#{configProperties['PANFMP_CONFIG_LUCENCE_DIR']}")
	private String PANFMP_CONFIG_LUCENCE_DIR;
	
	
	

	
	
	private String parseMessage(String name) {
		
		String message = "<p>";
		message += "Name:" + name + "<br>";	
		message += "</p>";			
		
		return message;
	}
	
//	private String parseMessage(JAXBElement<SamplingLocation> samplingLocationJaxB) {
//		SamplingLocation samplingLocation = samplingLocationJaxB.getValue();
//		if(samplingLocationJaxB.isNil()){
//			return samplingLocationJaxB.getValue().getNilReason();
//		}
//		String message = "<p>";
//		message += samplingLocation.getLocality()==null?"":"<b>Locality:</b> " + samplingLocation.getLocality() + "<br>";
//		message += samplingLocation.getElevation()==null?"":"<b>Elevation:</b> " + samplingLocation.getElevation().getValue() + "<br>";
//		message += samplingLocation.getElevation()==null?"":"<b>Datum:</b> " + samplingLocation.getElevation().getDatum() + "<br>";
//		message += samplingLocation.getElevation()==null?"":"<b>Units:</b> " + samplingLocation.getElevation().getUnits() + "<br>";
//		message += "</p>";			
//		
//		return message;
//	}
	
	public Samples search(String igsn) throws Exception {
		SearchService service=new SearchService(PANFMP_CONFIG_FILE_LOCATION, PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("sampleNumber",igsn), org.apache.lucene.search.BooleanClause.Occur.MUST);

		SearchResultCollectorImpl searchResultCollectorImpl = new SearchResultCollectorImpl();		
		service.search(searchResultCollectorImpl, bq);
		//VT:There will always ever only  be one since we are searching by igsn
		return searchResultCollectorImpl.getSamples().get(0);
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


	@Override
	public String getLuceneDir() {
		return this.PANFMP_CONFIG_LUCENCE_DIR; 
	}

	@Override
	public String getStoreLocation() {
		return this.PANFMP_CONFIG_FILE_LOCATION;
	}

	@Override
	public String getStoreIndex() {
		return this.PANFMP_CONFIG_FILE_INDEX;
	}

	@Override
	public void createSummaryResponse(List<SearchResultItem> searchResultItems,int size,List<SampleSummaryResponse> responses) {
		for (SearchResultItem item : searchResultItems) {
			
			SampleSummaryResponse summaryResponse= new SampleSummaryResponse();	
			if(item.getField("longtitude")!=null && item.getField("latitude")!= null){
				summaryResponse.setLongitude((Double)item.getField("longtitude")[0]);
				summaryResponse.setLatitude((Double)item.getField("latitude")[0]);
			}
			
			summaryResponse.setIgsn((String)item.getField("sampleNumber")[0]);
			summaryResponse.setName((String)item.getField("sampleName")[0]);			
			summaryResponse.setLogTimeStamp((String)item.getField("logtime")[0]);
			summaryResponse.setLandingPage((String)item.getField("landingPage")[0]);     			
			summaryResponse.setSearchResultCount(size);
			summaryResponse.setMessage(parseMessage((String)item.getField("sampleName")[0]));
			responses.add(summaryResponse);
		} 	
		
	}

	
	

}
