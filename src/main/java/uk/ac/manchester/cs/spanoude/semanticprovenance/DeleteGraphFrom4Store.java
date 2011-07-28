package uk.ac.manchester.cs.spanoude.semanticprovenance;
import uk.co.magus.fourstore.client.Store;
import java.io.IOException;
import java.net.MalformedURLException;


public class DeleteGraphFrom4Store extends DeleteGraph {

	public DeleteGraphFrom4Store(String storeLocation,String graph,String msg){
		
		DeleteGraph(storeLocation,graph,msg);
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
	
	void deleteGraph(String graph,String msg,Object KB){
		Store store=(Store)KB;
		try{
			String response = store.delete(graph);
			System.out.println(response);
			System.out.println(msg);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
