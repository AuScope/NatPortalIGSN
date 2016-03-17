allControllers.controller('browseCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','$location','$filter','FrontPageSearchParamService',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,$location,$filter,FrontPageSearchParamService) {

    $scope.viewSample = function(igsn,lat,lon){
    	ViewSampleSummaryService.viewSample(igsn,lat,lon);
    }
    
    $scope.filterMaterialType = function(materialIdentifier){
    	FrontPageSearchParamService.setMaterialType(materialIdentifier);
    	$location.path("/search");
    }
    
    $scope.$watch('searchText', function(newVal, oldVal){
    	FrontPageSearchParamService.setSearchText(newVal);
    });
    
    
    
    //Something in the angular route is trigger a refresh on a.href 
    var searchSample = function(page){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		var params ={	
				pageNumber:page,
				pageSize:20
				}
		
		//VT: Actual results
		$http.get('search.do',{
			params:params
		 })     
	     .success(function(data) {
	       $scope.samples = data;       	       
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: data.header + ": " + status ,
		           bodyText: data.message + ": Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
	    
	     
	}
	  
	  searchSample(1);
	  
	  $scope.stats ={};
	  
	    
	  var getStats = function(){
			//VT: Actual results
	  $http.get('getStats.do',{
		  params:{
			  statsGroup:"materialType",
			  displayName : "Material Type"
		  }
	  })     
	  .success(function(data) {
			 $scope.stats = data; 
			
	  })
	  .error(function(data, status) {    	
  	    modalService.showModal({}, {    	            	           
           headerText: "Error loading stats:" + status ,
		   bodyText: "Please contact cg-admin@csiro.au if this persist"
	 	 });
		       
	    })
	  }
	  getStats();  
	    
	   
	  
}]);