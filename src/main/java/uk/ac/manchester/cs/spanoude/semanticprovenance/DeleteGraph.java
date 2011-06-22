package uk.ac.manchester.cs.spanoude.semanticprovenance;
import uk.co.magus.fourstore.client.Store;
import java.io.IOException;
import java.net.MalformedURLException;


public class DeleteGraph {

	public DeleteGraph(Store store,String graph,String msg){
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
