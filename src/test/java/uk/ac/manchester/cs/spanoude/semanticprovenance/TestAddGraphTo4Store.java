package uk.ac.manchester.cs.spanoude.semanticprovenance;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.co.magus.fourstore.client.Store;

public class TestAddGraphTo4Store {
	String storeLocation="http://localhost:8001";
	GraphOperationFactory graphOperationFactory= new GraphOperationFactory() ;
	String tripleStore="4Store";
	String testURI="http://TestURI";
	AddGraphTo4Store addGraphTo4Store=new AddGraphTo4Store("a query",testURI,storeLocation,"a message to print",1);
	
	@Before
	public void setupFactoryAndRegisterClass(){
	
	try {
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo4Store");

		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Add",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo4Store"));//AddGraphTo4Store AddGraphTo"+tripleStore
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Delete",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.DeleteGraphFrom4Store"));//DeleteGraphFrom4Store DeleteGraphFrom"+tripleStore
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Query",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.Query4Store"));//Query4Store Query"+tripleStore
	
	
	} catch (ClassNotFoundException e) {
		
		e.printStackTrace();
	}
	}
	
	@Test
	public void testGetKBInstance(){
		Store testStore=(Store) addGraphTo4Store.getKBInstance(storeLocation);
		assertTrue("Incorrect class",testStore.getClass().getName().equals("uk.co.magus.fourstore.client.Store"));
	}
}
