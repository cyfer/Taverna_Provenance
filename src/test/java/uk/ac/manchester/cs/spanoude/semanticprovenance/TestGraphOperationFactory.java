package uk.ac.manchester.cs.spanoude.semanticprovenance;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.junit.*;

public class TestGraphOperationFactory {
	private static String tripleStore="4Store";
	GraphOperationFactory graphOperationFactory= new GraphOperationFactory() ;
	String sampleProvenance="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/d176f9ae-7666-4595-a12b-a1e25a6b15e8";
	String UID="d176f9ae-7666-4595-a12b-a1e25a6b15e8";
	String provenanceTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/d176f9ae-7666-4595-a12b-a1e25a6b15e8> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> <"+UID+">";
	String storeLocation="http://localhost:8001";
	String msg="Success";
	String sampleQuery="A query";
	
	@Before
	public void setupFactoryAndRegisterClass(){
	
	try {
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo"+tripleStore);
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AppendGraphTo"+tripleStore);
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.DeleteGraphFrom"+tripleStore);
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.Query"+tripleStore);
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Add",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo4Store"));//AddGraphTo4Store AddGraphTo"+tripleStore
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Append",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AppendGraphTo4Store"));//AppendGraphTo4Store AppendGraphTo"+tripleStore
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Delete",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.DeleteGraphFrom4Store"));//DeleteGraphFrom4Store DeleteGraphFrom"+tripleStore
		graphOperationFactory.registerTripleStoreOperation(tripleStore+"Query",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.Query4Store"));//Query4Store Query"+tripleStore
	
	
	} catch (ClassNotFoundException e) {
		
		e.printStackTrace();
	}
	}

@Test
public void TestAddGraphClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	AddGraph testGraph= graphOperationFactory.createAddGraph(tripleStore+"Add", provenanceTriple, sampleProvenance, storeLocation, msg, 1);
	assertTrue("Incorrect class",testGraph.getClass().getName().equals("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo4Store"));
    
}

@Test
public void TestAppendGraphClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	AppendGraph testGraph= graphOperationFactory.createAppendGraph(tripleStore+"Append", provenanceTriple, sampleProvenance, storeLocation, msg, 1);
	assertTrue("Incorrect class",testGraph.getClass().getName().equals("uk.ac.manchester.cs.spanoude.semanticprovenance.AppendGraphTo4Store"));
}

@Test
public void TestDeleteGraphClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	DeleteGraph testGraph= graphOperationFactory.createDeleteGraph(tripleStore+"Delete", sampleProvenance, storeLocation, msg);
	assertTrue("Incorrect class",testGraph.getClass().getName().equals("uk.ac.manchester.cs.spanoude.semanticprovenance.DeleteGraphFrom4Store"));
}

@Test
public void TestQueryClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	Query query= graphOperationFactory.createQuery(tripleStore+"Query", sampleQuery, storeLocation, msg,200);
	assertTrue("Incorrect class",query.getClass().getName().equals("uk.ac.manchester.cs.spanoude.semanticprovenance.Query4Store"));
}

@Test
public void testHashMapInfo(){
	assertTrue("Incorrect number of classes registered",graphOperationFactory.hashMapInfo()==4);

}
}
