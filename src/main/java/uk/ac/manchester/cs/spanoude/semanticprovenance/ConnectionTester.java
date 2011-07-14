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

/**
 * Tester class.
 * @author Emmanouil Spanoudakis
 *
 *
 */

public class ConnectionTester {
	private static InputStream serverResponse = null;
	public  ConnectionTester(){
		
	}
	
	public static void main(String[] args) throws IOException{
		
	
	String myExperimentQuery= "PREFIX mecomp: <http://rdf.myexperiment.org/ontologies/components/>"
        +"PREFIX dcterms: <http://purl.org/dc/terms/>"
        +"PREFIX mebase: <http://rdf.myexperiment.org/ontologies/base/>"
        +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"

       +"SELECT ?s WHERE {"
       +"?s dcterms:identifier ?o .  FILTER (regex(str(?o), 'bb9ce24e-4a54-4111-a4fe-a55d0e80ff95'))"
       +"} LIMIT 200";
	MyExperimentConnection testMyExp=new MyExperimentConnection(myExperimentQuery);
	
	
	
	
	
	
	/**
	 JSONObject json = new JSONObject();
	 JSONObject description = new JSONObject();
	 JSONObject singleResourceAnnotation=new JSONObject();
	 JSONObject singleResourceAnnotation2=new JSONObject();
	 description.put("description","Manoli's test description. Please ignore" );
	 //resource.put("resource", annotations);
	 //json.put( "bulk_annotations", resource);
	// json.put("bulk_annotations", annotations);
	 singleResourceAnnotation.put("resource","http://sandbox.biocatalogue.org/soap_operations/1057");
	 singleResourceAnnotation.put("annotations", description);
	 singleResourceAnnotation2.put("resource","http://sandbox.biocatalogue.org/soap_operations/1059");
	 singleResourceAnnotation2.put("annotations", description);
	 json.accumulate("bulk_annotations", singleResourceAnnotation);
	 json.accumulate("bulk_annotations", singleResourceAnnotation2);

	 System.out.println( json.toString(2) );
	 String JSONQuery=json.toString(2);
	 ConnectAndPOST testConnectAndPost=new ConnectAndPOST("http://sandbox.biocatalogue.org/annotations/bulk_create",JSONQuery);
   
	/**
	String JSONtestString="{"
  +"\"bulk_annotations\": [ {"
  + " \"resource\": \"http://sandbox.biocatalogue.org/soap_operations/1057\","
  +"  \"annotations\": {"
  +"\"description\": \"Manoli's test description. Please ignore\""
  + " }"
  +"}]}";
	
    ConnectAndPOST testConnectAndPost=new ConnectAndPOST("http://sandbox.biocatalogue.org/annotations/bulk_create",JSONtestString);
	*/
	
	
	//postAnnotations("http://sandbox.biocatalogue.org/soap_inputs/1503");  //    http://sandbox.biocatalogue.org/soap_operations/1057
	
	/**
	//Step 1. get Soap Services and operations
	SoapService servicesData;  
	try {
		ConnectAndGET testConnect=new ConnectAndGET(urlToConnectTo);
		servicesData =  SoapServiceDocument.Factory.parse(testConnect.getServerResponse()).getSoapService();
		System.out.println(servicesData.getName());
		Operations operations=servicesData.getOperations();
		SoapOperation[] opArray=operations.getSoapOperationArray();
	    System.out.println(servicesData.getDescription());
	 //Step 2. get Service Inputs and Outputs from Soap Operations
	   for (SoapOperation entry:opArray ){
		   String actualURL=entry.getHref();     //Url of service
		   urlToConnectTo = "http://sandbox.biocatalogue.org/lookup?wsdl_location=http://soap.bind.ca/wsdl/bind.wsdl";
		   urlToConnectTo=urlToConnectTo+"&operation_name="+entry.getName();
		   testConnect=new ConnectAndGET(urlToConnectTo);
		    SoapOperation soapOperation =SoapOperationDocument.Factory.parse(testConnect.getServerResponse()).getSoapOperation();
		    Inputs opInputs= soapOperation.getInputs();
			Outputs opOutputs=soapOperation.getOutputs();
			SoapInput[] inputArray=opInputs.getSoapInputArray();
			SoapOutput[] outputArray=opOutputs.getSoapOutputArray();	
			
			//SoapInput opInput=(SoapInput)SoapInputDocument.Factory.parse(serverResponse2).getSoapInput();
		   //System.out.println("Input test "+opInput.getName());
		  // SoapInput opInput= (SoapInput) entry.getInputs();
		  //System.out.println("Input test "+opInput.getDescription());
		  // SoapInput[] inputArray=opInput.g;
		  //System.out.println("Input Array length: "+opInput.sizeOfSoapInputArray());
		  System.out.println("Service name: "+entry.getName());
		//  System.out.println("Input test "+opInput.getName());
		  
		  
		  for(int i=0;i<inputArray.length;i++){
	    	System.out.println("Input"+i+" for service:"+inputArray[i].getName());
	    	String InputURL=inputArray[i].getHref();
	    	System.out.println(InputURL);
	    	}
		 
		  for(int i=0;i<outputArray.length;i++){
		    	System.out.println("Output"+i+" for service:"+outputArray[i].getName());
		    	}
		    	
	    }
	    
	} catch (XmlException e) {
		
		e.printStackTrace();
	}
	
	

	
	/**
	try { 
		BufferedReader br = new BufferedReader(new InputStreamReader(serverResponse)); String str = "";
	
	while ((str = br.readLine()) != null) { 
		text.append(str + "\n");
	}
	
	br.close();
	} 
	catch (Exception e) {
	System.err.println("An error has occurred, details:\n" + e.getMessage());
	}
		
	System.out.println(text.toString());
	
	*/
	
	
}
	
private static void postAnnotations(String toService) throws IOException{
	String JSONtestString="{"
		  +"\"bulk_annotations\": [ {"
		  + " \"resource\": \""+toService+"\","
		  +"  \"annotations\": {"
		  +"\"example_data\": \"Manoli's test description. Please ignore\"" // +"\"description\": \"Manoli's test description. Please ignore\""
		  + " }"
		  +"}]}";
			
     BioCatConnectAndPOST testConnectAndPost=new BioCatConnectAndPOST("http://sandbox.biocatalogue.org/annotations/bulk_create",JSONtestString);
	}
}