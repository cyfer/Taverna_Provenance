package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.util.List;
import java.util.Vector;

public abstract class Connection {

	List<ResultBindings> resultsList;
	Vector<String> exampleWorkflowList;
	String connectionCredentials;
	
	public void Connection(List<ResultBindings> resultsList, Vector<String> exampleWorkflowList,String connectionCredentials){
		this.resultsList=resultsList;
		this.exampleWorkflowList=exampleWorkflowList;
		this.connectionCredentials=connectionCredentials;
	}

	public List<ResultBindings> getResultsList() {
		return resultsList;
	}

	public Vector<String> getExampleWorkflowList() {
		return exampleWorkflowList;
	}

	public String getConnectionCredentials() {
		return connectionCredentials;
	}
}
