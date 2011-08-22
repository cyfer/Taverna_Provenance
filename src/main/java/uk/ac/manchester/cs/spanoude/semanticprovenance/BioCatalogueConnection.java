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

import sun.misc.BASE64Encoder;


public class BioCatalogueConnection extends Connection {

	public BioCatalogueConnection(List<ResultBindings> resultsList, Vector<String> exampleWorkflowList,String connectionCredentials, String websiteToAnnotate) throws IOException{
		
		Connection(resultsList,exampleWorkflowList,connectionCredentials,websiteToAnnotate);
	    
		String serviceURL="";
		String service="";
		String port="";
		String value="";
		System.out.println("New connection to Biocatalogue established");
		
		for(ResultBindings result:resultsList){
			
		String urlToConnectTo = websiteToAnnotate+"/lookup?wsdl_location=";
		serviceURL=result.getServiceURL();
		serviceURL=serviceURL.substring(1, serviceURL.indexOf(">"));
		System.out.println(urlToConnectTo+serviceURL);
		URLEncoder enc=null;
		serviceURL = enc.encode(serviceURL, "UTF-8"); //  encode service URL in case it contains ?
		//System.out.println(serviceURL);
		service=result.getService();
		service=service.substring(1, service.lastIndexOf("\""));
		port=result.getPort();
		value=result.getValue();
		
				
		
      	
		//Step 1. get Soap Services and operations
		SoapService servicesData;  
		try {
			
			BioCatConnectAndGET testConnect=new BioCatConnectAndGET(urlToConnectTo+serviceURL);
		if(testConnect.getResponseCode()==HttpURLConnection.HTTP_OK) {
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
								
			  System.out.println("Service name: "+entry.getName());
		// Add annotations for inputs			  
			  for(int i=0;i<inputArray.length;i++){
		    	  if(inputArray[i].getName().equals(port)){
				System.out.println("Input"+i+" for service:"+inputArray[i].getName());
		    	String inputURL=inputArray[i].getHref();
		    	//System.out.println(inputURL);
		    	postAnnotations(inputURL,"example_data",value,connectionCredentials);
		    	  }
		    	}
			 
		// Add annotations for outputs	  
			  for(int i=0;i<outputArray.length;i++){
				  if(outputArray[i].getName().equals(port)){
				  System.out.println("Output"+i+" for service:"+outputArray[i].getName());
				  String outputURL=outputArray[i].getHref();
			      value=result.getValue();
				  postAnnotations(outputURL,"example_data",value,connectionCredentials);
				  }
			  }
		
	  		  
	  // Add example workflow annotation
		if(exampleWorkflowList.size()>1){
			for(int i=1;i<exampleWorkflowList.size();i++){
			System.out.println("service url:"+actualURL);
			System.out.println(exampleWorkflowList.elementAt(0));
			if(exampleWorkflowList.elementAt(i).contains("\"")){
			 exampleWorkflowList.set(i, parseString(exampleWorkflowList.elementAt(i),"\""));
			                                                    }
		 	postAnnotations(actualURL,"example_workflow",exampleWorkflowList.elementAt(i),connectionCredentials);
		                                                  }
		                                 }
		else{
			System.out.println("No example workflow URL was found in MyExperiment");
	     	}
			
			break;
			   }
		}
	}
		else{
			System.out.println("Server returned:"+testConnect.getResponseCode());
		    }
		    
		} catch (XmlException e) {
			
			e.printStackTrace();
		}
		
		
		}
		
	}
	
	private static void postAnnotations(String toService,String dataType,String data,String connectionCredentials) throws IOException{
		String JSONtestString="{"
			  +"\"bulk_annotations\": [ {"
			  + " \"resource\": \""+toService+"\","
			  +"  \"annotations\": {"
			  +"\""+dataType+"\": \""+data+"\"" // +"\"description\": \"Manoli's test description. Please ignore\""
			  + " }"
			  +"}]}";
				
	     BioCatConnectAndPOST connectAndPost=new BioCatConnectAndPOST("http://sandbox.biocatalogue.org/annotations/bulk_create",JSONtestString,connectionCredentials);
		}
	
	protected static String parseString(String response, String delimiter){
		 response=response.trim();
		 int startchar=response.indexOf(delimiter);
		 int endchar=response.indexOf(delimiter, startchar+1);
		 return response.substring(startchar+1, endchar);
	 }
	
	
}
