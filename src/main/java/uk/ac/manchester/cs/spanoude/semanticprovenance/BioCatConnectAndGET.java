package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.net.*;
import java.io.*;

public class BioCatConnectAndGET {

private InputStream serverResponse = null;
final String USER_AGENT = "Emmanouil Spanoudakis";

	public BioCatConnectAndGET(String urlToConnectTo) throws IOException{
		URL url = new URL(urlToConnectTo);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", USER_AGENT); 
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
		
		//conn.disconnect(); close connection
		
	}


	public InputStream getServerResponse() {
		return serverResponse;
	}
	
	//public void killConnection(){
	//	conn.disconnect();
	//}
	
	
}
