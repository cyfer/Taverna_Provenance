package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import uk.co.magus.fourstore.client.Store;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.io.WriterException;
import java.util.zip.*;
import java.util.*;
import java.io.*;

/**
 * 
 * @author Emmanouil Spanoudakis
 *
 *
 */

public class Provenance {

	/**
	 * @param args - args[0]= /this : Returns wsdl services for the graph being inserted only
	 *               args[0]= /all : Returns wsdl services for all graphs in the KB
	 *               args[1]= provenance graph to be inserted in the KB (in .rdf format)
	 *               args[2]= workflow file (.t2flow format) that is converted into .scufl2 format and then extracted. It contains
	 *               the Profile and dataflow files that are inserted in the KB
	 *               args[3]= The directory specified by the user where the .csv files will be created
	 */
	private static String prefixes= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
        +"PREFIX j: <http://purl.org/taverna/janus#>"
        +"PREFIX scufl2: <http://ns.taverna.org.uk/2010/scufl2#>"
        +"PREFIX taverna: <http://ns.taverna.org.uk/2010/activity/wsdl/operation#>"
        +"PREFIX myont:<https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#>";
	
	private static String UID="";
	private static String currentGraphURI="";
	private static Boolean newGraphInserted=false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	    		
		Store store;
		Query newQuery;
		String scufl2File="";
		String response;
		try {
			System.out.println("Starting...");
			store = new Store("http://localhost:8001");
			String provenanceGraph= FileUtils.readFileToString(new File(args[1]), "utf-8");
						
			loadProvenanceFileInStore(store,provenanceGraph);			
						
			System.out.println("Creating Scufl2 file...");
			try {
			  scufl2File=createScufl2File(args[2]);
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (ReaderException e) {
				e.printStackTrace();
			} catch (WriterException e) {
				e.printStackTrace();
			}			                  
		    
			String unzipDirectory=unzipScufl2File(args[2]);
			loadProfileAndDataflowFiles(store,unzipDirectory);
			
		if (args[0].equals("all"))	
		 findWSDLServicesForEveryProvenanceGraph(store, args[3]);
		else if(args[0].equals("this")){
		if (newGraphInserted)
			findWSDLServicesForCurrentProvenanceGraph(store, args[3]);
		else
			System.out.println("Graph was not inserted because it is already present in the KB. Returning results from old graph.");
		    findWSDLServicesForCurrentProvenanceGraph(store, args[3]);
		}
		}
			catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	}
	
