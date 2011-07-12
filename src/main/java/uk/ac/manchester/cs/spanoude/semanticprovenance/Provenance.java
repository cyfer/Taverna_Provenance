package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import gnu.getopt.*;
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
	 * @param args - args[0]= Mode selector.Options:
	 *               1) this : Returns wsdl service information for the graph being inserted only
	 *               2) all : Returns wsdl service information for all graphs in the KB
	 *               3) allsame : Returns wsdl service information for graphs in the KB that are associated with the workflow being inserted only. This essentially means all provenance information regarding the workflow being inserted.
	 *               4) provonly : No new graph insertion. Returns all provenance information found in the KB regarding a workflow unique URI, that is supplied in args[1] instead .
	 *               args[1]= filepath to Taverna folder eg. /Users/dragonfighter/Documents/Master_Thesis/tools/taverna-nightly-2.3-SNAPSHOT-20110527/  .ALTERNATIVELY : Workflow unique URI in case of args[0]="provonly".
	 *               args[2]= workflow file (.t2flow format) that is converted into .scufl2 format and then extracted. The scufl2 file contains
	 *               the Profile and dataflow files that are inserted in the KB. ALTERNATIVELY: In case of args[0]="provonly", this argument contains the directory specified by the user where the .csv files will be created.
	 *               args[3]= The directory specified by the user where the .csv files will be created
	 *               args[4]-args[n]= Any arguments that may be needed for the workflow to run. Please follow the format: <Input Port> <Input Value> ommiting <>
	 *               Take care to spell input ports exactly as they appear in the workflow 
	 */
	private static String prefixes= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
        +"PREFIX j: <http://purl.org/taverna/janus#>"
        +"PREFIX scufl2: <http://ns.taverna.org.uk/2010/scufl2#>"
        +"PREFIX taverna: <http://ns.taverna.org.uk/2010/activity/wsdl/operation#>"
        +"PREFIX myont:<https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#>";
	
	protected static String UID="";
	protected static String currentGraphURI="";
	private static Boolean newGraphInserted=false;
	private static Boolean createCSVFile=false;
	private static Boolean taskIssued=false;
	private static Boolean tavernaDirSet=false;
	private static Boolean insertProvenanceDataInStore=false;
	private static Boolean noInput=false;         // whether or not  t2flow file is specified as input. Only in the case of -a without arguments this is true
	private static String provenanceOuputDir="";
	private static int resultOption=0;
	private static String filePathForCSV="";
	private static int querySoftLimit= 200;
	private static String tripleStore="4store";
	private static Vector<String> supportedTripleStores=new Vector<String>();
	
	public static void main(String[] args) {
		
		Query newQuery;
		String scufl2File="";
		String response;
		String tavernaDir="";	
		supportedTripleStores.addElement("4store");
		
		
		Getopt g = new Getopt("Provenance Processor", args, "a::t:s:p:c:d:l:k:h");   
		 //
		 int c;
		 String t2FlowLocation="";
		 String portBindings[];
		 while ((c = g.getopt()) != -1)
		   {
		     switch(c)
		       {
		          case 'a':{
		        	  if (taskIssued){
		        		  System.out.println("-a, -t, -s, or -p flags have been specified already. This -a flag wont be executed."); 
		        		  break;
		        	  }
		        	  System.out.println("Running query on all available data from KB. ");
		        	  t2FlowLocation = g.getOptarg();
		        	  if(t2FlowLocation != null){
		        	  System.out.println("Preparing to run specified workflow and insert provenance data into triple store");
		        	  taskIssued=true;
		        	  insertProvenanceDataInStore=true;
		        	  noInput=false;
		        	  resultOption=1;
		        	 
		        	  }
		        	  else{
		        		  noInput=true;
		        		  taskIssued=true;
			        	  insertProvenanceDataInStore=false;
		        	  }
		        		  
		        		  
		        	  break;
		        	}
		         
		          case 't':{
		        	  if (taskIssued){
		        		  System.out.println("-a, -t, -s, or -p flags have been specified already. This -t flag wont be executed."); 
		        		  break;
		        	  }
		            System.out.println("Preparing to run specified workflow, insert data into triple store. Query will be run on just inserted graph");
		            t2FlowLocation = g.getOptarg();
		            System.out.println (t2FlowLocation);
		            taskIssued=true;
		            insertProvenanceDataInStore=true;
		            noInput=false;
		            resultOption=2;
		            break;
		          }
		         
		          case 's':{
		        	  if (taskIssued){
		        		  System.out.println("-a, -t, -s, or -p flags have been specified already. This -s flag wont be executed."); 
		        		  taskIssued=true;
		        		  break;
		        	  }
		        	  System.out.println("Preparing to run specified workflow, insert data into triple store. Query will be run on all provenance information matching the specified workflow");
		        	  t2FlowLocation = g.getOptarg();
		        	  System.out.println (t2FlowLocation); 
		        	  taskIssued=true;
		        	  insertProvenanceDataInStore=true;
		        	  noInput=false;
		        	  resultOption=3;
		        	  break;
		          }
		          
		          case 'p':{
		        	  if (taskIssued){
		        		  System.out.println("-a, -t, -s, or -p flags have been specified already. This -p flag wont be executed."); 
		        		  break;
		        	  }
		        	  System.out.println("Parsing specified workflow and running query. No data will be inserted in the triple store");
		        	  t2FlowLocation = g.getOptarg();
		        	  System.out.println (t2FlowLocation); 
		        	  taskIssued=true;
		        	  insertProvenanceDataInStore=false;
		        	  noInput=false;
		        	  resultOption=4;
		        	  break;
		          }
			            
		          case 'h':{
		        	  System.out.println("-a<Workflow .t2flow filepath -OPTIONAL- <Workflow variables in the form PORTNAME PORTVALUE if workflow is specified>  NOTE: If you want to specify a workflow to be inserted leave no space. eg. -a/Users/Me/workflow.t2flow. Returns provenance information for all graphs currently present in the triple store. If the optional argument is specified, the workflow is run and inserted in the triple store prior to querying");
		        	  System.out.println("-t <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE>  The specified workflow is run and inserted in the triple store. Provenance information of the inserted graph only is returned");
		        	  System.out.println("-s <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE> The specified workflow is run and inserted in the triple store. All provenance information regarding the specified workflow is returned");
		        	  System.out.println("-p <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE> The specified workflow is run and parsed. All provenance information regarding the specified workflow is returned. No new data is inserted in the triple store");
		        	  System.out.println("-c <System directory for .csv file creation -REQUIRED-> A .csv file is created at the specified directory containing the query results of -a, -t, -s or -p flags ");
		        	  System.out.println("-d <Taverna directory -REQUIRED-> System directory of the Taverna program. Necessary");
		        	  System.out.println("-l <INTEGER -REQUIRED-> Sets Soft Limit for the queries. Default is 200 ");
		        	  System.out.println("-k <STRING name of desired triple store -REQUIRED-> Sets the triple store to be used by the application. Default is 4store. ");
		        	  System.out.println("-h returns help information ");
		        	  break;
		          }
		          case 'd':{
		        	  tavernaDir= g.getOptarg();
		        	  tavernaDirSet=true;
		        	  break;		        	  
		          }
		          
		          case 'c':{
		        	  filePathForCSV= g.getOptarg();
		        	  System.out.println("A csv file will be generated at :"+filePathForCSV); 
		        	  createCSVFile=true;
		          }
		          case 'l':{
		        	  try{
		        	        Integer.parseInt(g.getOptarg());
		        	        if(Integer.parseInt(g.getOptarg())>0)
		        	        querySoftLimit=Integer.parseInt(g.getOptarg());
		        	        //user inserted an integer
		        	    }
		        	    catch(NumberFormatException e){
		        	       //user inserted something else
		        	    }

		        	   
		          }
		          case 'k':{
		        	  if (supportedTripleStores.contains(g.getOptarg()))
		        	   tripleStore=g.getOptarg();
		        	  else{
		        		  System.out.println("This triple store is not supported by the application. Supported triple stores are:");
		        	      for(String entry:supportedTripleStores){
		        	    	  System.out.println(entry);
		        	      }
		        	  }
		        	  }
		            //
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		            System.out.print("getopt() returned " + c + "\n");
		       }
		   }
		//-----Step 1 Get non-option arguments and put them in an array ----------
		 String portArgs[]=new String[args.length-g.getOptind()];
		 int j=0;
		 for (int i = g.getOptind(); i < args.length ; i++){
			//System.out.println(portArgs);
			portArgs[j]=args[i];
			j++;
		 }
		 
		 //-----Step 2 Check flags and proceed with program execution ----------
		  if (tavernaDirSet){
			   if (!noInput){
			  //Step 2.1 Run workflow
			System.out.println (t2FlowLocation);
			   runWorkflow(tavernaDir,portArgs, t2FlowLocation);
			   
			  //Step 2.2 Load provenance file in triple store
				try {
					System.out.println("Starting Loading procedure...");
					//if(tripleStore.equals("4store"))
										
					String provenanceGraph= FileUtils.readFileToString(new File(provenanceOuputDir+"/provenance-janus.rdf"), "utf-8");						
					
				if(insertProvenanceDataInStore){
					loadProvenanceFileInStore(provenanceGraph,true);			
				
			 //Step 2.3 Create Scufl2 file out of workflow file
					System.out.println("Creating Scufl2 file...");
					try {
					  scufl2File=createScufl2File(t2FlowLocation);
					} catch (JAXBException e) {
						e.printStackTrace();
					} catch (ReaderException e) {
						e.printStackTrace();
					} catch (WriterException e) {
						e.printStackTrace();
					}			                  
		    //Step 2.4. Unzip Scufl2 file and create profile and dataflow rdf files   
					String unzipDirectory=unzipScufl2File(t2FlowLocation);             // check check
					
		   //Step 2.5 Load profile, dataflow and bundle files in the triple store	
					loadProfileAndDataflowFiles(unzipDirectory);
				}
				else{
					loadProvenanceFileInStore(provenanceGraph,false);	
				        }
				
		 //Step 2.6 Run wsdl finding query on specified graphs and create .csv files with the results if so specified.	
				switch(resultOption){
				
				case 1:{
					 findWSDLServicesForEveryProvenanceGraph();
					 break;
				       }
				case 2:{
					if (newGraphInserted)
						findWSDLServicesForCurrentProvenanceGraph();
					else
						System.out.println("Graph was not inserted because it is already present in the KB. Returning results from old graph.");
					    findWSDLServicesForCurrentProvenanceGraph();
					    
					    break;
				       }
				case 3:{
					findWSDLServicesForCurrentAndAssociatedProvenanceGraphs();
					break;
					   }
				case 4:{
					findWSDLServicesForWorkflowUID();
					break;
				}
				}
				
					
			 }
				catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			   }
			   else{
				 findWSDLServicesForEveryProvenanceGraph();
				   }
		  			   
	    	  }
		 else 
		    System.out.println("You have not specified a Taverna program folder. Do so by using -d <Taverna directory> and rerun the program");
		 
		 
			 
			 
	
			
		}
	
	
