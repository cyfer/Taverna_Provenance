package uk.ac.manchester.cs.spanoude.semanticprovenance;

public class ResultBindings {
String port;
String value;
String service;
String serviceURL;
	
  public ResultBindings(String port,String value,String service, String serviceURL ){
	  this.port=port;
	  this.value=value;
	  this.service=service;
	  this.serviceURL=serviceURL;
  }

public String getService() {
	return service;
}

public void setService(String service) {
	this.service = service;
}

public String getServiceURL() {
	return serviceURL;
}

public void setServiceURL(String serviceURL) {
	this.serviceURL = serviceURL;
}

public String getPort() {
	return port;
}

public void setPort(String port) {
	this.port = port;
}

public String getValue() {
	return value;
}

public void setValue(String value) {
	this.value = value;
}
  
  
  
}
