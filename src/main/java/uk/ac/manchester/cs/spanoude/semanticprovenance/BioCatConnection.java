package uk.ac.manchester.cs.spanoude.semanticprovenance;
import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.xmlbeans.*;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.biocatalogue.x2009.xml.rest.*;
import org.biocatalogue.x2009.xml.rest.SoapOperation.*;
import org.biocatalogue.x2009.xml.rest.SoapService.*;


public class BioCatConnection {

	public BioCatConnection(List<ResultBindings> resultsList, Vector<String> exampleWorkflowList) throws IOException{
		
		
	    
		String serviceURL="";
		String service="";
		String port="";
		String value="";
		
		for(ResultBindings result:resultsList){
		String urlToConnectTo = "http://sandbox.biocatalogue.org/lookup?wsdl_location=";
		serviceURL=result.getServiceURL();
		serviceURL=serviceURL.substring(1, serviceURL.indexOf(">"));
		service=result.getService();
		service=service.substring(1, service.lastIndexOf("\""));
		port=result.getPort();
		value=result.getValue();
		
		//---TODO
		//Check if service exists in biocatalogue -lookup
		//--TODO
      	
		//Step 1. get Soap Services and operations
		SoapService servicesData;  
		try {
			BioCatConnectAndGET testConnect=new BioCatConnectAndGET(urlToConnectTo+serviceURL);
			servicesData =  SoapServiceDocument.Factory.parse(testConnect.getServerResponse()).getSoapService();
			//System.out.println(servicesData.getName());
			Operations operations=servicesData.getOperations();
			SoapOperation[] opArray=operations.getSoapOperationArray();
		   // System.out.println(servicesData.getDescription());
		
		   //Step 2. get Service Inputs and Outputs from Soap Operations
		   for (SoapOperation entry:opArray ){
			  
			   if(entry.getName().equals(service)){    //eg idSearch
			   String actualURL=entry.getHref();     //Url of service
			   urlToConnectTo = "http://sandbox.biocatalogue.org/lookup?wsdl_location="+serviceURL+"&operation_name="+entry.getName(); //
			   //urlToConnectTo=urlToConnectTo+"&operation_name="+entry.getName();
			   testConnect=new BioCatConnectAndGET(urlToConnectTo);
			    SoapOperation soapOperation =SoapOperationDocument.Factory.parse(testConnect.getServerResponse()).getSoapOperation();
			    Inputs opInputs= soapOperation.getInputs();
				Outputs opOutputs=soapOperation.getOutputs();
				SoapInput[] inputArray=opInputs.getSoapInputArray();
				SoapOutput[] outputArray=opOutputs.getSoapOutputArray();	
								
		//	  System.out.println("Service name: "+entry.getName());
		// Add annotations for inputs			  
			  for(int i=0;i<inputArray.length;i++){
		    	  if(inputArray[i].getName().equals(port)){
				//System.out.println("Input"+i+" for service:"+inputArray[i].getName());
		    	String inputURL=inputArray[i].getHref();
		    	//System.out.println(inputURL);
		    	postAnnotations(inputURL,"example_data",value);
		    	  }
		    	}
			 
		// Add annotations for outputs	  
			  for(int i=0;i<outputArray.length;i++){
				  if(outputArray[i].getName().equals(port)){
				  System.out.println("Output"+i+" for service:"+outputArray[i].getName());
				  String outputURL=outputArray[i].getHref();
			      value=result.getValue();
				  postAnnotations(outputURL,"example_data",value);
				  }
			  }
		
	  // Add example workflow annotation
		if(exampleWorkflowList.size()>1){
			System.out.println("service url:"+actualURL);
			System.out.println(exampleWorkflowList.elementAt(0));
			if(exampleWorkflowList.elementAt(0).contains("\"")){
				exampleWorkflowList.set(1, parseString(exampleWorkflowList.elementAt(1),"\""));
			}
		 	postAnnotations(actualURL,"example_workflow",exampleWorkflowList.elementAt(1));
			
		}
			  
			break;
			   }
		}
		    
		} catch (XmlException e) {
			
			e.printStackTrace();
		}
		
		
		}
		
	}
	
	private static void postAnnotations(String toService,String dataType,String data) throws IOException{
		String JSONtestString="{"
			  +"\"bulk_annotations\": [ {"
			  + " \"resource\": \""+toService+"\","
			  +"  \"annotations\": {"
			  +"\""+dataType+"\": \""+data+"\"" // +"\"description\": \"Manoli's test description. Please ignore\""
			  + " }"
			  +"}]}";
				
	     BioCatConnectAndPOST testConnectAndPost=new BioCatConnectAndPOST("http://sandbox.biocatalogue.org/annotations/bulk_create",JSONtestString);
		}
	
	protected static String parseString(String response, String delimiter){
		 response=response.trim();
		 int startchar=response.indexOf(delimiter);
		 int endchar=response.indexOf(delimiter, startchar+1);
		 return response.substring(startchar+1, endchar);
	 }
	
	
}
