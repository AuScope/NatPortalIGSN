allControllers.controller('searchCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','leafletData','FrontPageSearchParamService','leafletData',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,leafletData,FrontPageSearchParamService,leafletData) {

	$scope.totalItem = 0;
	$scope.currentPages = 1;
	
	
	
	angular.extend($scope, {
	    center: {
	    	lat: -28,
	        lng: 135,
	        zoom: 3
	    },
	    tiles:{
            url: "http://{s}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
            options: {
                attribution: 'All maps &copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, map data &copy; <a href="http://www.openstreetmap.org">OpenStreetMap</a> (<a href="http://www.openstreetmap.org/copyright">ODbL</a>'
            }
        }, 
        events: {
            map: {
                enable: ['zoomstart', 'drag', 'click', 'mousemove'],
                logic: 'emit'
            }
        },
        layers: {
           
            overlays: {
                draw: {
                    name: 'draw',
                    type: 'group',
                    visible: true,
                    layerParams: {
                        showOnSelector: false
                    }
                }
            }
        },
	    defaults: {
            scrollWheelZoom: false
        }
	});
	
	var bboxDrawer={};
	
	var me = this;
	
	leafletData.getMap('mapSearch').then(function(map) {
		bboxDrawer = new L.Draw.Rectangle(map);		
        leafletData.getLayers().then(function(baselayers) {
           var drawnItems = baselayers.overlays.draw;
           map.on('draw:created', function (e) {
             var layer = e.layer;
             drawnItems.addLayer(layer);
             console.log(JSON.stringify(layer.toGeoJSON()));
           });
        });
    });
	
	$scope.$on('leafletDirectiveMap.mousemove', function(event,args){
        $scope.map.lat = args.leafletEvent.latlng.lat;
        $scope.map.lon = args.leafletEvent.latlng.lng;
       
    });	
	
	$scope.drawBoundingBox = function(){
		bboxDrawer.enable();
	}
	
	$scope.clearBoundingBox = function(){
		bboxDrawer.disable();
		var drawnItems = this.layers.overlays.draw;
		
		leafletData.getMap('mapSearch').then(function(map) {
			leafletData.getLayers('mapSearch').then(function(baselayers) {
	           var drawnItems = baselayers.overlays.draw;	          	            
	           for(var i=0; i < drawnItems.getLayers().length;i++){
	        	   drawnItems.removeLayer(drawnItems.getLayers()[i]);
	           }
	            
	        });
	    }); 
	}
	
	
	$scope.filterSwitch=function(statsGroup,filter){
		if(filter==false){			
			$scope.checkboxClass[statsGroup] ="";			
			$scope.checkbox[statsGroup].filter=false;
		}
		
		if(filter){	
			if($scope.checkbox[statsGroup].filter==false){
				$scope.checkboxClass[statsGroup] ="";			
				$scope.checkbox[statsGroup].filter=false;
				return;
			}
			$scope.checkboxClass[statsGroup] ="disable";
			for(var key in $scope.checkbox[statsGroup]){
				$scope.checkbox[statsGroup][key]=false;
			}	
			$scope.checkbox[statsGroup].filter=true;
			
		}
	}
	
    
    $scope.viewSample = function(name,lat,lon){
    	ViewSampleSummaryService.viewSample(name,lat,lon);
    }
    //load stats
    $scope.stats =[];
    $scope.checkbox=[];
    $scope.combo=[];
    $scope.text=[];
    $scope.checkboxClass=[];
    
    $scope.setStats = function(){
		
		
		//VT: Actual results
		$http.get('getStats.do')     
	     .success(function(data) {
	    	 $scope.stats = data; 
	    	 setupControls(data);
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading stats:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
   
	}
    
    //VT: this is the code entry point
    $scope.setStats();
    
    $scope.reset = function(){
    	$scope.stats =[];
        $scope.checkbox=[];
        $scope.combo=[];
        $scope.text=[];
    	FrontPageSearchParamService.reset();
    	$scope.setStats();
    }
    
    
    var compileParam = function(page){
    	var params ={	
				pageNumber:page,
				pageSize:10
		}
    	
    	for(var statsgroup in $scope.checkbox){    		
    		if(!$scope.checkbox[statsgroup].filter){
    			var filters=[];
    			var statsTable = {};
    			$scope.stats.find(function(element,index,array){
    				if(element.statsGroup==statsgroup){
    					statsTable = element.statsTable;
    					return;
    				}
    			},this);
	    		for(var stats in $scope.checkbox[statsgroup]){
	    			if($scope.checkbox[statsgroup][stats]==true){
	    				filters.push(statsTable[stats].value);
	    			}
	    		}
	    		params[statsgroup] = filters
    		}
    	}
    	
    	for(var statsgroup in $scope.combo){    		
    		params[statsgroup] = $scope.combo[statsgroup];
    	}
    	
    	for(var statsgroup in $scope.text){    		
    		params[statsgroup] = $scope.text[statsgroup];
    	}
    	
    	
    	
    	
    	return params;
    }
    
    $scope.pageChanged = function() {
    	$scope.searchSample($scope.currentPages);
	  };
    
    $scope.searchSample = function(page){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		var params = compileParam(page);
		
		
		
		//VT: Actual results
		$http.get('search.do',{
			params:params
		 })     
	     .success(function(data) {
	       $scope.samples = data;	       
	   	   $scope.totalItem = data.length > 0?data[0].searchResultCount:0;
	   	   $scope.markers={};
	   	   
	   	   if($scope.totalItem==0){
   		     modalService.showModal({}, {
		           headerText: "No Record Found" ,
		           bodyText: "Please change your search parameter"
	    	 });
	   	   }
	   	   
	   	   data.forEach(function(current,index,arr){
	   		   
	   		   if(current.latitude && current.longitude){
		   		   this.markers[index+'_markers'] =   {
		            	lat: current.latitude,
		    	        lng: current.longitude,		    	     
		                icon:{
		                	iconUrl:'http://maps.google.com/mapfiles/kml/paddle/'+ (index+1) +'.png',
		                	iconSize:     [35, 35], // size of the icon
		                }
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
    
    

	var setupControls = function(stats) {	
		
		if(FrontPageSearchParamService.getMaterialType()){
			for (var i = 0; i < stats.length; i++) {
				if(stats[i].statsGroup!='materialType'){
					$scope.checkbox[stats[i].statsGroup]={};
					$scope.checkbox[stats[i].statsGroup].filter=true;	
					$scope.checkboxClass[stats[i].statsGroup]="disable";
				}else{
					$scope.checkbox[stats[i].statsGroup]={};
					$scope.checkbox[stats[i].statsGroup].filter=false;
					$scope.checkbox[stats[i].statsGroup][FrontPageSearchParamService.getMaterialType()]=true;
					$scope.checkboxClass[stats[i].statsGroup]="";
					FrontPageSearchParamService.reset();
				}
						
			}
		}else if(FrontPageSearchParamService.getSearchText()){
			
			$scope.text.searchText = FrontPageSearchParamService.getSearchText();
    		FrontPageSearchParamService.reset();
	    	
		}else{
			for (var i = 0; i < stats.length; i++) {	
				$scope.checkbox[stats[i].statsGroup]={};
				$scope.checkbox[stats[i].statsGroup].filter=true;	
				$scope.checkboxClass[stats[i].statsGroup]="disable";
						
			}
		}
		
		
		
		 $scope.searchSample($scope.currentPages);

	}
	
	
	
	
	
	  
	  
	  
	  
	  
	  
}]);