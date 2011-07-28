package uk.ac.manchester.cs.spanoude.semanticprovenance;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import uk.co.magus.fourstore.client.Store;

public class AddGraphTo4Store extends AddGraph {
	
	
	// "https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#"
	
	
	
	public AddGraphTo4Store(String graph, String URI,String storeLocation,String msg,int inputFormat)  {
		  
		   AddGraph(graph,URI,storeLocation,msg,inputFormat);
		}
	
	
	
	 Object getKBInstance(String storeLocation){
		try {
			Store store = new Store(storeLocation );
			return store;
		} catch (MalformedURLException e) {
		  e.printStackTrace();
		}
		return null;
	}
	
	
	 void addInXMLFormat(String graph, String URI,String msg,Object KB){
		Store store=(Store)KB;
		try {
			String response=store.add(URI,graph, Store.InputFormat.XML);
			System.out.println(response);
			System.out.println(msg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	 void addInTurtleFormat(String graph, String URI,String msg,Object KB){
		Store store=(Store)KB;
		try {
			String response=store.add(URI,graph, Store.InputFormat.TURTLE);
			System.out.println(response);
			System.out.println(msg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
}
