package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
	 * @param args - use flags detailed in help (type -h or --help for help). Format should be <flag> <argument> where appropriate
	 *              
	 */
	private static String prefixes= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
        +"PREFIX j: <http://purl.org/net/taverna/janus#>"  //in versions earlier than 2.3 it is http://purl.org/taverna/janus#
        +"PREFIX scufl2: <http://ns.taverna.org.uk/2010/scufl2#>"
        +"PREFIX taverna: <http://ns.taverna.org.uk/2010/activity/wsdl/operation#>"
        +"PREFIX myont:<https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#>";
	
	protected static String UID="";
	protected static String pureUID="";
	protected static String currentGraphURI="";
	private static Boolean newGraphInserted=false;
	protected static Boolean createCSVFile=false;
	private static Boolean taskIssued=false;
	private static Boolean tavernaDirSet=false;
	private static Boolean insertProvenanceDataInStore=false;
	private static Boolean noInput=false;         // whether or not  t2flow file is specified as input. Only in the case of -a without arguments this is true
	private static String provenanceOuputDir="";
	private static int resultOption=0;
	protected static String filePathForCSV="";
	private static int querySoftLimit= 400;
	private static String tripleStore="4Store";
	private static Vector<String> supportedTripleStores=new Vector<String>();
	private static String storeResponse="";
	private static String storeLocation="http://localhost:8001";
	private static String classPackage="uk.ac.manchester.cs.spanoude.semanticprovenance.";
	protected static GraphOperationFactory graphOperationFactory= new GraphOperationFactory() ;
	
	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		Query newQuery;
		String scufl2File="";
		String response;
		String tavernaDir="";	
		supportedTripleStores.addElement("4Store");
		//fix getOpt Start
		LongOpt[] longopts = new LongOpt[11];
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("all", LongOpt.OPTIONAL_ARGUMENT, null, 'a');
		longopts[2] = new LongOpt("this", LongOpt.REQUIRED_ARGUMENT, null, 't');
		longopts[3] = new LongOpt("same", LongOpt.REQUIRED_ARGUMENT, null, 's');
		longopts[4] = new LongOpt("provenanceonly", LongOpt.REQUIRED_ARGUMENT, null, 'p');
		longopts[5] = new LongOpt("csvlocation", LongOpt.REQUIRED_ARGUMENT, null, 'c');
		longopts[6] = new LongOpt("tavernadirectory", LongOpt.REQUIRED_ARGUMENT, null, 'd');
		longopts[7] = new LongOpt("softlimit", LongOpt.REQUIRED_ARGUMENT, null, 'l');
		longopts[8] = new LongOpt("triplestorename", LongOpt.REQUIRED_ARGUMENT, null, 'k');
		longopts[9] = new LongOpt("triplestorelocation", LongOpt.REQUIRED_ARGUMENT, null, 'b');
		longopts[10] = new LongOpt("package", LongOpt.REQUIRED_ARGUMENT, null, 'g');
		
		Getopt g = new Getopt("Provenance Processor", args, "a::t:s:p:c:d:l:k:b:g:h",longopts);   
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
		            insertProvenanceDataInStore=true;   //true
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
		        	  System.out.println("For long options, use --Option=argument or --Option if no argument is needed. For short option use -option <space> argument or -option if no argument is needed. -a short option must be written as -a<argument> (no space between -a and argument) if an argument must be specified");
		        	  System.out.println("Long: --all  |  Short: -a<Workflow .t2flow filepath -OPTIONAL- <Workflow variables in the form PORTNAME PORTVALUE if workflow is specified>  NOTE: If you want to specify a workflow to be inserted leave no space. eg. -a/Users/Me/workflow.t2flow. Returns provenance information for all graphs currently present in the triple store. If the optional argument is specified, the workflow is run and inserted in the triple store prior to querying");
		        	  System.out.println("Long: --this |  Short: -t <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE>  The specified workflow is run and inserted in the triple store. Provenance information of the inserted graph only is returned");
		        	  System.out.println("Long: --same |  Short: -s <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE> The specified workflow is run and inserted in the triple store. All provenance information, including past provenance, regarding the specified workflow is returned");
		        	  System.out.println("Long: --provenance  | Short: -p <Workflow .t2flow filepath -REQUIRED-> <Workflow variables in the form PORTNAME PORTVALUE> The specified workflow is run and parsed. All provenance information, including past provenance, regarding the specified workflow is returned. No new data is inserted in the triple store");
		        	  System.out.println("Long: --csvlocation | Short: -c <System directory for .csv file creation -REQUIRED-> A .csv file is created at the specified directory containing the query results of -a, -t, -s or -p flags ");
		        	  System.out.println("Long: --tavernadirectory | Short: -d <Taverna directory -REQUIRED-> System directory of the Taverna program. Necessary");
		        	  System.out.println("Long: --softlimit        | Short: -l <INTEGER -REQUIRED-> Sets Soft Limit for the queries. Default is 200 ");
		        	  System.out.println("Long: --triplestorename  | Short: -k <STRING name of desired triple store -REQUIRED-> Sets the triple store to be used by the application. Default is 4Store. Give the store name, exactly as it appears in your classes eg. AddGraphTo4Store -> -k 4Store ");
		        	  System.out.println("Long: --package          | Short: -g <STRING package name where the AddGraphToX and other classes reside -REQUIRED-> Defines the package for custom classes that will be registered to factory methods. Argument must be in the form: uk.ac.manchester.cs.spanoude.semanticprovenance. After the last .,there should be the Class names. Do NOT include class names in your argument");
		        	  System.out.println("Long: --triplestorelocation | Short: -b <STRING location of triple store . eg: http://localhost:8001  -REQUIRED-> Sets the triple store location to be used by the application. Default is http://localhost:8001 . ");
		        	  System.out.println("Long: --help                | Short: -h returns help information ");
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
                     break;
		        	   
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
		        	  break;
		        	  }
		          case 'b':{
		        	  storeLocation=g.getOptarg();
		        	  System.out.println("Store location set at: "+storeLocation);
		        	  break;
		          }
		          case 'g':{
		        	  classPackage=g.getOptarg();
		        	  System.out.println("class package: "+classPackage);
		        	  break;
		          }
		            //
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		            System.out.print("getopt() returned " + c + "\n");
		       }
		   }
		 //fix getOpt End
		 
		//-----Step 0. Register classes in the respective factories
		 try {
			//Class.forName(classPackage+"AddGraphTo"+tripleStore);
			 graphOperationFactory.registerTripleStoreOperation(tripleStore+"Add",Class.forName(classPackage+"AddGraphTo"+tripleStore));
			 graphOperationFactory.registerTripleStoreOperation(tripleStore+"Append",Class.forName(classPackage+"AppendGraphTo"+tripleStore));
			 graphOperationFactory.registerTripleStoreOperation(tripleStore+"Delete",Class.forName(classPackage+"DeleteGraphFrom"+tripleStore));
			 graphOperationFactory.registerTripleStoreOperation(tripleStore+"Query",Class.forName(classPackage+"Query"+tripleStore));
			 
		 } catch (ClassNotFoundException e1) {
			
			e1.printStackTrace();
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
					loadProvenanceFileInStore(provenanceGraph,true,graphOperationFactory);			
				
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
					loadProfileAndDataflowFiles(unzipDirectory,graphOperationFactory);
				}
				else{
					loadProvenanceFileInStore(provenanceGraph,false,graphOperationFactory);	
				        }
				
		 //Step 2.6 Run wsdl finding query on specified graphs and create .csv files with the results if so specified.	
				switch(resultOption){
				
				case 1:{
					storeResponse= findWSDLServicesForEveryProvenanceGraph(graphOperationFactory);
					 break;
				       }
				case 2:{
					if (newGraphInserted){
						storeResponse=findWSDLServicesForCurrentProvenanceGraph(graphOperationFactory);
					       break;}
					else
						{System.out.println("Graph was not inserted because it is already present in the KB. Returning results from old graph.");
						storeResponse= findWSDLServicesForCurrentProvenanceGraph(graphOperationFactory);
					    
					    break;
						}
				       }
				case 3:{
					storeResponse=findWSDLServicesForCurrentAndAssociatedProvenanceGraphs(graphOperationFactory);
					break;
					   }
				case 4:{
					storeResponse=findWSDLServicesForWorkflowUID(graphOperationFactory);
					break;
				}
				}
		
			//Step 2.7 : Annotate Biocatalogue with: 1)Example data for inputs and outputs	, 2) Example workflows that use the wsdl services identified, found through MyExperiment SPARQL endpoint
				//--Add an abstraction layer here in case someone wants to annotate something else?!
				System.out.println("Annotating Biocatalogue...");
				annotateBioCatalogue(storeResponse);
				
				
			 }
				catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			   }
			   else{
				 findWSDLServicesForEveryProvenanceGraph(graphOperationFactory);
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
		
		for(int i=0;i<args.length;i=i+2){ //i=i+2
			//System.out.println(i+":"+args[i]);
			String input=args[i];
			String value=args[i+1];
			//System.out.println(input+" "+value);
			commandToExecute=commandToExecute+" -inputvalue "+input+" "+value+" ";
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
	
private static void loadProvenanceFileInStore(String graph, Boolean insert,GraphOperationFactory graphOperationFactory ) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
	AddGraph newGraph;
	DeleteGraph delete;
	Query newQuery;	
	String response;
	boolean masterGraphExists;
	//--Check if provenance master graph exists
	String findProvenanceGraphQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> ?o }";
	newQuery=graphOperationFactory.createQuery(tripleStore+"Query",findProvenanceGraphQuery,storeLocation,"Check for master provenance graph",querySoftLimit);
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//insert the provenance file with a temporary URI in order to query it for its UID and workflow run URIs
	String idQuery= prefixes+"SELECT ?idURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?idURI rdf:type j:workflow_spec }} LIMIT 100"; //. ?idURI rdfs:comment ?workflowId if we need the actual id string
	newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",graph,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance",storeLocation,"Loading temporary provenance graph...",1);//new AddGraphTo4Store(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance",storeLocation,"Loading temporary provenance graph...",1);  //add temporary provenance graph
	newQuery= graphOperationFactory.createQuery(tripleStore+"Query",idQuery,storeLocation,"Getting unique workflow id...",querySoftLimit);    //get unique workflow id
	response= newQuery.getStoreResponse();
	//response=parseResponse(response,"\"");           //parse server response to get actual id string
	UID=response.substring(response.indexOf("<")+1, response.indexOf(">")); //maybe should use -1 for end character...
	System.out.println("UID:"+UID);
	pureUID=UID.substring(UID.indexOf("workflow/")+9,UID.lastIndexOf("/"));
	System.out.println("pure:"+pureUID);
	String findWorkflowRunQuery= prefixes+"SELECT ?runURI WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance> { ?runURI rdf:type j:workflow_run }} LIMIT 100";
	newQuery= graphOperationFactory.createQuery(tripleStore+"Query",findWorkflowRunQuery,storeLocation,"Getting workflow run id...",querySoftLimit);    //get unique workflow id
	response=newQuery.getStoreResponse();
	
	//--Delete temporary graph
	delete= graphOperationFactory.createDeleteGraph(tripleStore+"Delete","https://github.com/cyfer/Taverna_Provenance/wiki/tempprovenance",storeLocation,"Deleted temporary graph");   //Delete temporary graph
	
	if(!insert){  //if insert is not true we are only looking for the workflow UID and can now return
	 return;}
	
	//--Check if the provenance file has already been inserted in the triple store 
	String workflowrunIdURI=response.substring(response.indexOf("<"), response.lastIndexOf(">")+1);
	String checkWorkflowRunUniquenessQuery=prefixes+"ASK {"+workflowrunIdURI+" rdf:type j:workflow_run}";
	newQuery= graphOperationFactory.createQuery(tripleStore+"Query",checkWorkflowRunUniquenessQuery,storeLocation,"Checking workflow run id uniqueness...",querySoftLimit);
	response=newQuery.getStoreResponse();
	System.out.println(response);		
	
	//--If workflow run is not found amongst provenance files, insert the graph and references. Otherwise return.
	if(response.contains("false")){    
    UUID provenanceUID = UUID.randomUUID();
    newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",graph,"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID),storeLocation,"Loading finalised provenance graph...",1);//new AddGraphTo4Store(graph,"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID),storeLocation,"Loading finalised provenance graph...",1);  //Re-insert graph, with correct URI based on workflow UID
    System.out.println("Graph: "+"https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+" has been added to KB" );
    currentGraphURI="https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID);
    newGraphInserted=true;
  //-----add reference triples in the master graph
	String provenanceTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/provenance/"+String.valueOf(provenanceUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProvenanceOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=graphOperationFactory.createAppendGraph(tripleStore+"Append", provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",storeLocation,"Appending triple to master provenance graph",2);//(provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",tripleStore,"Appending triple to master provenance graph",2);
			}
	else{
		System.out.println("Creating new master provenance graph");
		newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",provenanceTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance",storeLocation,"Creating new master provenance graph",2);
        	}
	}
	else{
		System.out.println("These provenance data already exist in the triple store!");
		//System.out.println(workflowrunIdURI);
		String getMatchingProvenanceGraph=prefixes+" SELECT ?provenance  WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance>{"
                                          +"?provenance myont:isProvenanceOf <"+UID+"> }"
                                          +" GRAPH ?provenance {"+workflowrunIdURI+" rdf:type j:workflow_run }"
                                          +"} LIMIT 200";

		newQuery=graphOperationFactory.createQuery(tripleStore+"Query",getMatchingProvenanceGraph,storeLocation,"Getting existing graph URI...",querySoftLimit);
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

private static void loadProfileAndDataflowFiles (String baseDir,GraphOperationFactory graphOperationFactory) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
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
	newQuery=graphOperationFactory.createQuery(tripleStore+"Query",findMasterBundleGraphQuery,storeLocation,"Check for master bundle graph",querySoftLimit);
	response=newQuery.getStoreResponse();
	if(response.contains("false"))
		masterGraphExists=false;
	else
		masterGraphExists=true;
	
	//-- Check if a workflowBundle graph exists for the specific provenance file
	String checkBundleExistenceQuery="ASK { ?s <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+"> }";
	newQuery= graphOperationFactory.createQuery(tripleStore+"Query",checkBundleExistenceQuery,storeLocation,"Checking if workflow bundle for the specified provenance file exists...",querySoftLimit);
	if(newQuery.getStoreResponse().contains("false")){
	try {
		workflowBundleGraph = FileUtils.readFileToString(new File(baseDir+"workflowBundle.rdf"));
		UUID bundleUID = UUID.randomUUID();
		newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",workflowBundleGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),storeLocation,"Loading workflow bundle graph...",1);//new AddGraphTo4Store(workflowBundleGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),storeLocation,"Loading workflow bundle graph...",1);
	String pathQuery=prefixes+"SELECT ?profile ?dataflow WHERE { GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+">{"
			+"?profURI rdf:type scufl2:Profile . ?profURI rdfs:seeAlso ?profile . ?dataflowURI rdf:type scufl2:Workflow . ?dataflowURI rdfs:seeAlso ?dataflow }"
			+"} LIMIT 100";
	newQuery= graphOperationFactory.createQuery(tripleStore+"Query",pathQuery,storeLocation,"Getting paths from Bundle...",querySoftLimit);
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
	newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",profileGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID),storeLocation,"Loading profile graph...",1);//new AddGraphTo4Store(profileGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID),storeLocation,"Loading profile graph...",1);
	newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID),storeLocation,"Loading dataflow graph...",1);//new AddGraphTo4Store(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID),storeLocation,"Loading dataflow graph...",1);
	newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),storeLocation,"Loading workflowBundle graph...",1);//new AddGraphTo4Store(dataflowGraph,"https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID),storeLocation,"Loading workflowBundle graph...",1);
	//-----And add reference triples in the master graphs
	String profileTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/profile/"+String.valueOf(profileUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isProfileOf> <"+UID+">";
	String dataflowTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/dataflow/"+String.valueOf(dataflowUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isDataflowOf> <"+UID+">";
	String bundleTriple="<https://github.com/cyfer/Taverna_Provenance/wiki/workflowbundle/"+String.valueOf(bundleUID)+"> <https://github.com/cyfer/Taverna_Provenance/wiki/Ontology#isWorkflowBundleOf> <"+UID+">";
	if(masterGraphExists){
		AppendGraph append=graphOperationFactory.createAppendGraph(tripleStore+"Append",profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",storeLocation,"Appending triple to master profile graph",2);
		append=graphOperationFactory.createAppendGraph(tripleStore+"Append",dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",storeLocation,"Appending triple to master dataflow graph",2);
		append=graphOperationFactory.createAppendGraph(tripleStore+"Append",bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",storeLocation,"Appending triple to master bundle graph",2);
	}
	else{
		newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",storeLocation,"Creating new master profile graph",2);//new AddGraphTo4Store(profileTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile",storeLocation,"Creating new master profile graph",2);
		newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",storeLocation,"Creating new master dataflow graph",2);//new AddGraphTo4Store(dataflowTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterdataflow",storeLocation,"Creating new master dataflow graph",2);
		newGraph=graphOperationFactory.createAddGraph(tripleStore+"Add",bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",storeLocation,"Creating new master bundle graph",2);//new AddGraphTo4Store(bundleTriple,"https://github.com/cyfer/Taverna_Provenance/wiki/masterbundle",storeLocation,"Creating new master bundle graph",2);
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

protected static String  findWSDLServicesForEveryProvenanceGraph(GraphOperationFactory graphOperationFactory) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
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
	 Query newQuery=graphOperationFactory.createQuery(tripleStore+"Query",wsdlQuery,storeLocation,"Getting wsdl services for all graphs...",querySoftLimit);
	 
	 if(createCSVFile){
	 generateCsvFile(filePathForCSV+"AllGraphCSV.csv",newQuery.getStoreResponse());
	 System.out.println("CSV file: AllGraphCSV.csv has been created in directory "+filePathForCSV);
	 }
	 return newQuery.getStoreResponse();
	} 

protected static String  findWSDLServicesForCurrentProvenanceGraph(GraphOperationFactory graphOperationFactory) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	//Run the wsdl finding query
	 
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"	GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprofile> {"
	+"	?profile myont:isProfileOf <"+UID+">}"


	+"	GRAPH <"+ currentGraphURI+"> {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
	+"LIMIT"+querySoftLimit;
	
	 Query newQuery=graphOperationFactory.createQuery(tripleStore+"Query",wsdlQuery,storeLocation,"Getting wsdl services for current graph...",querySoftLimit);
	 	 if(createCSVFile){
		 generateCsvFile(filePathForCSV+"SingleGraphCSV.csv",newQuery.getStoreResponse());
		 System.out.println("CSV file: SingleGraphCSV.csv has been created in directory "+filePathForCSV);
		 }
	 	 
	 	 
	 return newQuery.getStoreResponse();
	} 

protected static String findWSDLServicesForCurrentAndAssociatedProvenanceGraphs(GraphOperationFactory graphOperationFactory) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
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
	 Query newQuery=graphOperationFactory.createQuery(tripleStore+"Query",wsdlQuery,storeLocation,"Getting wsdl services for current graph and all associated graphs...",querySoftLimit);
	 if(createCSVFile){
		 generateCsvFile(filePathForCSV+"CurrentAndAssociatedCSV.csv",newQuery.getStoreResponse());
		 System.out.println("CSV file: CurrentAndAssociatedCSV.csv has been created in directory "+filePathForCSV);
		 }
	 
	 
	 return newQuery.getStoreResponse();
	}

