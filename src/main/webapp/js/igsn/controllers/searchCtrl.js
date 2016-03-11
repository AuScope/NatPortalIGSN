allControllers.controller('searchCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','DropDownValueService','leafletData','$routeParams',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,DropDownValueService,leafletData,$routeParams) {

	$scope.totalItem = 0;
	$scope.currentPages = 1;
	
	angular.extend($scope, {
	    center: {
	    	lat: -28,
	        lng: 135,
	        zoom: 4
	    },
	    tiles:{
            url: "http://{s}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
            options: {
                attribution: 'All maps &copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, map data &copy; <a href="http://www.openstreetmap.org">OpenStreetMap</a> (<a href="http://www.openstreetmap.org/copyright">ODbL</a>'
            }
        },
	    defaults: {
            scrollWheelZoom: false
        }
	});
	
	
	
	
    
    $scope.viewSample = function(name){
    	ViewSampleSummaryService.viewSample(name);
    }
    //load stats
    $scope.stats ={};
    $scope.checkbox=[];
    
    var getStats = function(){
		
		
		//VT: Actual results
		$http.get('getStats.do')     
	     .success(function(data) {
	    	 $scope.stats = data; 
	    	 $scope.checkbox=setupCheckbox(data);
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading stats:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
   
	}
    
    getStats();
    //end load stats
    
    $scope.pageChanged = function() {
    	searchSample($scope.currentPages);
	  };
    
    var searchSample = function(page){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		var params ={	
				pageNumber:page,
				pageSize:10
		}
		
		if($routeParams.materialIdentifier){
			params.materialType=$routeParams.materialIdentifier;
		}
		
		//VT: Actual results
		$http.get('search.do',{
			params:params
		 })     
	     .success(function(data) {
	       $scope.samples = data;	       
	   	   $scope.totalItem = data[0].searchResultCount;
	   	   $scope.markers={};
	   	   data.forEach(function(current,index,arr){
	   		   

	   		   this.markers[index+'wtf'] =   {
	            	lat: current.latitude,
	    	        lng: current.longitude,		    	     
	                icon:{
	                	iconUrl:'http://maps.google.com/mapfiles/kml/paddle/'+ (index+1) +'.png'
	                }
	            }
	   	   },$scope);
		
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading data:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
	     
	}
    
    searchSample($scope.currentPages);
    
    var setupCheckbox=function(stats){
    	var results = [];
    	for(var i=0;i< stats.length;i++){
    		results[stats[i].statsGroup] = angular.copy(stats[i].statsTable);
    		for(key in results[stats[i].statsGroup]){
    			results[stats[i].statsGroup][key]=true;
    		}
    	}
    	return results;
    }
	  
	  
	  
	  
	  
	  
	  
}]);