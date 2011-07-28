package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import uk.co.magus.fourstore.client.Store;

public class AppendGraphTo4Store extends AppendGraph{

	
	public AppendGraphTo4Store(String graph, String URI,String storeLocation,String msg,int inputFormat){
		AppendGraph(graph,URI,storeLocation,msg,inputFormat);
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
	
	
	 void appendInXMLFormat(String graph, String URI,String msg,Object KB){
		Store store=(Store)KB;
		try {
			String response = store.append(URI, graph, Store.InputFormat.XML);
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
	
	 void appendInTurtleFormat(String graph, String URI,String msg,Object KB){
		Store store=(Store)KB;
		try {
			String response=store.append(URI,graph, Store.InputFormat.TURTLE);
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

