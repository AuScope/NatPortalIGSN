allControllers.controller('searchCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','DropDownValueService','leafletData',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,DropDownValueService,leafletData) {

	
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
        },
	    markers: {
            osloMarker: {
            	lat: -25.2726333,
    	        lng: 114.9839476,
                message: "I want to travel here!",
                focus: true,
                draggable: false
            }
        }
	});
	
	
	
	
    
    $scope.viewSample = function(name){
    	ViewSampleSummaryService.viewSample(name);
    }
    
    
    
    
    
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
	  
	  //searchSample(1);
	  
}]);