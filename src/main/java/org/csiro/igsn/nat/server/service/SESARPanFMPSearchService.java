package org.csiro.igsn.nat.server.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;














import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
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
import org.csiro.igsn.bindings.allocation2_0.EventType;
import org.csiro.igsn.bindings.allocation2_0.IdentifierType;
import org.csiro.igsn.bindings.allocation2_0.NilReasonType;
import org.csiro.igsn.bindings.allocation2_0.ObjectFactory;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.MaterialTypes;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SampleCollectors;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SampleTypes;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SamplingLocation;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SamplingMethod;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SamplingTime;
import org.csiro.igsn.nat.server.response.LuceneStats;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.csiro.igsn.utilities.SpatialUtilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Point;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchResultList;
import de.pangaea.metadataportal.search.SearchService;

@Service
public class SESARPanFMPSearchService extends PanFMPSearchService{
	
	@Value("#{configProperties['SESAR_PANFMP_CONFIG_FILE_LOCATION']}")
	private String PANFMP_CONFIG_FILE_LOCATION;
	
	@Value("#{configProperties['SESAR_PANFMP_CONFIG_FILE_INDEX']}")
	private String PANFMP_CONFIG_FILE_INDEX;
	
	@Value("#{configProperties['SESAR_PANFMP_CONFIG_LUCENCE_DIR']}")
	private String PANFMP_CONFIG_LUCENCE_DIR;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	
	
	private String parseMessage(String name) {
		
		String message = "<p>";
		message += "Name:" + name + "<br>";	
		message += "</p>";			
		
		return message;
	}
	
	public Samples search(String igsn) throws Exception {
		SearchService service=new SearchService(PANFMP_CONFIG_FILE_LOCATION, PANFMP_CONFIG_FILE_INDEX);
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("sampleNumber",igsn), org.apache.lucene.search.BooleanClause.Occur.MUST);

