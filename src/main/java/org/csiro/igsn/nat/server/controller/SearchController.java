package org.csiro.igsn.nat.server.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.mutable.MutableInt;
import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample.SamplingLocation;
import org.csiro.igsn.nat.server.response.LuceneStats;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.csiro.igsn.nat.server.service.CSIROPanFMPSearchService;
import org.csiro.igsn.nat.server.service.PanFMPSearchService;
import org.csiro.igsn.nat.server.service.SESARPanFMPSearchService;
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
	
	
	Hashtable<String,PanFMPSearchService> services;
	
	@Autowired
	public SearchController(CSIROPanFMPSearchService csiroPanFMPSearchService,SESARPanFMPSearchService sesarPanFMPSearchService ){
		services = new Hashtable<String,PanFMPSearchService>();
		services.put("CSIRO", csiroPanFMPSearchService);
		services.put("SESAR", sesarPanFMPSearchService);
	}
	
	/**
	 * 
	 * @param igsn
	 * @param sampleName
	 * @param materialType
	 * @param sampleCollector
	 * @param curators
	 * @param sampleType
	 * @param samplingFeatureType
	 * @param searchText
	 * @param latitudeBound - array size of 2 for where index 0 == min and index 1 == max
	 * @param longitudeBound - array size of 2 for where index 0 == min and index 1 == max
	 * @param pageNumber
	 * @param pageSize
	 * @param user
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "search.do")
    public ResponseEntity<Object> search(            
            @RequestParam(required = false, value ="igsn") String igsn,
            @RequestParam(required = false, value ="sampleName") String sampleName,
            @RequestParam(required = false, value ="materialType", defaultValue="") String [] materialType,
            @RequestParam(required = false, value ="sampleCollector") String sampleCollector,
            @RequestParam(required = false, value ="curators") String curators,
            @RequestParam(required = false, value ="sampleType", defaultValue="") String [] sampleType,
            @RequestParam(required = false, value ="samplingFeatureType",defaultValue="") String [] samplingFeatureType,
            @RequestParam(required = false, value ="searchText") String searchText,
            @RequestParam(required = false, value ="latitudeBound",defaultValue="") Double [] latitudeBound,
            @RequestParam(required = false, value ="longitudeBound",defaultValue="") Double [] longitudeBound,
            @RequestParam(required = true, value ="repository") String repository,
            @RequestParam(required = false, value ="pageNumber") Integer pageNumber, 
            @RequestParam(required = false, value ="pageSize") Integer pageSize, 
            Principal user,
            HttpServletResponse response) {
		
    	try{
    		MutableInt resultCount=new MutableInt() ;
    		List<SampleSummaryResponse> responses= new ArrayList<SampleSummaryResponse>();
    		
    		services.get(repository).search(igsn, sampleName,materialType,curators, sampleCollector,  sampleType, samplingFeatureType,searchText,latitudeBound,longitudeBound, pageNumber,pageSize,responses);
    	
    		
    		
		    return  new ResponseEntity<Object>(responses,HttpStatus.OK);    		
	    	
    	}catch(Exception e){
    		e.printStackTrace();
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error updating",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }
	

	@RequestMapping(value = "getDetailed.do")
    public ResponseEntity<Object> getDetailed(            
            @RequestParam(required = true, value ="igsn") String igsn,
            @RequestParam(required = true, value ="repository") String repository,
            Principal user,
            HttpServletResponse response) {
    	
    	try{
    		Samples samples = services.get(repository).search(igsn);
    		
		    return  new ResponseEntity<Object>(samples,HttpStatus.OK);    		
	    	
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error updating",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }
	
	@RequestMapping(value = "getStats.do")
    public ResponseEntity<Object> getStats(  
    	   @RequestParam(required = false, value ="statsGroup") String statsGroup,
    	   @RequestParam(required = false, value ="displayName") String displayName,
    	   @RequestParam(required = false, value ="repository") String repository,
            Principal user,
            HttpServletResponse response) {
		
    	try{
    		if(statsGroup==null){
    			ArrayList<LuceneStats> result = new ArrayList<LuceneStats>();
    			if(repository!=null){
    				services.get(repository).getAllStats(result);
    			}
    			return  new ResponseEntity<Object>(result,HttpStatus.OK);
    		}else{
    			//VT: This is targeting a single stat group
    			LuceneStats result = new LuceneStats(statsGroup,displayName,"");
    			if(repository!=null){
    				services.get(repository).populatedStat(result);
    			}else{    				
    				/**
    				 * VT: The only time and only reason why we want to browse through both catalogue is in the home page
    				 * so that we can add up the total number of material Type in both catalogue
    				 */    				
    				services.get("CSIRO").populatedStat(result);
    				services.get("SESAR").populatedStat(result);
    			}
    			return  new ResponseEntity<Object>(result,HttpStatus.OK);
    		}
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error getting stats",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }

}
