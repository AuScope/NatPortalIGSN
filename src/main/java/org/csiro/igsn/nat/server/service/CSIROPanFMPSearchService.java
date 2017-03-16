package org.csiro.igsn.nat.server.service;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.search.BooleanQuery;
import org.csiro.igsn.jaxb.oai.bindings.igsn.Resource;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchService;

@Service
public class CSIROPanFMPSearchService extends PanFMPSearchService{
	
	@Value("#{configProperties['TEST_PANFMP_CONFIG_FILE_LOCATION']}")
	private String TEST_PANFMP_CONFIG_FILE_LOCATION;
	
	@Value("#{configProperties['TEST_PANFMP_CONFIG_FILE_INDEX']}")
	private String TEST_PANFMP_CONFIG_FILE_INDEX;
	
	@Value("#{configProperties['TEST_PANFMP_CONFIG_LUCENCE_DIR']}")
	private String TEST_PANFMP_CONFIG_LUCENCE_DIR;
	
	
	

	
	
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
	
	public Resource search(String identifier) throws Exception {
		SearchService service=new SearchService(TEST_PANFMP_CONFIG_FILE_LOCATION, TEST_PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("identifier",identifier), org.apache.lucene.search.BooleanClause.Occur.MUST);

		SearchResultCollectorImpl searchResultCollectorImpl = new SearchResultCollectorImpl();		
		service.search(searchResultCollectorImpl, bq);
		//VT:There will always ever only  be one since we are searching by identifier
		return searchResultCollectorImpl.getResource();
	}
	
	
	

	
	
	
	
	private class SearchResultCollectorImpl implements SearchResultCollector{
		
		Resource resource;

		@Override
		 public boolean collect(SearchResultItem item) {
			   
			JAXBContext jaxbContext;
			try {
				jaxbContext = JAXBContext.newInstance(Resource.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				this.resource = (Resource) jaxbUnmarshaller.unmarshal(new StringReader(item.getXml()));
			
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		     return true; // return false to stop collecting results
		   }
		
		public Resource getResource(){
			return this.resource;
		}
		
	}


	@Override
	public String getLuceneDir() {
		return this.TEST_PANFMP_CONFIG_LUCENCE_DIR; 
	}

	@Override
	public String getStoreLocation() {
		return this.TEST_PANFMP_CONFIG_FILE_LOCATION;
	}

	@Override
	public String getStoreIndex() {
		return this.TEST_PANFMP_CONFIG_FILE_INDEX;
	}

	@Override
	public void createSummaryResponse(List<SearchResultItem> searchResultItems,int size,List<SampleSummaryResponse> responses) {
		for (SearchResultItem item : searchResultItems) {
			
			SampleSummaryResponse summaryResponse= new SampleSummaryResponse();	
			if(item.getField("longtitude")!=null && item.getField("latitude")!= null){
				summaryResponse.setLongitude((Double)item.getField("longtitude")[0]);
				summaryResponse.setLatitude((Double)item.getField("latitude")[0]);
			}
			
			summaryResponse.setIgsn((String)item.getField("identifier")[0]);
			summaryResponse.setName((String)item.getField("title")[0]);
			if(item.getField("wkt")!=null){
				summaryResponse.setWkt((String)item.getField("wkt")[0]);
			}
			summaryResponse.setLandingPage((String)item.getField("identifier")[0]);     			
			summaryResponse.setSearchResultCount(size);
			summaryResponse.setMessage(parseMessage((String)item.getField("title")[0]));
			responses.add(summaryResponse);
		} 	
		
	}

	
	

}
