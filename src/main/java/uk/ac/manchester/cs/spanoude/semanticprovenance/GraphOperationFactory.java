package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class GraphOperationFactory {

	private HashMap m_RegisteredTriplestoresOperations = new HashMap();
	
	public void registerTripleStoreOperation (String tripleStore, Class operationClass)
	{
		 m_RegisteredTriplestoresOperations.put(tripleStore, operationClass);
	}

	public AddGraph createAddGraph(String tripleStoreAndOperation,String graph, String URI,String storeLocation,String msg,int inputFormat) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Class addGraphClass = (Class)m_RegisteredTriplestoresOperations.get(tripleStoreAndOperation);
		Constructor addGraphClassConstructor = addGraphClass.getDeclaredConstructor(new Class[] { String.class,String.class,String.class,String.class,int.class});
		return (AddGraph)addGraphClassConstructor.newInstance(new Object[] {graph,URI,storeLocation,msg,inputFormat });
	}
	
	public AppendGraph createAppendGraph(String tripleStoreAndOperation,String graph, String URI,String storeLocation,String msg,int inputFormat) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		Class appendGraphClass = (Class)m_RegisteredTriplestoresOperations.get(tripleStoreAndOperation);
		//System.out.println(appendGraphClass);
		Constructor appendGraphClassConstructor = appendGraphClass.getDeclaredConstructor(new Class[] { String.class,String.class,String.class,String.class,int.class});
		return (AppendGraph)appendGraphClassConstructor.newInstance(new Object[] {graph,URI,storeLocation,msg,inputFormat });
	}
	
	public DeleteGraph createDeleteGraph(String tripleStoreAndOperation,String graph, String storeLocation,String msg) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		Class deleteGraphClass = (Class)m_RegisteredTriplestoresOperations.get(tripleStoreAndOperation);
		Constructor deleteGraphClassConstructor = deleteGraphClass.getDeclaredConstructor(new Class[] { String.class,String.class,String.class });
		return (DeleteGraph)deleteGraphClassConstructor.newInstance(new Object[] {graph,storeLocation,msg});
	}
	
	public Query createQuery(String tripleStoreAndOperation,String query, String storeLocation,String msg,int softLimit) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		Class queryClass = (Class)m_RegisteredTriplestoresOperations.get(tripleStoreAndOperation);
		Constructor queryClassConstructor = queryClass.getDeclaredConstructor(new Class[] { String.class,String.class,String.class,int.class });
		return (Query)queryClassConstructor.newInstance(new Object[] {query,storeLocation,msg,softLimit});
	}
	
	
	
	public int hashMapInfo(){
		System.out.println(m_RegisteredTriplestoresOperations.get("4Store"));
		return m_RegisteredTriplestoresOperations.size();
	}


}
