var app = angular.module('app', ['ngRoute','allControllers','ngAnimate','ui.bootstrap','leaflet-directive']);

app.config(['$routeProvider',
            function($routeProvider) {
              $routeProvider.
                when('/', {
              	  redirectTo: '/browse'
                
                }).
                when('/browse', {
                  templateUrl: 'views/browse.html'
               
                }).
                when('/search', {
                    templateUrl: 'views/search.html'
                 
                 }).	                
                otherwise({
                  redirectTo: '/'
                });
              
}]);

app.filter('escape', function() {
	  return window.encodeURIComponent;
});

app.config(function($logProvider){
	  $logProvider.debugEnabled(false);
	});

app.service('modalService', ['$modal',function ($modal) {

    var modalDefaults = {
        backdrop: true,
        keyboard: true,
        modalFade: true,
        templateUrl: 'widget/modal.html'
    };

    var modalOptions = {
        closeButtonText: 'Close',
        actionButtonText: 'OK',
        headerText: 'Proceed?',
        bodyText: 'Perform this action?'
    };

    this.showModal = function (customModalDefaults, customModalOptions) {
        if (!customModalDefaults) customModalDefaults = {};
        customModalDefaults.backdrop = 'static';
        return this.show(customModalDefaults, customModalOptions);
    };

    this.show = function (customModalDefaults, customModalOptions) {
        //Create temp objects to work with since we're in a singleton service
        var tempModalDefaults = {};
        var tempModalOptions = {};

        //Map angular-ui modal custom defaults to modal defaults defined in service
        angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

        //Map modal.html $scope custom properties to defaults defined in service
        angular.extend(tempModalOptions, modalOptions, customModalOptions);

        if (!tempModalDefaults.controller) {
            tempModalDefaults.controller = function ($scope, $modalInstance) {
                $scope.modalOptions = tempModalOptions;
                $scope.modalOptions.ok = function (result) {
                    $modalInstance.close(result);
                };
                $scope.modalOptions.close = function (result) {
                    $modalInstance.dismiss('cancel');
                };
            }
        }

        return $modal.open(tempModalDefaults).result;
    };

}]);



app.service('FrontPageSearchParamService', function() {
	
	 var param = {};	

   return {
   	  
       setMaterialType : function(type){
       	param.materialType=type;
       },
       getMaterialType : function(){
       	return param.materialType;
       },
       setSearchText : function(text){
          	param.searchText=text;
       },
       getSearchText : function(){
      	 return param.searchText;
       },
       reset : function(){
    	   param = {};
       }
   };	    
   
});

app.service('DropDownValueService', ['$q','$http',function($q,$http) {
	

    this.getStates = function() {
            return [{            	 
            	value:{
            		zoom:4,
            		lat:-27.672817,
            		lon:121.62831,
            		bbox:{
            			minlat:-36.87,
            			maxlat:-14,
            			minlon:112,
            			maxlon:129
            		}
            	},
            	display:'Western Australia'            	
            },{            	
            	value:{
            		zoom:5,
            		lat:-19.491411,
            		lon:132.55096,
            		bbox:{
            			minlat:-27,
            			maxlat:-11,
            			minlon:128,
            			maxlon:138.27
            		}
            	},
            	display:'Northern Territory'             	
            },{            	
            	value:{
            		zoom:5,
            		lat:-32.2211,
            		lon:136,
            		bbox:{
            			minlat:-39,
            			maxlat:-25.56,
            			minlon:128,
            			maxlon:142
            		}
            	},
            	display:'South Australia'             	
            },{
            	value:{
            		zoom:6,
            		lat:-37.471308,
            		lon:144.785153,
            		bbox:{
            			minlat:-39.41,
            			maxlat:-34.03,
            			minlon:141,
            			maxlon:150.12
            		}
            	},
            	display:'Victoria'             	
            },{
            	value:{
            		zoom:6,
            		lat:-32.342841,
            		lon:146.986084,
            		bbox:{
            			minlat:-28.80,
            			maxlat:-25.56,
            			minlon:140,
            			maxlon:154
            		}
            	},
            	display:'New South Whales'             	
            },{
            	value:{
            		zoom:5,
            		lat:-20.917574,
            		lon:142.702796,
            		bbox:{
            			minlat:-29.53,
            			maxlat:-10,
            			minlon:137,
            			maxlon:154
            		}
            	},
            	display:'Queensland'             	
            },{
            	value:{
            		zoom:7,
            		lat:-42.252918,
            		lon:146.607056,
            		bbox:{
            			minlat:-44.11,
            			maxlat:-39.38,
            			minlon:143,
            			maxlon:148.8
            		}
            	},
            	display:'Tasmania'             	
            }];
        };
        
        
   
     
        
}]);

app.service('ViewSampleSummaryService',['$modal','$q',function ($modal,$q) {
	     
     this.viewSample = function(igsn,lat,lon){
    	
		 var modalInstance = $modal.open({
	         animation: true,
	         templateUrl: 'views/modals/sampleDetails.html',
	         controller: 'sampleDetailsCtrl',
	         size: 'lg',
	         resolve: {
	        	 params: function () {
	               return {
	              	 igsn :igsn,
	              	 lat : lat,
	              	 lon : lon
	               }
	             }
	           }
	       });
    	 
     }
}])