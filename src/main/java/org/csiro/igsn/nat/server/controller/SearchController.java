package org.csiro.igsn.nat.server.controller;

import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;








import org.apache.commons.lang3.mutable.MutableInt;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.csiro.igsn.nat.server.service.PanFMPSearchService;
import org.csiro.igsn.utilities.SpatialUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vividsolutions.jts.geom.Point;

@Controller
public class SearchController {
	
	PanFMPSearchService panFMPSearchService;
	
	@Autowired
	public SearchController(PanFMPSearchService panFMPSearchService){
		this.panFMPSearchService = panFMPSearchService;
	}
	
	@RequestMapping(value = "search.do")
    public ResponseEntity<Object> search(            
            @RequestParam(required = false, value ="igsn") String igsn,
            @RequestParam(required = false, value ="sampleName") String sampleName,
            @RequestParam(required = false, value ="materialType") String materialType,
            @RequestParam(required = false, value ="sampleCollector") String sampleCollector,
            @RequestParam(required = false, value ="sampleType") String sampleType,
            @RequestParam(required = false, value ="samplingFeatureType") String samplingFeatureType,
            @RequestParam(required = false, value ="pageNumber") Integer pageNumber, 
            @RequestParam(required = false, value ="pageSize") Integer pageSize, 
            Principal user,
            HttpServletResponse response) {
    	
    	try{
    		MutableInt resultCount=new MutableInt() ;
    		List<Samples> samples = panFMPSearchService.search(igsn, sampleName, 
    						materialType==null?null:URLDecoder.decode(materialType, "UTF-8"), 
    						sampleCollector,  sampleType, samplingFeatureType, pageNumber,pageSize,resultCount);
    		List<SampleSummaryResponse> responses= new ArrayList<SampleSummaryResponse>();
    		
    		for(Samples s: samples){
    			
    			SampleSummaryResponse summaryResponse= new SampleSummaryResponse();
    			Sample sample=s.getSample().get(0);
    			
    			if(sample.getSamplingLocation().isNil()==false && sample.getSamplingLocation().getValue().getWkt()!=null){
	    			Point point =(Point)SpatialUtilities.wktToGeometry(sample.getSamplingLocation().getValue().getWkt().getValue());
	    			summaryResponse.setLongitude(point.getCoordinate().x);
	    			summaryResponse.setLatitude(point.getCoordinate().y);
    			}
    			
    			summaryResponse.setIgsn(sample.getSampleNumber().getValue());
    			summaryResponse.setName(sample.getSampleName());
    			summaryResponse.setLogTimeStamp(sample.getLogElement().getTimeStamp());
    			summaryResponse.setLandingPage(sample.getLandingPage());     			
    			summaryResponse.setSearchResultCount(resultCount.getValue());
    			responses.add(summaryResponse);
    		}
		    return  new ResponseEntity<Object>(responses,HttpStatus.OK);    		
	    	
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error updating",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }
	
	@RequestMapping(value = "getDetailed.do")
    public ResponseEntity<Object> getDetailed(            
            @RequestParam(required = true, value ="igsn") String igsn,
            Principal user,
            HttpServletResponse response) {
    	
    	try{
    		Samples samples = panFMPSearchService.search(igsn);
    		
		    return  new ResponseEntity<Object>(samples,HttpStatus.OK);    		
	    	
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error updating",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }
	
	@RequestMapping(value = "getStats.do")
    public ResponseEntity<Object> getStats(  
    	   @RequestParam(required = false, value ="statsGroup") String statsGroup,
            Principal user,
            HttpServletResponse response) {
		
    	try{
    		if(statsGroup==null){
    			return  new ResponseEntity<Object>(panFMPSearchService.getAllStats(),HttpStatus.OK);
    		}else{
    			return  new ResponseEntity<Object>(panFMPSearchService.getStats(statsGroup),HttpStatus.OK);
    		}
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error getting stats",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }

}
