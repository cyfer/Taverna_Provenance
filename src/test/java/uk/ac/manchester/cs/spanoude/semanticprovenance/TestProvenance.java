package uk.ac.manchester.cs.spanoude.semanticprovenance;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import uk.co.magus.fourstore.client.Store;

public class TestProvenance {
 private Provenance provTest=new Provenance();
 private Store store;
 private String tavernaDir="/Users/dragonfighter/Documents/Master_Thesis/tools/taverna-nightly-2.3-SNAPSHOT-20110527/";
 private String workflowDir="/Users/dragonfighter/Documents/Master_Thesis/workflows/getgotermsfromuniprotid_420087.t2flow"; //sample workflow
 private String[] args={"UniprotIds", "A2ABK9"};
 
 @Before
 public void createStoreConnection(){
	 provTest.UID="http://purl.org/taverna/janus#/46638a04-ed74-4c6a-af23-84dffd3470f6" ;
	 provTest.currentGraphURI="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/d176f9ae-7666-4595-a12b-a1e25a6b15e8";
 try {
	 store = new Store("http://localhost:8001");
	   
 }
 catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
   
 }
 
 @Test
 public void testFindWSDLServicesForEveryProvenanceGraph(){
	assertTrue("FindWSDLServicesForEveryProvenanceGraph Query did not run correctly", provTest.findWSDLServicesForEveryProvenanceGraph().contains("?processor") );
 }
 
 
 @Test
 public void testFindWSDLServicesForCurrentProvenanceGraph(){
		assertTrue("FindWSDLServicesForCurrentProvenanceGraph Query did not run correctly", provTest.findWSDLServicesForCurrentProvenanceGraph().contains("?processor") );
	 }
 
 
 @Test
 public void testFindWSDLServicesForCurrentAndAssociatedProvenanceGraphs(){
		assertTrue("FindWSDLServicesForCurrentAndAssociatedProvenanceGraphs Query did not run correctly", provTest.findWSDLServicesForCurrentAndAssociatedProvenanceGraphs().contains("?processor") );
	 }
 
 @Test
 public void testFindWSDLServicesForWorkflowUID(){
		assertTrue(" FindWSDLServicesForWorkflowUID Query did not run correctly", provTest.findWSDLServicesForWorkflowUID().contains("?processor") );
	 }
 
 @Test
 public void testParseResponse(){
	 assertEquals("test",provTest.parseResponse("|test|","|"));
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