protected static String findWSDLServicesForWorkflowUID(GraphOperationFactory graphOperationFactory) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	//Run the wsdl finding query
	String wsdlQuery=prefixes+" SELECT ?processor ?portname ?valuebinding ?activityname ?wsdlURL WHERE {" 
	
	+"GRAPH <https://github.com/cyfer/Taverna_Provenance/wiki/masterprovenance> {"
	+	"?provenance myont:isProvenanceOf <"+UID+">}"


	+"	GRAPH ?provenance {?procURI j:has_processor_type ?type . FILTER regex(str(?type), \"WSDLActivity\") . ?procURI rdfs:comment ?processor . ?procURI <http://knoesis.wright.edu/provenir/provenir.owl#has_parameter> ?param . ?param rdf:type j:port . ?param rdfs:comment ?portname . ?param j:has_value_binding ?valueURI . ?valueURI rdfs:comment ?valuebinding"
	+"	} "

	+"	GRAPH ?profile { ?procbindingURI  scufl2:bindActivity ?activityURI . ?procbindingURI scufl2:name ?name . FILTER (str(?processor)=?name). ?configURI scufl2:configure ?activityURI . ?configURI <http://ns.taverna.org.uk/2010/activity/wsdl#operation> ?bnode . ?bnode taverna:name ?activityname . ?bnode taverna:wsdl ?wsdlURL}"
	+"	}"
	+"LIMIT"+querySoftLimit;
	 Query newQuery=graphOperationFactory.createQuery(tripleStore+"Query",wsdlQuery,storeLocation,"Getting wsdl services for specified workflow...",querySoftLimit);
	 
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

