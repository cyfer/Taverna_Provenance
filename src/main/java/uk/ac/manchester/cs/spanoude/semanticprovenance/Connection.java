package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.util.List;
import java.util.Vector;

public abstract class Connection {

	List<ResultBindings> resultsList;
	Vector<String> exampleWorkflowList;
	String connectionCredentials;
	String websiteToAnnotate;
	
	public void Connection(List<ResultBindings> resultsList, Vector<String> exampleWorkflowList,String connectionCredentials,String websiteToAnnotate){
		this.resultsList=resultsList;
		this.exampleWorkflowList=exampleWorkflowList;
		this.connectionCredentials=connectionCredentials;
		this.websiteToAnnotate=websiteToAnnotate;
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
	
	public String getWebsiteToAnnotate(){
		return websiteToAnnotate;
	}
}