		SearchResultCollectorImpl searchResultCollectorImpl = new SearchResultCollectorImpl();		
		service.search(searchResultCollectorImpl, bq);
		//VT:There will always ever only  be one since we are searching by igsn
		return searchResultCollectorImpl.getSamples();
	}
	
	
	private class SearchResultCollectorImpl implements SearchResultCollector{
		
		Samples samples;

		@Override
		 public boolean collect(SearchResultItem item) {
			   
			
			try {
				ObjectFactory of = new ObjectFactory();
				samples = of.createSamples();
				Sample sample = new Sample();
				
				//VT:Name
				sample.setSampleName((String)item.getField("sampleName")[0]);
				
				//VT:Sample number
				Samples.Sample.SampleNumber sampleNumber = new Samples.Sample.SampleNumber();
				sampleNumber.setValue((String)item.getField("sampleNumber")[0]);
				sampleNumber.setIdentifierType(IdentifierType.IGSN);
				sample.setSampleNumber(sampleNumber);
				
				//VT:Material Type
				Samples.Sample.MaterialTypes materialType = of.createSamplesSampleMaterialTypes();
				JAXBElement<MaterialTypes> materialTypeJaxB = of.createSamplesSampleMaterialTypes(materialType);
				if(item.getField("materialType") != null){
					materialTypeJaxB.setNil(false);
					for(Object material:item.getField("materialType")){
						materialType.getMaterialType().add((String)material);
					}
				}else{
					materialTypeJaxB.setNil(true);
					materialType.setNilReason(NilReasonType.MISSING.value());
				}
				sample.setMaterialTypes(materialTypeJaxB);
				
				
				//VT: sampleCollector
				Samples.Sample.SampleCollectors sampleCollector = of.createSamplesSampleSampleCollectors();
				JAXBElement<SampleCollectors> sampleCollectorJaxb = of.createSamplesSampleSampleCollectors(sampleCollector);
				if(item.getField("sampleCollector") != null){
					sampleCollectorJaxb.setNil(false);
					for(Object sampleCollectorObj:item.getField("sampleCollector")){
						Samples.Sample.SampleCollectors.Collector collector = new Samples.Sample.SampleCollectors.Collector();
						collector.setValue((String)sampleCollectorObj);
						sampleCollector.getCollector().add(collector);
					}
				}else{
					sampleCollectorJaxb.setNil(true);
					sampleCollector.setNilReason(NilReasonType.MISSING.value());
				}
				
				sample.setSampleCollectors(sampleCollectorJaxb);
				
				
				//VT:Curators
				if(item.getField("curators").length > 0){
					Samples.Sample.SampleCuration sampleCuration = new Samples.Sample.SampleCuration();					
					for(Object curationObj: item.getField("curators")){
						Samples.Sample.SampleCuration.Curation curation = new Samples.Sample.SampleCuration.Curation();
						curation.setCurator((String)curationObj);
						sampleCuration.getCuration().add(curation);
					}
					sample.setSampleCuration(sampleCuration);
				}
				
				//VT: sampleType
				Samples.Sample.SampleTypes sampleTypes = of.createSamplesSampleSampleTypes();
				JAXBElement<SampleTypes> sampleTypesJaxb = of.createSamplesSampleSampleTypes(sampleTypes);
				if(item.getField("sampleType") != null){
					sampleTypesJaxb.setNil(false);
					for(Object o:item.getField("sampleType")){
						sampleTypes.getSampleType().add((String)o);
					}
				}else{
					sampleTypesJaxb.setNil(true);
					sampleTypes.setNilReason(NilReasonType.MISSING.value());
				}
				sample.setSampleTypes(sampleTypesJaxb);
				
				//VT: logtime
				if(item.getField("logtime") != null){
					Samples.Sample.LogElement logElement = new Samples.Sample.LogElement();
					logElement.setEvent(EventType.SUBMITTED);
					logElement.setValue("Registered");
					if(item.getField("logtime")!=null){					
						
						logElement.setTimeStamp(dateFormat.format((Date)item.getField("logtime")[0]));
					}
					sample.setLogElement(logElement);
				}
				
				//VT:landingPage
				sample.setLandingPage((String)item.getField("landingPage")[0]);
				
				//VT: Othername
				if(item.getField("otherName") != null){
					Samples.Sample.OtherNames otherNameList = new Samples.Sample.OtherNames();
					for( Object otherName: item.getField("otherName")){					
						otherNameList.getOtherName().add((String)otherName);										
					}
					
					sample.setOtherNames(otherNameList); 
					
				}
				
				if(item.getField("description") != null){
					sample.setComments((String)item.getField("description")[0]);
				}
				
				//VT:SamplingMethod
				Samples.Sample.SamplingMethod samplingMethod = of.createSamplesSampleSamplingMethod();
				JAXBElement<SamplingMethod> samplingMethodJaxb = of.createSamplesSampleSamplingMethod(samplingMethod);
				samplingMethod.setNilReason(NilReasonType.MISSING.value());
				samplingMethodJaxb.setNil(true);
				sample.setSamplingMethod(samplingMethodJaxb);
				
				//VT:sampling time
				Samples.Sample.SamplingTime samplingTime = of.createSamplesSampleSamplingTime();
				JAXBElement<SamplingTime> samplingTimeJaxb = of.createSamplesSampleSamplingTime(samplingTime);
				if(item.getField("logtime") != null){					
					samplingTimeJaxb.setNil(false);
					samplingTime.setTimeInstant(dateFormat.format((Date)item.getField("logtime")[0]));
				}else{
					samplingTimeJaxb.setNil(true);
					samplingTime.setNilReason(NilReasonType.MISSING.value());
				}
				sample.setSamplingTime(samplingTimeJaxb);
				
				samples.getSample().add(sample);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		     return false; // return false to stop collecting results
		   }
		
		public Samples getSamples(){
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
	public void createSummaryResponse(List<SearchResultItem> searchResultItems, int size,
			List<SampleSummaryResponse> responses) {
		for (SearchResultItem item : searchResultItems) {
			
			SampleSummaryResponse summaryResponse= new SampleSummaryResponse();	
			if(item.getField("longtitude")!=null && item.getField("latitude")!= null){
				summaryResponse.setLongitude((Double)item.getField("longtitude")[0]);
				summaryResponse.setLatitude((Double)item.getField("latitude")[0]);
			}
			
			summaryResponse.setIgsn((String)item.getField("sampleNumber")[0]);
			summaryResponse.setName((String)item.getField("sampleName")[0]);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date)item.getField("logtime")[0]);
			summaryResponse.setLogTimeStamp(String.valueOf(calendar.get(Calendar.YEAR)));
			summaryResponse.setLandingPage((String)item.getField("landingPage")[0]);     			
			summaryResponse.setSearchResultCount(size);
			summaryResponse.setMessage(parseMessage((String)item.getField("sampleName")[0]));
			responses.add(summaryResponse);
		} 	
		
	}
	

}