protected static String	runWorkflow(String tavernaDir, String args[], String workflowDir){
	try {
		System.out.println("Running workflow...");
		String ls_str;
		String commandToExecute="sh "+tavernaDir+"executeworkflow.sh -embedded " +workflowDir;
		
		for(int i=0;i<args.length;i=i+2){
			commandToExecute=commandToExecute+" -inputvalue "+args[i]+" "+args[i+1]+" ";
		}
		System.out.println(commandToExecute+" -janus");
		
        Process ls_proc = Runtime.getRuntime().exec(commandToExecute+" -janus"); //+workflowDir
        BufferedReader ls_in= new BufferedReader(new InputStreamReader(ls_proc.getInputStream()));
		try {
		while ((ls_str = ls_in.readLine()) != null) {
		System.out.println(ls_str);
		if (ls_str.contains("Outputs will be saved to the directory:"))
			provenanceOuputDir=ls_str.substring(ls_str.indexOf(":")+2);
		System.out.println("Workflow execution successful");
			}
		} catch (IOException e) {
		System.exit(0);
		}
		} catch (IOException e1) {
		System.err.println(e1);
		System.exit(1);
		}
		return "Success";

}
	
private static void loadProvenanceFileInStore(String graph, Boolean insert){
	AddGraph newGraph;
	DeleteGraph delete;
	Query newQuery;	
	String response;
	boolean masterGraphExists;
	//--Check if provenance master graph exists
	String findProvenanceGraphQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> ?o }";
	newQuery=new Query(findProvenanceGraphQuery,tripleStore,"Check for master provenance graph");
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//insert the provenance file with a temporary URI in order to query it for its UID and workflow run URIs
	String idQuery= prefixes+"SELECT ?idURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?idURI rdf:type j:workflow_spec }} LIMIT 100"; //. ?idURI rdfs:comment ?workflowId if we need the actual id string
	newGraph=new AddGraph(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance",tripleStore,"Loading temporary provenance graph...",1);  //add temporary provenance graph
	newQuery= new Query(idQuery,tripleStore,"Getting unique workflow id...");    //get unique workflow id
	response= newQuery.getStoreResponse();
	//response=parseResponse(response,"\"");           //parse server response to get actual id string
	UID=response.substring(response.indexOf("<")+1, response.indexOf(">"));
	System.out.println("UID:"+UID);
	String findWorkflowRunQuery= prefixes+"SELECT ?runURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?runURI rdf:type j:workflow_run }} LIMIT 100";
	newQuery= new Query(findWorkflowRunQuery,tripleStore,"Getting workflow run id...");    //get unique workflow id
	response=newQuery.getStoreResponse();
	
	//--Delete temporary graph
	delete= new DeleteGraph(tripleStore,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance","Deleted temporary graph");   //Delete temporary graph
	
	if(!insert){  //if insert is not true we are only looking for the workflow UID and can now return
	 return;}
	
	//--Check if the provenance file has already been inserted in the triple store 
	String workflowrunIdURI=response.substring(response.indexOf("<"), response.lastIndexOf(">")+1);
	String checkWorkflowRunUniquenessQuery=prefixes+"ASK {"+workflowrunIdURI+" rdf:type j:workflow_run}";
	newQuery= new Query(checkWorkflowRunUniquenessQuery,tripleStore,"Checking workflow run id uniqueness...");
	response=newQuery.getStoreResponse();
	System.out.println(response);		
	
	//--If workflow run is not found amongst provenance files, insert the graph and references. Otherwise return.
	if(response.contains("false")){    
    UUID provenanceUID = UUID.randomUUID();
    newGraph=new AddGraph(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID),tripleStore,"Loading finalised provenance graph...",1);  //Re-insert graph, with correct URI based on workflow UID
    System.out.println("Graph: "+"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+" has been added to KB" );
    currentGraphURI="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID);
    newGraphInserted=true;
  //-----add reference triples in the master graph
	String provenanceTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=new AppendGraph(provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",tripleStore,"Appending triple to master provenance graph",2);
			}
	else{
		System.out.println("Creating new master provenance graph");
		newGraph=new AddGraph(provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",tripleStore,"Creating new master provenance graph",2);
        	}
	}
	else{
		System.out.println("These provenance data already exist in the triple store!");
		//System.out.println(workflowrunIdURI);
		String getMatchingProvenanceGraph=prefixes+" SELECT ?provenance  WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance>{"
                                          +"?provenance myont:isProvenanceOf <"+UID+"> }"
                                          +" GRAPH ?provenance {"+workflowrunIdURI+" rdf:type j:workflow_run }"
                                          +"} LIMIT 200";

		newQuery= new Query(getMatchingProvenanceGraph,tripleStore,"Getting existing graph URI...");
		currentGraphURI=newQuery.getStoreResponse();
		currentGraphURI=currentGraphURI.substring(currentGraphURI.indexOf("<")+1, currentGraphURI.lastIndexOf(">")+1);
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

private static void loadProfileAndDataflowFiles (String baseDir){
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
	newQuery=new Query(findMasterBundleGraphQuery,tripleStore,"Check for master bundle graph");
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//-- Check if a workflowBundle graph exists for the specific provenance file
	String checkBundleExistenceQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+"> }";
	newQuery= new Query(checkBundleExistenceQuery,tripleStore,"Checking if workflow bundle for the specified provenance file exists...");
	if(newQuery.getStoreResponse().contains("false")){
	try {
		workflowBundleGraph = FileUtils.readFileToString(new File(baseDir+"workflowBundle.rdf"));
		UUID bundleUID = UUID.randomUUID();
		newGraph=new AddGraph(workflowBundleGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),tripleStore,"Loading workflow bundle graph...",1);
	String pathQuery=prefixes+"SELECT ?profile ?dataflow WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+">{"
			+"?profURI rdf:type scufl2:Profile . ?profURI rdfs:seeAlso ?profile . ?dataflowURI rdf:type scufl2:Workflow . ?dataflowURI rdfs:seeAlso ?dataflow }"
			+"} LIMIT 100";
	newQuery= new Query(pathQuery,tripleStore,"Getting paths from Bundle...");
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
	newGraph=new AddGraph(profileGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID),tripleStore,"Loading profile graph...",1);
	newGraph=new AddGraph(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID),tripleStore,"Loading dataflow graph...",1);
	newGraph=new AddGraph(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),tripleStore,"Loading workflowBundle graph...",1);
	//-----And add reference triples in the master graphs
	String profileTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProfileOf> <"+UID+">";
	String dataflowTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isDataflowOf> <"+UID+">";
	String bundleTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=new AppendGraph(profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",tripleStore,"Appending triple to master profile graph",2);
		append=new AppendGraph(dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",tripleStore,"Appending triple to master dataflow graph",2);
		append=new AppendGraph(bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",tripleStore,"Appending triple to master bundle graph",2);
	}
	else{
		newGraph=new AddGraph(profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",tripleStore,"Creating new master profile graph",2);
		newGraph=new AddGraph(dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",tripleStore,"Creating new master dataflow graph",2);
		newGraph=new AddGraph(bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",tripleStore,"Creating new master bundle graph",2);
	}
	
	} catch (IOException e) {
	  e.printStackTrace();
	}
	}
	else{
		System.out.println("WorkflowBundle, profile and dataflow graphs already exist");
	}
}

// ------------- Provenance finding methods ----------------------------

protected static String  findWSDLServicesForEveryProvenanceGraph(){
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
+"LIMIT"+querySoftLimit;
	 Query newQuery=new Query(wsdlQuery,tripleStore,"Getting wsdl services for all graphs...");
	 
	 if(createCSVFile){
	 generateCsvFile(filePathForCSV+"AllGraphCSV.csv",newQuery.getStoreResponse());
	 System.out.println("CSV file: AllGraphCSV.csv has been created in directory "+filePathForCSV);
	 }
	 return newQuery.getStoreResponse();
	} 

protected static String  findWSDLServicesForCurrentProvenanceGraph(){
	//Run the wsdl finding query
	 
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile> {"
	+"	?profile myont:isProfileOf <"+UID+">}"


	+"	GRAPH <"+ currentGraphURI+"> {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
	+"LIMIT"+querySoftLimit;
	
	 Query newQuery=new Query(wsdlQuery,tripleStore,"Getting wsdl services for current graph...");
	 	 if(createCSVFile){
		 generateCsvFile(filePathForCSV+"SingleGraphCSV.csv",newQuery.getStoreResponse());
		 System.out.println("CSV file: SingleGraphCSV.csv has been created in directory "+filePathForCSV);
		 }
	 return newQuery.getStoreResponse();
	} 

protected static String findWSDLServicesForCurrentAndAssociatedProvenanceGraphs(){
	//Run the wsdl finding query
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance> {"
	+	"?provenance myont:isProvenanceOf <"+UID+">}"
	
	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile> {"
	+"	?profile myont:isProfileOf <"+UID+">}"

	+"	GRAPH ?provenance {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
	+"LIMIT"+querySoftLimit;
	 Query newQuery=new Query(wsdlQuery,tripleStore,"Getting wsdl services for current graph and all associated graphs...");
	 if(createCSVFile){
		 generateCsvFile(filePathForCSV+"CurrentAndAssociatedCSV.csv",newQuery.getStoreResponse());
		 System.out.println("CSV file: CurrentAndAssociatedCSV.csv has been created in directory "+filePathForCSV);
		 }
	 return newQuery.getStoreResponse();
	}

protected static String findWSDLServicesForWorkflowUID(){
	//Run the wsdl finding query
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance> {"
	+	"?provenance myont:isProvenanceOf <"+UID+">}"


	+"	GRAPH ?provenance {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
	+"LIMIT"+querySoftLimit;
	 Query newQuery=new Query(wsdlQuery,tripleStore,"Getting wsdl services for specified workflow...");
	 if(createCSVFile){
		 generateCsvFile(filePathForCSV+"WorkflowSpecificProvenanceCSV.csv",newQuery.getStoreResponse());
		 System.out.println("CSV file: WorkflowSpecificProvenanceCSV.csv has been created in directory "+filePathForCSV);
		 }
	 return newQuery.getStoreResponse();
	}
//------------- Provenance finding methods ----------------------------



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
	    
	    for (int i=5;i<responseInArray.length;i++){
	    	    	
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


protected static String parseResponse(String response, String delimiter){
	 response=response.trim();
	 int startchar=response.indexOf(delimiter);
	 int endchar=response.indexOf(delimiter, startchar+1);
	 return response.substring(startchar+1, endchar);
 }

 
 
 
}
