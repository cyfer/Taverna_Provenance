package uk.ac.manchester.cs.spanoude.semanticprovenance;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.junit.*;

public class TestAddGraphFactory {
	private static String tripleStore="4Store";
	GraphOperationFactory addGraphFactory= new GraphOperationFactory() ;
	private String sampleProvenance="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/d176f9ae-7666-4595-a12b-a1e25a6b15e8";
	String UID="d176f9ae-7666-4595-a12b-a1e25a6b15e8";
	String provenanceTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/d176f9ae-7666-4595-a12b-a1e25a6b15e8> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> <"+UID+">";
	private String storeLocation="http://localhost:8001";
	String msg="Success";
	
	
	@Before
	public void setupFactoryAndRegisterClass(){
	
	try {
		Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo"+tripleStore);
		addGraphFactory.registerTripleStoreOperation(tripleStore+"Add",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance.AddGraphTo4Store"));//AddGraphTo4Store AddGraphTo"+tripleStore
	} catch (ClassNotFoundException e) {
		
		e.printStackTrace();
	}
	}

@Test
public void TestClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	//System.out.println("Test start");
	//System.out.println(addGraphFactory.hashMapInfo());
	AddGraph testGraph= addGraphFactory.createAddGraph(tripleStore, provenanceTriple, sampleProvenance, storeLocation, msg, 1);
	System.out.println(testGraph.getClass().getName());
	assertTrue("Incorrect class",testGraph.getClass().getName().equals("AddGraphTo4Store"));
    
}
}
