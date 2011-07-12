package uk.ac.manchester.cs.spanoude.semanticprovenance;

import java.io.*;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import sun.misc.BASE64Encoder;

public class ConnectAndPOST {

	
	private InputStream serverResponse = null;
	final String USER_AGENT = "Emmanouil Spanoudakis";
	
	
	public ConnectAndPOST(String urlToConnectTo, String JSONtoPOST) throws IOException{
		
		final StringBuilder text = new StringBuilder();
		String userpassword ="dragonfighter@gmail.com:19851988";
		BASE64Encoder enc = new sun.misc.BASE64Encoder();
		String encodedAuthorization = enc.encode( userpassword.getBytes() );
		try{
		URL url = new URL(urlToConnectTo);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT); 
		conn.setRequestProperty("Content-Type", "application/json"); //x-www-form-urlencoded
		conn.addRequestProperty("Authorization", "Basic "+encodedAuthorization);
     	conn.setRequestProperty("Accept", "application/json");
		
     	conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(JSONtoPOST);
        wr.flush();
     	
     	
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
		
	}
}
