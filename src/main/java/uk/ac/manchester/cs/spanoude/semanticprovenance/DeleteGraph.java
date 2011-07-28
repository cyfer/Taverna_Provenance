package uk.ac.manchester.cs.spanoude.semanticprovenance;

public abstract class DeleteGraph {
	String graph;
	String storeLocation;
	String msg;
	
	public void  DeleteGraph(String graph,String storeLocation,String msg){
		this.graph=graph;
		this.storeLocation=storeLocation;
		this.msg=msg;
		
		Object store = getKBInstance(storeLocation ); 
		System.out.println("Deleting graph");
		 deleteGraph(graph, msg,store);
	}
	
	abstract Object getKBInstance(String storeLocation);
	abstract void deleteGraph(String graph,String msg,Object KB);
}
