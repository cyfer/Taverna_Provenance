package uk.ac.manchester.cs.spanoude.semanticprovenance;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.junit.*;
import java.util.List;

public class TestConnectionFactory {
	private static String service="BioCatalogue";
	ConnectionFactory connectionFactory= new ConnectionFactory() ;
	private Vector<String> myExperimentResults=new Vector<String>();
	private List<ResultBindings> resultBindings=new Vector<ResultBindings>(); //=new List<ResultBindings>()
	private String websiteToAnnotate="http://sandbox.biocatalogue.org";
	
	@Before
	public void setupFactoryAndRegisterClass(){
		
		try {
			Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance."+service+"Connection");
			connectionFactory.registerServiceConnections(service+"Connection",Class.forName("uk.ac.manchester.cs.spanoude.semanticprovenance."+service+"Connection"));
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
@Test
public void TestConnectionClassCreation() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
		Connection testConnection= connectionFactory.createConnection(service+"Connection", resultBindings, myExperimentResults, "username:password",websiteToAnnotate);
		assertTrue("Incorrect class",testConnection.getClass().getName().equals("uk.ac.manchester.cs.spanoude.semanticprovenance.BioCatalogueConnection"));
	    
	}
}
