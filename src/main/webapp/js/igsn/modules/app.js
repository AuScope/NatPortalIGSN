var app = angular.module('app', ['ngRoute','allControllers','ngAnimate','ui.bootstrap']);

app.config(['$routeProvider',
            function($routeProvider) {
              $routeProvider.
                when('/', {
              	  redirectTo: '/browse'
                
                }).
                when('/browse', {
                  templateUrl: 'views/browse.html'
               
                }).
                
                otherwise({
                  redirectTo: '/'
                });
              
}]);

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

app.service('ViewSampleSummaryService',['$modal','$q','modalService',function ($modal,$q,modalService) {
	     
     this.viewSample = function(name){
    	
		 var modalInstance = $modal.open({
	         animation: true,
	         templateUrl: 'views/modals/sampleDetails.html',
	         controller: 'sampleDetailsCtrl',
	         size: 'lg',
	         resolve: {
	        	 params: function () {
	               return {
	              	 name :name	              	
	               }
	             }
	           }
	       });
    	 
     }
}])