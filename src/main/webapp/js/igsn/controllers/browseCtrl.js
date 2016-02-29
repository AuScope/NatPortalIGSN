allControllers.controller('browseCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','DropDownValueService',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,DropDownValueService) {

	
	 
	
	
	
    
    $scope.viewSample = function(igsn){
    	ViewSampleSummaryService.viewSample(igsn);
    }
    
    
    
    
    //Something in the angular route is trigger a refresh on a.href 
    var searchSample = function(page){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		var params ={	
				pageNumber:page,
				pageSize:10
				}
		
		//VT: Actual results
		$http.get('search.do',{
			params:params
		 })     
	     .success(function(data) {
	       $scope.samples = data;       
	       $scope.toggleFilter=false;
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading data:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
	    
	     
	}
	  
	  searchSample(1);
	  
}]);