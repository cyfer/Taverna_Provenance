package uk.ac.manchester.cs.spanoude.semanticprovenance;
import java.io.IOException;
import java.net.MalformedURLException;

import uk.co.magus.fourstore.client.Store;

public class AddGraph {
	
	// "https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#"
	
	public AddGraph(String graph, String URI, Store store,String msg,int inputFormat){
		try {
			String response=""; 			
			System.out.println("Adding graph");
			if (inputFormat==1){
			response = store.add(URI,
					graph, Store.InputFormat.XML);
			}
			else if(inputFormat==2){
				response = store.add(URI,
						graph, Store.InputFormat.TURTLE);
			}
			System.out.println(response);
			System.out.println(msg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
