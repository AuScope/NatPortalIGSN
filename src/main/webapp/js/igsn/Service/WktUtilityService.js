app.service('WktUtilityService', function() {
	
	this.flattenTo2D = function(wkt3D){
		if(wkt3D.toLowerCase().startsWith("pointz")){  
			wkt3D = wkt3D.toLowerCase().replace("pointz","point");
			wkt3D = wkt3D.substring(0,wkt3D.lastIndexOf(" "));
			wkt3D += ")";
			return wkt3D
    	}else{
    		return wkt3D;
    	}
	}
    
});