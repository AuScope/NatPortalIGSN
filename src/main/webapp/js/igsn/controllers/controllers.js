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
//			leafletData.getMap('map').then(function(mymap) {
//	        	var wkt = new Wkt.Wkt();        	
//	        	wkt.read(params.wkt);        	
//	        	wkt.toObject();	        	
//	        	var myLayer = L.geoJSON().addTo(mymap);
//	        	myLayer.addData(wkt.toJson());
//	        	if(wkt.type=="point"){
//	        		mymap.panTo(L.latLng(wkt.components[0].y, wkt.components[0].x));
//	        	}else if(wkt.components[0][0]){
//	        		var arr = [];
//	        		for(var i = 0; i<wkt.components[0].length;i++){
//	        			arr.push(L.latLng(wkt.components[0][i].y, wkt.components[0][i].x));
//	        		}
//	        		var polygon = L.polygon(arr);
//	        		mymap.panTo(polygon.getBounds().getCenter());
//	        	}
//	        });    
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
		}
		
		
		$scope.center = {
			lat: params.lat,
	        lng: params.lon,
	        zoom: 4
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
