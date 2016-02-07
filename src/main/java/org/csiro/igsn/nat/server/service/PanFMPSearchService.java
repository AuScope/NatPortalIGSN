package org.csiro.igsn.nat.server.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.search.BooleanQuery;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchService;

@Service
public class PanFMPSearchService {
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_LOCATION']}")
	private String PANFMP_CONFIG_FILE_LOCATION;
	
	@Value("#{configProperties['PANFMP_CONFIG_FILE_INDEX']}")
	private String PANFMP_CONFIG_FILE_INDEX;
	
	

	public List<Samples> search(String igsn, String sampleType, Integer pageNumber, Integer pageSize) throws Exception {
		
		SearchService service=new SearchService(PANFMP_CONFIG_FILE_LOCATION, PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("sampleName2","sample name"), org.apache.lucene.search.BooleanClause.Occur.MUST);
		
		
		SearchResultCollectorImpl searchResultCollectorImpl = new SearchResultCollectorImpl();
		
		service.search(searchResultCollectorImpl, bq);
		return searchResultCollectorImpl.getSamples();
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
