allControllers.controller('searchCtrl', ['$scope','$rootScope','$http','ViewSampleSummaryService','modalService','DropDownValueService','leafletData','$routeParams',
                                  function ($scope,$rootScope,$http,ViewSampleSummaryService,modalService,DropDownValueService,leafletData,$routeParams) {

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
	    defaults: {
            scrollWheelZoom: false
        }
	});
	
	
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
    $scope.checkboxClass=[];
    
    var getStats = function(){
		
		
		//VT: Actual results
		$http.get('getStats.do')     
	     .success(function(data) {
	    	 $scope.stats = data; 
	    	 setupCheckbox(data);
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
	   	   $scope.totalItem = data[0]?data[0].searchResultCount:0;
	   	   $scope.markers={};
	   	   
	   	   
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
    

	var setupCheckbox = function(stats) {	
		
		if($routeParams.materialIdentifier){
			for (var i = 0; i < stats.length; i++) {
				if(stats[i].statsGroup!='materialType'){
					$scope.checkbox[stats[i].statsGroup]={};
					$scope.checkbox[stats[i].statsGroup].filter=true;	
					$scope.checkboxClass[stats[i].statsGroup]="disable";
				}else{
					$scope.checkbox[stats[i].statsGroup]={};
					$scope.checkbox[stats[i].statsGroup].filter=false;
					$scope.checkbox[stats[i].statsGroup][$routeParams.materialIdentifier]=true;
					$scope.checkboxClass[stats[i].statsGroup]="";
					
				}
						
			}
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