	//
	
private static void loadProvenanceFileInStore(Store store,String graph){
	AddGraph newGraph;
	DeleteGraph delete;
	Query newQuery;	
	String response;
	boolean masterGraphExists;
	//--Check if provenance master graph exists
	String findProvenanceGraphQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> ?o }";
	newQuery=new Query(findProvenanceGraphQuery,store,"Check for master provenance graph");
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//insert the provenance file with a temporary URI in order to query it for its UID and workflow run URIs
	String idQuery= prefixes+"SELECT ?idURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?idURI rdf:type j:workflow_spec }} LIMIT 100"; //. ?idURI rdfs:comment ?workflowId if we need the actual id string
	newGraph=new AddGraph(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance",store,"Loading temporary provenance graph...",1);  //add temporary provenance graph
	newQuery= new Query(idQuery,store,"Getting unique workflow id...");    //get unique workflow id
	response= newQuery.getStoreResponse();
	//response=parseResponse(response,"\"");           //parse server response to get actual id string
	UID=response.substring(response.indexOf("<")+1, response.indexOf(">"));
	System.out.println("UID:"+UID);
	String findWorkflowRunQuery= prefixes+"SELECT ?runURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?runURI rdf:type j:workflow_run }} LIMIT 100";
	newQuery= new Query(findWorkflowRunQuery,store,"Getting workflow run id...");    //get unique workflow id
	response=newQuery.getStoreResponse();
	
	//--Delete temporary graph
	delete= new DeleteGraph(store,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance","Deleted temporary graph");   //Delete temporary graph
	
	//--Check if the provenance file has already been inserted in the triple store 
	String workflowrunIdURI=response.substring(response.indexOf("<"), response.lastIndexOf(">")+1);
	String checkWorkflowRunUniquenessQuery=prefixes+"ASK {"+workflowrunIdURI+" rdf:type j:workflow_run}";
	newQuery= new Query(checkWorkflowRunUniquenessQuery,store,"Checking workflow run id uniqueness...");
	response=newQuery.getStoreResponse();
	System.out.println(response);		
	
	//--If workflow run is not found amongst provenance files, insert the graph and references. Otherwise return.
	if(response.contains("false")){    
    UUID provenanceUID = UUID.randomUUID();
    newGraph=new AddGraph(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID),store,"Loading finalised provenance graph...",1);  //Re-insert graph, with correct URI based on workflow UID
    System.out.println("Graph: "+"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+" has been added to KB" );
    currentGraphURI="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+">";
    newGraphInserted=true;
  //-----add reference triples in the master graph
	String provenanceTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=new AppendGraph(provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",store,"Appending triple to master provenance graph",2);
			}
	else{
		System.out.println("Creating new master provenance graph");
		newGraph=new AddGraph(provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",store,"Creating new master provenance graph",2);
        	}
	}
	else{
		System.out.println("These provenance data already exist in the triple store!");
		//System.out.println(workflowrunIdURI);
		String getMatchingProvenanceGraph=prefixes+" SELECT ?provenance  WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance>{"
                                          +"?provenance myont:isProvenanceOf <"+UID+"> }"
                                          +" GRAPH ?provenance {"+workflowrunIdURI+" rdf:type j:workflow_run }"
                                          +"} LIMIT 200";

		newQuery= new Query(getMatchingProvenanceGraph,store,"Getting existing graph URI...");
		currentGraphURI=newQuery.getStoreResponse();
		currentGraphURI=currentGraphURI.substring(currentGraphURI.indexOf("<"), currentGraphURI.lastIndexOf(">")+1);
		System.out.println(currentGraphURI);
	}	

}



/**
 * @author Stian Soiland-Reyes
 * @param filepath
 * @throws JAXBException
 * @throws IOException
 * @throws ReaderException
 * @throws WriterException
 */
private static String  createScufl2File(String filepath) throws JAXBException, IOException,
ReaderException, WriterException{	
	    WorkflowBundleIO io = new WorkflowBundleIO();
		File t2File = new File(filepath);
		String filename = t2File.getName();
		filename = filename.replaceFirst("\\..*", ".scufl2"); //
		File scufl2File = new File(t2File.getParentFile(), filename);
		WorkflowBundle wfBundle = io.readBundle(t2File, "application/vnd.taverna.t2flow+xml");
		io.writeBundle(wfBundle, scufl2File, "application/vnd.taverna.scufl2.workflow-bundle");
		return filename;
	}



private static String unzipScufl2File(String filepath){
	 final int BUFFER = 2048;
	 try {
         BufferedOutputStream dest = null;
         BufferedInputStream is = null;
         ZipEntry entry;
         
        File initialFile = new File(filepath);
 		String filename = initialFile.getName();
 		filename = filename.replaceFirst("\\..*", ".scufl2"); 
 		File scufl2File = new File(initialFile.getParentFile(), filename);
 		String zipdir=scufl2File.getPath();
 		File dirfile = new File(zipdir+"_contents");
        dirfile.mkdir();
        System.out.println("Create dir " + dirfile.getAbsolutePath());
 		String path=dirfile.getAbsolutePath()+"/";
 		ZipFile zipfile = new ZipFile(scufl2File);
        Enumeration e = zipfile.entries();
           
        while(e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            if (entry.isDirectory()) {
                File file = new File(path+entry.getName());
                file.mkdir();
                System.out.println("Create dir " + entry.getName());
            }
            else{
            System.out.println("Extracting: " +entry);
            is = new BufferedInputStream(zipfile.getInputStream(entry));
            int count;
            byte data[] = new byte[BUFFER];
            FileOutputStream fos = new FileOutputStream(new File(path+entry.getName()));
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = is.read(data, 0, BUFFER)) 
              != -1) {
               dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            is.close();
         }
           }
        return path;
      } catch(Exception e) {
         e.printStackTrace();
      }
      return "error occured";
}

private static void loadProfileAndDataflowFiles(Store store, String baseDir){
	String workflowBundleGraph="";
	String profileGraph="";
	String dataflowGraph="";
	AddGraph newGraph;
	Query newQuery;	
	boolean masterGraphExists;
	String response;
	
	//-- Check if all 3 master graphs exist. Only one check is needed, that of the master bundle graph.
	//-- If the master bundle graph exists so will the profile and dataflow master graphs
	String findMasterBundleGraphQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> ?o }";
	newQuery=new Query(findMasterBundleGraphQuery,store,"Check for master bundle graph");
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//-- Check if a workflowBundle graph exists for the specific provenance file
	String checkBundleExistenceQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+"> }";
	newQuery= new Query(checkBundleExistenceQuery,store,"Checking if workflow bundle for the specified provenance file exists...");
	if(newQuery.getStoreResponse().contains("false")){
	try {
		workflowBundleGraph = FileUtils.readFileToString(new File(baseDir+"workflowBundle.rdf"));
		UUID bundleUID = UUID.randomUUID();
		newGraph=new AddGraph(workflowBundleGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),store,"Loading workflow bundle graph...",1);
	String pathQuery=prefixes+"SELECT ?profile ?dataflow WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+">{"
			+"?profURI rdf:type scufl2:Profile . ?profURI rdfs:seeAlso ?profile . ?dataflowURI rdf:type scufl2:Workflow . ?dataflowURI rdfs:seeAlso ?dataflow }"
			+"} LIMIT 100";
	newQuery= new Query(pathQuery,store,"Getting paths from Bundle...");
	response= newQuery.getStoreResponse();
	//----get profile filename (It's always in profile sub Folder)
	String profilePathArray[]= response.split("profile/");
	int indexOfEnd=profilePathArray[1].indexOf(".rdf");       
	String profileFileName=profilePathArray[1].substring(0, indexOfEnd);
	profileFileName=profileFileName+".rdf";
	//System.out.println(profileFileName);
	
	//----get dataflow filename (It's always in dataflow sub Folder)
	String dataflowPathArray[]= response.split("workflow/");
	indexOfEnd=dataflowPathArray[1].indexOf(".rdf");
	String dataflowFileName=dataflowPathArray[1].substring(0, indexOfEnd);
	dataflowFileName=dataflowFileName+".rdf";
	//System.out.println(dataflowFileName);
	
	//---Now insert them in the graph
	profileGraph = FileUtils.readFileToString(new File(baseDir+"/profile/"+profileFileName));
	dataflowGraph = FileUtils.readFileToString(new File(baseDir+"/workflow/"+dataflowFileName));
	UUID profileUID = UUID.randomUUID();  //generate UIDs
	UUID dataflowUID = UUID.randomUUID();
	newGraph=new AddGraph(profileGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID),store,"Loading profile graph...",1);
	newGraph=new AddGraph(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID),store,"Loading dataflow graph...",1);
	newGraph=new AddGraph(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),store,"Loading workflowBundle graph...",1);
	//-----And add reference triples in the master graphs
	String profileTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProfileOf> <"+UID+">";
	String dataflowTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isDataflowOf> <"+UID+">";
	String bundleTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=new AppendGraph(profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",store,"Appending triple to master profile graph",2);
		append=new AppendGraph(dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",store,"Appending triple to master dataflow graph",2);
		append=new AppendGraph(bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",store,"Appending triple to master bundle graph",2);
	}
	else{
		newGraph=new AddGraph(profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",store,"Creating new master profile graph",2);
		newGraph=new AddGraph(dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",store,"Creating new master dataflow graph",2);
		newGraph=new AddGraph(bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",store,"Creating new master bundle graph",2);
	}
	
	} catch (IOException e) {
	  e.printStackTrace();
	}
	}
	else{
		System.out.println("WorkflowBundle, profile and dataflow graphs already exist");
	}
}

private static void  findWSDLServicesForEveryProvenanceGraph(Store store, String filePathForCSV){
	//Run the wsdl finding query
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 

	+"GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance> {"
	+	"?provenance myont:isProvenanceOf ?x}"

	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow> {"
	+"	?dataflow myont:isDataflowOf ?x}"


	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile> {"
	+"	?profile myont:isProfileOf ?x}"


	+"	GRAPH ?provenance {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
+"LIMIT 200";
	 Query newQuery=new Query(wsdlQuery,store,"Getting wsdl services...");
	 generateCsvFile(filePathForCSV+"AllGraphCSV.csv",newQuery.getStoreResponse());
	} 

private static void  findWSDLServicesForCurrentProvenanceGraph(Store store,String filePathForCSV){
	//Run the wsdl finding query
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile> {"
	+"	?profile myont:isProfileOf <"+UID+">}"


	+"	GRAPH "+ currentGraphURI+" {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
+"LIMIT 200";
	 Query newQuery=new Query(wsdlQuery,store,"Getting wsdl services...");
	 generateCsvFile(filePathForCSV+"SingleGraphCSV.csv",newQuery.getStoreResponse());
	} 


private static void generateCsvFile(String fileName, String response)
{   response=response.replaceAll("\n", "\t");
	String responseInArray[]=response.split("\t");
	
	try
	{
	    FileWriter writer = new FileWriter(fileName);

	    writer.append("Processor Name");
	    writer.append(',');
	    writer.append("Port Name");
	    writer.append(',');
	    writer.append("Value Binding");
	    writer.append(',');
	    writer.append("Activity Name");
	    writer.append(',');
	    writer.append("WSDL Service URL");
	    writer.append('\n');
	    
	    for (int i=0;i<responseInArray.length;i++){
	    	    	
	    	if(responseInArray[i].contains("^^")){
	    		int indexOfEnd=responseInArray[i].indexOf("^^");
		        String tmp=responseInArray[i].substring(1, indexOfEnd-1);
	    	    writer.append(tmp);	
	    	    if((i+1)%5==0)
	    	    writer.append('\n');
	    	    else
	    	    	writer.append(',');
	    	}
	    	else{
	    	writer.append(responseInArray[i]);
	    	if((i+1)%5==0)
	    	    writer.append('\n');
	    	else
	    		writer.append(',');
	    	}
	    	
	    
	    }
	   
	    //generate whatever data you want

	    writer.flush();
	    writer.close();
	}
	catch(IOException e)
	{
	     e.printStackTrace();
	} 
 }


 private static String parseResponse(String response, String delimiter){
	 response=response.trim();
	 int startchar=response.indexOf(delimiter);
	 int endchar=response.indexOf(delimiter, startchar+1);
	 return response.substring(startchar+1, endchar);
 }

 
 
 
}
