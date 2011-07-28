package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.IOException;
import java.net.MalformedURLException;

import uk.co.magus.fourstore.client.Store;

public abstract class AppendGraph {

	String graph;
	String URI;
	String storeLocation;
	String msg;
	int inputFormat;
	
	public void AppendGraph(String graph, String URI,String storeLocation,String msg,int inputFormat){
		
		this.graph=graph;
		this.URI=URI;
		this.storeLocation=storeLocation;
		this.msg=msg;
		this.inputFormat=inputFormat;
		
		Object store = getKBInstance(storeLocation ); //storeLocation   "http://localhost:8001"
		System.out.println("Appending graph");
		if (inputFormat==1){
			appendInXMLFormat(graph, URI,msg, store);
		}
		else if(inputFormat==2){
			appendInTurtleFormat(graph, URI,msg, store);
		}
	}
	
	abstract Object getKBInstance(String storeLocation);
	abstract void appendInXMLFormat(String graph, String URI,String msg,Object KB);
	abstract void appendInTurtleFormat(String graph, String URI,String msg,Object KB);
		
		
	
}
