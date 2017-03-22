var allControllers = angular.module('allControllers', []);

//SERVICE LEVEL CONTROLLERS
allControllers.controller('sampleDetailsCtrl', function ($scope, $uibModalInstance, params, modalService,$http,leafletData,$timeout) {
	
	$scope.details={};
	$scope.title = params.igsn;
	
	var getDetails = function(igsn){
		$http.get('getDetailed.do',{
			params:{				
				igsn: igsn		
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
	getDetails(params.igsn);
	
	var panToWktCenter = function(wkt){
		if(wkt.type.toLowerCase() !="point"){
			var arr = [];
	 		for(var i = 0; i<wkt.components[0].length;i++){
	 			arr.push(L.latLng(wkt.components[0][i].y, wkt.components[0][i].x));
	 		}
	 		var polygon = L.polygon(arr);
	 		$scope.center = {
					lat: polygon.getBounds().getCenter().lat,
			        lng: polygon.getBounds().getCenter().lng,
			        zoom: 3	
				}
	 		
		}
	}
	
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
	    	url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
            options: {
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }
        }
	});
	
	if((params.lat && params.lon) || params.wkt){
		if(params.wkt){	
			var wkt = new Wkt.Wkt();        	
        	wkt.read(params.wkt);        	
        	wkt.toObject();	
        	 $scope.geojson = {
        		data: wkt.toJson() 
             };
        	 panToWktCenter(wkt);
		}else{
			$scope.markers = {
	            singleMarker: {
	            	lat: params.lat,
	    	        lng: params.lon,
	                message: params.message,
	                focus: true,
	                draggable: false
	            }
		    };
			$scope.center = {
				lat: params.lat,
		        lng: params.lon,
		        zoom: 4
			}
		}
		
		
		
	}
	
	leafletData.getMap("map").then(function (map) {
		
		$timeout(function(){
			map.invalidateSize()
		},500)
	
		 
	})
	
	
	
	
	
	
    $scope.ok = function () {	
		 $uibModalInstance.close();		 
	 };
	$scope.cancel = function () {
		    $uibModalInstance.dismiss('cancel');
	}; 
	  
});
