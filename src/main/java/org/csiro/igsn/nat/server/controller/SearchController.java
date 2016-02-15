package org.csiro.igsn.nat.server.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;


import org.csiro.igsn.bindings.allocation2_0.Samples;
import org.csiro.igsn.bindings.allocation2_0.Samples.Sample;
import org.csiro.igsn.nat.server.response.SampleSummaryResponse;
import org.csiro.igsn.nat.server.service.PanFMPSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(required = false, value ="sampleType") String sampleType,
            @RequestParam(required = false, value ="pageNumber") Integer pageNumber, 
            @RequestParam(required = false, value ="pageSize") Integer pageSize, 
            Principal user,
            HttpServletResponse response) {
    	
    	try{
    		List<Samples> samples = panFMPSearchService.search(igsn,sampleType,pageNumber,pageSize);
    		List<SampleSummaryResponse> responses= new ArrayList<SampleSummaryResponse>();
    		for(Samples s: samples){
    			SampleSummaryResponse summaryResponse= new SampleSummaryResponse();
    			Sample sample=s.getSample().get(0);
    			summaryResponse.setIgsn(sample.getSampleNumber().getValue());
    			summaryResponse.setName(sample.getSampleName());
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
            Principal user,
            HttpServletResponse response) {
		
    	try{
		    return  new ResponseEntity<Object>(panFMPSearchService.getAllStats(),HttpStatus.OK);    		
    	}catch(Exception e){
    		return new  ResponseEntity<Object>(new ExceptionWrapper("Error getting stats",e.getMessage()),HttpStatus.BAD_REQUEST);
    	}
    }

}
