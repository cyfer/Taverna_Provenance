package uk.ac.manchester.cs.spanoude.semanticprovenance;
import static org.junit.Assert.*;

import org.junit.Test;
import uk.co.magus.fourstore.client.Store;

public class TestQuery4Store {
    private String storeLocation="http://localhost:8001";
	private Query4Store query4Store=new Query4Store("a query",storeLocation,"a message to print",200);
	
@Test
public void testGetKBInstance(){
	Store testStore=(Store) query4Store.getKBInstance(storeLocation);
	assertTrue("Incorrect class",testStore.getClass().getName().equals("uk.co.magus.fourstore.client.Store"));
}
	
}
