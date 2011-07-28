package uk.ac.manchester.cs.spanoude.semanticprovenance;

public abstract class AddGraph {

	String graph;
	String URI;
	String storeLocation;
	String msg;
	int inputFormat;
	
	public  void AddGraph(String graph, String URI,String storeLocation,String msg,int inputFormat){
		this.graph=graph;
		this.URI=URI;
		this.storeLocation=storeLocation;
		this.msg=msg;
		this.inputFormat=inputFormat;
		
		Object store = getKBInstance(storeLocation ); //storeLocation   "http://localhost:8001"
		System.out.println("Adding graph");
		if (inputFormat==1){
			addInXMLFormat(graph, URI,msg, store);
		}
		else if(inputFormat==2){
			addInTurtleFormat(graph, URI,msg, store);
		}
	}
	
	abstract Object getKBInstance(String storeLocation);
	abstract void addInXMLFormat(String graph, String URI,String msg,Object KB);
	abstract void addInTurtleFormat(String graph, String URI,String msg,Object KB);
}
