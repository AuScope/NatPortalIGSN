allControllers.controller('searchCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','leafletData','FrontPageSearchParamService','DropDownValueService','$sce', 
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,leafletData,FrontPageSearchParamService,DropDownValueService,$sce) { 

	$scope.totalItem = 0;
	$scope.currentPages = 1;
	$scope.mapCoord={};
	$scope.browse = true;
	$scope.showMapOverlayList= true;
	$scope.showMapOverlayListFunc = function(show){
		$scope.showMapOverlayList = show;
	} 
	
	var parseStatsToHtmlList = function(objectList){
		  var result = "<ul class='small' style='padding-left:10px'>";
		  for(var index in objectList){
			  result += "<li><a target='_blank' href='"+objectList[index].value+"'>" + index + "</a></li>"
		  }
		  return result + "</ul>";
	  };
	
	var getStatsTableFromStats = function(arr,statgroup){
		for(var index in arr){
			 if(arr[index].statsGroup == statgroup){
				 return arr[index].statsTable;
			 }
		  }
		return {};
	}
	
	$scope.htmlPopover = {
			material : $sce.trustAsHtml("<p>Type of material represented by the sample.</p>"),
			resourceType : $sce.trustAsHtml("<p>Type of material represented by the sample.</p>"),
			searchText : $sce.trustAsHtml('<p>Perform text search across title, identifier, alternateidentifier, contributor, supplemental metadata, description, related resource and collector</p>'),
	}
	
	$scope.states = DropDownValueService.getStates();
	
	$scope.bboxSearch={};
	
	$scope.repositories = DropDownValueService.getRepositories();
	
	
	angular.extend($scope, {
	    center: {
	    	lat: -28,
	        lng: 135,
	        zoom: 3
	    },
	    tiles:{
	    	url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
            options: {
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }
        }, 
        events: {
            map: {
                enable: ['zoomstart', 'drag', 'click', 'mousemove','resize'],
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
                },
                samplecluster: {
                    name: 'samplecluster',
                    type: 'markercluster',                   
                    visible: true,
                    layerParams: {
                        showOnSelector: false
                    },
                    layerOptions:{
                        maxClusterRadius:30
                      },
                }
            }
        },
	    defaults: {
            scrollWheelZoom: false
        }
	});
	
	$scope.panToRegion=function(lat,lon,zoom,bbox){
		$scope.center = {
				lat: lat,
		        lng: lon,
		        zoom: zoom	
		}
		$scope.bboxSearch=bbox;
		
	}
	
	$scope.zoomToMarker = function (lat,lon, operation){
		if(operation=='+'){
			$scope.center = {
					lat: lat,
			        lng: lon,
			        zoom: $scope.center.zoom + 2	
			}
		}else if (operation=='-'){
			$scope.center = {
					lat: lat,
			        lng: lon,
			        zoom: $scope.center.zoom - 2	
			}
		}else{
			$scope.center = {
					lat: lat,
			        lng: lon,
			        zoom: $scope.center.zoom	
			}
		}
	}
	
	$scope.clearRegion = function(){
		
		$scope.center = {
		    	lat: -28,
		        lng: 135,
		        zoom: 3
		    };
		
		$scope.bboxSearch={};
		$scope.regionSelected="";
		
	}
		
	
	var bboxDrawer={};
	
	var me = this;
	
	leafletData.getMap('mapSearch').then(function(map) {
		bboxDrawer = new L.Draw.Rectangle(map);		
        leafletData.getLayers().then(function(baselayers) {
           var drawnItems = baselayers.overlays.draw;
           map.on('draw:created', function (e) {
             var layer = e.layer;
             drawnItems.addLayer(layer);
             $scope.bboxSearch=extractBbox(layer.toGeoJSON().geometry.coordinates[0])
           });
        });
    });
	
	$scope.$on('leafletDirectiveMap.mapSearch.mousemove', function(event,args){
        $scope.mapCoord.lat = args.leafletEvent.latlng.lat;
        $scope.mapCoord.lon = args.leafletEvent.latlng.lng;
       
    });	
	
	$scope.$on('leafletDirectiveMarker.mapSearch.click', function (e, args) {
		ViewSampleSummaryService.viewSample(args.model.igsn,args.model.lat,args.model.lng,args.model.wkt,args.model.message);
	});
	
	$scope.drawBoundingBox = function(){
		$scope.clearBoundingBox();
		bboxDrawer.enable();
	}
	
	$scope.clearBoundingBox = function(){
		bboxDrawer.disable();
		var drawnItems = this.layers.overlays.draw;
		$scope.bboxSearch={};
		$scope.regionSelected="";
		leafletData.getMap('mapSearch').then(function(map) {
			leafletData.getLayers('mapSearch').then(function(baselayers) {
	           var drawnItems = baselayers.overlays.draw;	          	            
	           for(var i=0; i < drawnItems.getLayers().length;i++){
	        	   drawnItems.removeLayer(drawnItems.getLayers()[i]);
	           }
	            
	        });
	    }); 
	}
	
	$scope.panToFirstMarker = function(){
		for(var key in $scope.markers){
   		   if($scope.browse){
   			   $scope.zoomToMarker($scope.markers[key].lat,$scope.markers[key].lng);
   		   }else{   			   			  
		   		$scope.center = {
					lat: $scope.markers[key].lat,
			        lng: $scope.markers[key].lng+25,
			        zoom: 3	
				}
   		   }
	   		leafletData.getMap('mapSearch').then(function(map) {
	   			setTimeout(function(){
	   		      map.invalidateSize();
	   		    }, 200);
		    });
	   		break;
   	   }
	}
	
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
	
    
    $scope.viewSample = function(name,lat,lon,message){
    	ViewSampleSummaryService.viewSample(name,lat,lon,message);
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
	    	 $scope.htmlPopover = {
				material : $sce.trustAsHtml("<p>Type of material represented by the sample. Click on the links below for more information</p>" + parseStatsToHtmlList(getStatsTableFromStats(data,'material'))),
				resourceType : $sce.trustAsHtml("<p>Type of material represented by the sample. Click on the links below for more information</p>" +parseStatsToHtmlList(getStatsTableFromStats(data,'resourceType'))),
				searchText : $sce.trustAsHtml('<p>Perform text search across title, identifier, alternateidentifier, contributor, supplemental metadata, description, related resource and collector</p>'),
	    	 }
	     })
	     .error(function(data, status) {    	
	    	 modalService.showModal({}, {    	            	           
		           headerText: "Error loading stats:" + status ,
		           bodyText: "Please contact cg-admin@csiro.au if this persist"
	    	 });
	       
	     })
   
	}
    
    
    $scope.setStats();
    
    var resetMapLayer = function(){
    	$scope.markers={};
    	$scope.geojson = {
    			data:[]	
    		}
    }
    
    $scope.reset = function(){
    	$scope.stats =[];
        $scope.checkbox=[];
        $scope.combo=[];
        $scope.text=[];
    	FrontPageSearchParamService.reset();
    	$scope.samples=[];
    	resetMapLayer();
    	$scope.totalItem=0;
    	$scope.browse = true;
    	$scope.setStats();
    	$scope.clearRegion();
    	$scope.clearBoundingBox();
    	
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
    		if($scope.combo[statsgroup] && $scope.combo[statsgroup] != ""){
    			params[statsgroup] = $scope.combo[statsgroup];
    		}
    	}
    	
    	for(var statsgroup in $scope.text){    		
    		params[statsgroup] = $scope.text[statsgroup];
    	}
    	    	    
 		
 		if($scope.bboxSearch.minlat && $scope.bboxSearch.maxlat && $scope.bboxSearch.minlon && $scope.bboxSearch.maxlon){
 			params.latitudeBound=[$scope.bboxSearch.minlat,$scope.bboxSearch.maxlat];
 			params.longitudeBound=[$scope.bboxSearch.minlon,$scope.bboxSearch.maxlon];
 		}
         
 		
    	
    	return params;
    }
    
    $scope.pageChanged = function() {
    	if($scope.browse==true){
    		$scope.searchSample($scope.currentPages,true);
    	}else{
    		$scope.searchSample($scope.currentPages,false,true);
    	}
    	
	  };
    
    $scope.searchSample = function(page,noParam,paging){
		$scope.currentPages = page;//VT page is reset to 1 on new search
		$scope.samples=[];
		var params={};
		if(noParam){
			params ={	
				pageNumber:page,
				pageSize:500				
			}
			params.latitudeBound=[-90,90];
 			params.longitudeBound=[-180,180];
			$scope.browse = true;
		}else{
			params = compileParam(page);
			resetMapLayer();
			$scope.browse = false;
			if(!paging){//VT: if we are simply paging through the records, honor the user's request to show or hide the overlay
				$scope.showMapOverlayListFunc(true);
			}
		}		
		
		//VT: Actual results
		$http.get('search.do',{
			params:params
		 })     
	     .success(function(data) {
	       $scope.samples = data;	       
	   	   $scope.totalItem = data.length > 0?data[0].searchResultCount:0;
	   	   resetMapLayer();
	   	   
	   	   if($scope.totalItem==0){
   		     modalService.showModal({}, {
		           headerText: "No Record Found" ,
		           bodyText: "Please change your search parameter"
	    	 });
	   	   }	  
	   	
	   	   if($scope.browse){
	   		 data.forEach(function(current,index,arr){		   		   
		   		   if(current.latitude && current.longitude){
			   		   this.markers[index+'_markers'] =   {
			            	lat: current.latitude,
			    	        lng: current.longitude,	
			    	        igsn: current.igsn,
			    	        message: current.message,			    	        
			    	        layer: 'samplecluster'			               
			            }
			   		 $scope.panToFirstMarker();
		   		   }else if(current.wkt){
			   			var wkt = new Wkt.Wkt();        	
			        	wkt.read(current.wkt);        	
			        	wkt.toObject();			        	
		   			   	$scope.geojson.data.push(wkt.toJson());
		   			   	panToWktCenter(wkt);
		   		   }
		   	   },$scope);
	   	   }else{
	   		 data.forEach(function(current,index,arr){		   		   
		   		   if(current.latitude && current.longitude){
			   		   this.markers[index+'_markers'] =   {
			   				lat: current.latitude,
			    	        lng: current.longitude,	
			    	        igsn: current.igsn,
			    	        message: current.message,			    	      
			    	        layer: 'samplecluster',	
			                icon:{
			                	iconUrl:'http://maps.google.com/mapfiles/kml/paddle/'+ (index+1) +'.png',
			                	iconSize:     [35, 35], // size of the icon
			                }
			            }
			   		 $scope.panToFirstMarker();
		   		   }else if(current.wkt){
			   			var wkt = new Wkt.Wkt();        	
			        	wkt.read(current.wkt);        	
			        	wkt.toObject();			        	
		   			   	$scope.geojson.data.push(wkt.toJson());
		   			   	panToWktCenter(wkt);
		   		   }
		   	   },$scope); 
	   	   }
	   	   
	   	  
	   	   
	   	
	   	   
	   	  
	   	   
		
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
				if(stats[i].statsGroup!='material'){
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
			
			$scope.searchSample($scope.currentPages);
		}else if(FrontPageSearchParamService.getSearchText()){			
			$scope.text.searchText = FrontPageSearchParamService.getSearchText();
    		FrontPageSearchParamService.reset();
    		$scope.searchSample($scope.currentPages);
		}else{
			for (var i = 0; i < stats.length; i++) {	
				$scope.checkbox[stats[i].statsGroup]={};
				$scope.checkbox[stats[i].statsGroup].filter=true;	
				$scope.checkboxClass[stats[i].statsGroup]="disable";
						
			}
			$scope.searchSample($scope.currentPages,true);
		}
		
		
		
		 

	}
	
	
	
	var extractBbox = function(coordinates){
		var bbox={};
		bbox.minlon=Math.min(coordinates[0][0],Math.min(coordinates[1][0],Math.min(coordinates[2][0],coordinates[3][0])));
		bbox.maxlon=Math.max(coordinates[0][0],Math.max(coordinates[1][0],Math.max(coordinates[2][0],coordinates[3][0])));
		bbox.minlat=Math.min(coordinates[0][1],Math.min(coordinates[1][1],Math.min(coordinates[2][1],coordinates[3][1])));
		bbox.maxlat=Math.max(coordinates[0][1],Math.max(coordinates[1][1],Math.max(coordinates[2][1],coordinates[3][1])));
		return bbox;
	}
	
	  
	  
	  
	  
	  
	  
}]);