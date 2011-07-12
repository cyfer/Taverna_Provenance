package uk.ac.manchester.cs.spanoude.semanticprovenance;
import uk.co.magus.fourstore.client.Store;
import java.io.IOException;
import java.net.MalformedURLException;


public class DeleteGraph {

	public DeleteGraph(String storeType,String graph,String msg){
		if(storeType.equals("4store")){
		try{
		Store store = new Store("http://localhost:8001");
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
		else{
			System.out.println("Store not currently supported");
		}
	}
}
