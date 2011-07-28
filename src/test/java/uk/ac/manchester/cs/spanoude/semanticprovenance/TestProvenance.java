package uk.ac.manchester.cs.spanoude.semanticprovenance;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import uk.co.magus.fourstore.client.Store;

public class TestProvenance {
 private Provenance provTest=new Provenance();
 private Store store;
 private String tavernaDir="/Users/dragonfighter/Documents/Master_Thesis/tools/taverna-commandline-2.3.0/";
 private String workflowDir="/Users/dragonfighter/Documents/Master_Thesis/workflows/getgotermsfromuniprotid_420087.t2flow"; //sample workflow
 private String[] args={"UniprotIds", "A2ABK9"};
 //private GraphOperationFactory graphOperationFactory= new GraphOperationFactory() ;
 private static String tripleStore="4Store";
 private static String classPackage="uk.ac.manchester.cs.spanoude.semanticprovenance.";
 private static int querySoftLimit= 400;
	
 
 @Before
 public void createStoreConnection(){
	 provTest.UID="http://ns.taverna.org.uk/2010/workflow/46638a04-ed74-4c6a-af23-84dffd3470f6/" ;
	 provTest.currentGraphURI="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/3a1dbb06-540d-43ba-b81f-0b83345f35df";//d176f9ae-7666-4595-a12b-a1e25a6b15e8
     provTest.createCSVFile=false;
     provTest.filePathForCSV="/Users/dragonfighter/Documents/Master_Thesis/workflows/";
	
     /** try {
	 store = new Store("http://localhost:8001");
	 
	   
 }
 catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	*/
	 try {
			//Class.forName(classPackage+"AddGraphTo"+tripleStore);
		 provTest.graphOperationFactory.registerTripleStoreOperation(tripleStore+"Add",Class.forName(classPackage+"AddGraphTo"+tripleStore));
		 provTest.graphOperationFactory.registerTripleStoreOperation(tripleStore+"Append",Class.forName(classPackage+"AppendGraphTo"+tripleStore));
		 provTest.graphOperationFactory.registerTripleStoreOperation(tripleStore+"Delete",Class.forName(classPackage+"DeleteGraphFrom"+tripleStore));
		 provTest.graphOperationFactory.registerTripleStoreOperation(tripleStore+"Query",Class.forName(classPackage+"Query"+tripleStore));
			 
		 } catch (ClassNotFoundException e1) {
			
			e1.printStackTrace();
		}
   
 }
 
 
 @Test
 public void testFindWSDLServicesForEveryProvenanceGraph() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	assertTrue("FindWSDLServicesForEveryProvenanceGraph Query did not run correctly", provTest.findWSDLServicesForEveryProvenanceGraph(provTest.graphOperationFactory).contains("?processor") );
 }

 
 
 @Test
 public void testFindWSDLServicesForCurrentProvenanceGraph() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		assertTrue("FindWSDLServicesForCurrentProvenanceGraph Query did not run correctly", provTest.findWSDLServicesForCurrentProvenanceGraph(provTest.graphOperationFactory).contains("?processor") );
	 }
 
 
 @Test
 public void testFindWSDLServicesForCurrentAndAssociatedProvenanceGraphs() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		assertTrue("FindWSDLServicesForCurrentAndAssociatedProvenanceGraphs Query did not run correctly", provTest.findWSDLServicesForCurrentAndAssociatedProvenanceGraphs(provTest.graphOperationFactory).contains("?processor") );
	 }
 
 @Test
 public void testFindWSDLServicesForWorkflowUID() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		assertTrue(" FindWSDLServicesForWorkflowUID Query did not run correctly", provTest.findWSDLServicesForWorkflowUID(provTest.graphOperationFactory).contains("?processor") );
	 }
 
 @Test
 public void testParseResponse(){
	 assertEquals("test",provTest.parseResponse("|test|","|"));
 }


 @Test
 public void testParseQueryResults() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	 assertTrue("Array is empty",provTest.parseQueryResults(provTest.findWSDLServicesForWorkflowUID(provTest.graphOperationFactory)).size()>0);
 }
 
 
 /**
  @Test
	public void testRunWorkflow() {
		assertEquals("Success",provTest.runWorkflow(tavernaDir, args, workflowDir));
  }

	@Test
	public void testMain() {
		fail("Not yet implemented");
	}

	*/
	
}
