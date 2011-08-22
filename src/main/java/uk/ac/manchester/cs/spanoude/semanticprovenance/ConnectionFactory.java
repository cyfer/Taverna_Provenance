package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class ConnectionFactory {
	
	private HashMap m_RegisteredServicesConnections = new HashMap();
	
	public void registerServiceConnections (String service, Class connectionClass)
	{
		m_RegisteredServicesConnections.put(service, connectionClass);
	}

	
	public Connection createConnection(String serviceConnection,List<ResultBindings> resultsList, Vector<String> exampleWorkflowList, String connectionCredentials,String websiteToAnnotate) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Class connectionClass = (Class)m_RegisteredServicesConnections.get(serviceConnection);
		Constructor connectionClassConstructor = connectionClass.getDeclaredConstructor(new Class[] { List.class,Vector.class,String.class,String.class});
		return (Connection)connectionClassConstructor.newInstance(new Object[] {resultsList,exampleWorkflowList,connectionCredentials,websiteToAnnotate });
	}

}
