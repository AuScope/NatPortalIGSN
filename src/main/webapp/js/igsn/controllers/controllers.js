var allControllers = angular.module('allControllers', []);

//SERVICE LEVEL CONTROLLERS
allControllers.controller('sampleDetailsCtrl', function ($scope, $modalInstance, params, modalService,$http,leafletData,$timeout) {
	
	$scope.details={};
	$scope.title = params.igsn;
	
	var getDetails = function(collectionId){
		$http.get('getDetailed.do',{
			params:{	
				igsn: params.igsn		
				}
		 })     
	     .success(function(data) {
	       $scope.details = data;       
	        
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading data:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
	}
	getDetails();
	
	angular.extend($scope, {
	    center: {
	    	lat: -28,
	        lng: 135,
	        zoom: 4
	    },
	    defaults: {
            scrollWheelZoom: false
        },
        tiles:{
            url: "http://{s}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
            options: {
                attribution: 'All maps &copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, map data &copy; <a href="http://www.openstreetmap.org">OpenStreetMap</a> (<a href="http://www.openstreetmap.org/copyright">ODbL</a>'
            }
        }
	});
	
	if(params.lat && params.lon){
		$scope.markers = {
            singleMarker: {
            	lat: params.lat,
    	        lng: params.lon,
                message: params.message,
                focus: true,
                draggable: false
            }
        }
	}
	
	leafletData.getMap("map").then(function (map) {
		
		$timeout(function(){
			map.invalidateSize()
		},500)
	
		 
	})
	
	
	
	
	
	
    $scope.ok = function () {	
		 $modalInstance.close();		 
	 };
	$scope.cancel = function () {
		    $modalInstance.dismiss('cancel');
	}; 
	  
});
