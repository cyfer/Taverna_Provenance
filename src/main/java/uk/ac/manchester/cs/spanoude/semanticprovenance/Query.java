package uk.ac.manchester.cs.spanoude.semanticprovenance;
import uk.co.magus.fourstore.client.Store;
import java.io.IOException;
import java.net.MalformedURLException;

public abstract class Query {
    String StoreResponse="";
	String query;
	String storeLocation;
	String msg;
	int softLimit;
	
	public void Query(String query,String storeLocation,String msg,int softLimit){
		this.query=query;
		this.storeLocation=storeLocation;
		this.msg=msg;
		this.softLimit=softLimit;
		
		Object store = getKBInstance(storeLocation ); //storeLocation   "http://localhost:8001"
		System.out.println("Querying KB..");
		StoreResponse=queryKB(query,storeLocation,msg,softLimit,store);
			
	}
	
	public String getStoreResponse(){
		return StoreResponse;
	}
	
	abstract Object getKBInstance(String storeLocation);
	abstract String queryKB(String query, String storeLocation,String msg,int softLimit,Object KB);
}