protected static List<ResultBindings> parseQueryResults(String response){
	response=response.replaceAll("\n", "\t");
	String responseInArray[]=response.split("\t");
	List<ResultBindings> resultBindings=new  Vector<ResultBindings>();
	
	
	for (int i=5;i<responseInArray.length;i++){
    	
    	if(responseInArray[i].contains("^^")){
    		int indexOfEnd=responseInArray[i].indexOf("^^");
	        String tmp=responseInArray[i].substring(1, indexOfEnd-1);
	        responseInArray[i]=tmp;
    	}
    	
	}
	
	//System.out.println("Array length"+responseInArray.length);
	for (int i=5;i<responseInArray.length;i++){
		
		if((i+4)%5==0){      // get port and value bindings and put them in a vector
			resultBindings.add(new ResultBindings(responseInArray[i],responseInArray[i+1],responseInArray[i+2],responseInArray[i+3]));
		    
		}
		
	}
	/**
	for(ResultBindings entry:resultBindings){
		System.out.println("Port:"+entry.getPort()+" \n Value:"+entry.getValue()+" \n Service:"+entry.getService()+" \n url:"+entry.getServiceURL() );
	}
	*/
	
	return resultBindings;
    
	
}

protected static void annotateBioCatalogue(String storeResponse){
	try {
		String myExperimentQuery= "PREFIX mecomp: <http://rdf.myexperiment.org/ontologies/components/>"
	        +"PREFIX dcterms: <http://purl.org/dc/terms/>"
	        +"PREFIX mebase: <http://rdf.myexperiment.org/ontologies/base/>"
	        +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"

	       +"SELECT ?s WHERE {"
	       +"?s dcterms:identifier ?o .  FILTER (regex(str(?o), '"+pureUID+"'))"    //example : bb9ce24e-4a54-4111-a4fe-a55d0e80ff95
	       +"} LIMIT 200";
		MyExperimentConnection testMyExp=new MyExperimentConnection(myExperimentQuery);
		BioCatConnection connection=new BioCatConnection(parseQueryResults(storeResponse),testMyExp.getExampleWorkflows());
	} catch (IOException e) {
		
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
