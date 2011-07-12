package uk.ac.manchester.cs.spanoude.semanticprovenance;
import uk.co.magus.fourstore.client.Store;
import java.io.IOException;
import java.net.MalformedURLException;

public class Query {
	private String StoreResponse="";

	public Query(String query,String storeType,String msg){
		if(storeType.equals("4store")){
		try{
		Store store = new Store("http://localhost:8001");
		String response = store.query(query,Store.OutputFormat.TAB_SEPARATED,100);
		StoreResponse=response;
		System.out.println("\n "+msg);
		System.out.println(response);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		else{
			System.out.println("Store not currently supported");
		}
	}
	
	public String getStoreResponse(){
		return StoreResponse;
	}
}
