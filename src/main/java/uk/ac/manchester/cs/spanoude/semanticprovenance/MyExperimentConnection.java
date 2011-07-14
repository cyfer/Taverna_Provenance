package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class MyExperimentConnection {
	private static InputStream serverResponse = null;
	private Vector<String> results= new Vector<String>() ;
	
	public MyExperimentConnection(String query) throws UnsupportedEncodingException{
		String urlToConnectTo = "http://rdf.myexperiment.org/sparql?query=";
		URLEncoder encoder = null;
	    String UrlParameters=encoder.encode(query,"UTF-8");
	    urlToConnectTo=urlToConnectTo+UrlParameters+"&formatting=CSV";
	    
		final StringBuilder text = new StringBuilder();
		try{
			URL url = new URL(urlToConnectTo);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Emmanouil"); 
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //x-www-form-urlencoded
			conn.setRequestProperty("Accept", "application/xml");
			int iResponseCode = conn.getResponseCode(); 
			
			switch (iResponseCode) {
			case HttpURLConnection.HTTP_OK: // regular operation Ñ can directly use the response data input stream 
				{serverResponse = conn.getInputStream();
				break;}
			case HttpURLConnection.HTTP_BAD_REQUEST: // this was a bad XML request - need full XML response to read the error // message from that the server has provided (if any); // Java throws IOException if getInputStream() is used when non HTTP_OK // response code was received - // hence can use getErrorStream() straight away to fetch the error document 
				{serverResponse = conn.getErrorStream();
				break;}
			case HttpURLConnection.HTTP_UNAUTHORIZED: // this content is not authorised for current user Ð i.e. the application // may need to prompt the current user to provide their login credentials /* decide what to do here */
			    {System.out.println("nothing");
			    break;}
			case HttpURLConnection.HTTP_NOT_FOUND: // nothing was found at the provided URL Ð may try to use error stream // (or just do nothing) 
				{serverResponse = conn.getErrorStream();
				break;}
			default: // unexpected response code - raise an exception 
				throw new IOException("Received unexpected HTTP response code (" +
						iResponseCode + ") while fetching data at " );
		    }
			
			}
		    catch (Exception e) {
			e.printStackTrace();
		    } 
		   
		  //Get the server response and print it
			
			try { 
				BufferedReader br = new BufferedReader(new InputStreamReader(serverResponse)); String str = "";
			
			while ((str = br.readLine()) != null) { 
				text.append(str + "\n");
			}
			
			br.close();
			} 
			catch (Exception e) {
			System.err.println("An error has occurred, details:\n" + e.getMessage());
			}
				
			System.out.println(text.toString());
			parseResultsToList(text.toString());
	}
	
	private void parseResultsToList(String response){
		
		String responseInArray[]=response.split(",");
		for (int i=0;i<responseInArray.length;i++){
						
			if(!responseInArray[i].equals(null)){
				System.out.println(i+":"+responseInArray[i]);
				
				results.addElement(responseInArray[i]);
				
			}
			
		}
		

		
	}
	
	public Vector<String> getExampleWorkflows(){
		return results;
	}
}
