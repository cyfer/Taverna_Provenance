package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import uk.co.magus.fourstore.client.Store;

public class Query4Store extends Query {

	public Query4Store(String query,String storeLocation,String msg,int softLimit){
		Query(query,storeLocation,msg,softLimit);
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
	
	String queryKB(String query,String storeLocation,String msg,int softLimit,Object KB){
		Store store=(Store)KB;
		String response="";
		try {
			response = store.query(query,Store.OutputFormat.TAB_SEPARATED,softLimit);
			System.out.println(response);
			System.out.println("\n "+msg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
}
