allControllers.controller('browseCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','$location','$filter','FrontPageSearchParamService','DropDownValueService',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,$location,$filter,FrontPageSearchParamService,DropDownValueService) {

	//VT: For the time being, our front page only show results from CSIRO repo.
    $scope.viewSample = function(igsn,lat,lon,viewSample){
    	ViewSampleSummaryService.viewSample(igsn,lat,lon,viewSample,'CSIRO');
    }
    $scope.repositories = DropDownValueService.getRepositories();
    $scope.selectedRepository='CSIRO';
    
    $scope.filterMaterialType = function(materialIdentifier){
    	if($scope.stats.statsTable[materialIdentifier] && $scope.stats.statsTable[materialIdentifier].count != 0){
    		FrontPageSearchParamService.setMaterialType(materialIdentifier,$scope.selectedRepository);
	    	$location.path("/search");    		
    	}else{
    		 modalService.showModal({}, {    	            	           
		           headerText: "Material Type Selection" ,
		           bodyText: "This selection does not contain any results. Please choose another."
	    	 });  		 
    	}
    }
    
    $scope.$watch('searchText', function(newVal, oldVal){
    	FrontPageSearchParamService.setSearchText(newVal,$scope.selectedRepository);
    });
    
    $scope.$watch('selectedRepository', function(newVal, oldVal){
    	FrontPageSearchParamService.setRepository($scope.selectedRepository);
    });
    
    $scope.changeRepo = function(repository){
    	searchSample(1,repository);
    	getStats(repository);
    	$scope.selectedRepository=repository; 
    }
    
    //Something in the angular route is trigger a refresh on a.href 
    var searchSample = function(page,repository){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		
		//VT: for the time being, the front page only show a summary from the CSIRO repository
		var params ={	
				repository : repository,
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
	  
	  searchSample(1,'CSIRO');
	  
	  $scope.stats ={};
	  
	    
	  var getStats = function(repository){
			//VT: Actual results
	  $http.get('getStats.do',{
		  params:{	
			  repository : repository,
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
	  getStats('CSIRO');  
	    
	   
	  
}]);