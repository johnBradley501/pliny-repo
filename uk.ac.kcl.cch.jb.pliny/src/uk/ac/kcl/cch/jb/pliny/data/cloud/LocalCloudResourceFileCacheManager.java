package uk.ac.kcl.cch.jb.pliny.data.cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//import org.apache.http.HttpResponse; 
//import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.runtime.Plugin;
import org.json.JSONArray;
import org.json.JSONException;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.rdb.LocalResourceFileCacheManager;

public class LocalCloudResourceFileCacheManager extends LocalResourceFileCacheManager {
	
	private String cloudCacheName;
	private CloudServices services;
	
	private static File cacheArea = null;
	

	// https://developers.google.com/appengine/docs/java/blobstore/
	
	public LocalCloudResourceFileCacheManager(CloudServices services, Plugin owner, String cacheName, String myType){
		super(owner);
		this.services = services;
		cloudCacheName = owner.getBundle().getSymbolicName()+"/"+cacheName;
		if(cacheArea == null)cacheArea = getTemporaryCacheArea();
		this.setupTemporaryCacheArea(cacheArea, cacheName, myType);
	}
	
	// advice about temporary file directory:
	// http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
	
	private File getTemporaryCacheArea() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"), "plinyCache");
	    File ownerDataFile = new File(baseDir, "owner.txt");
	    Properties myProperties = new Properties();
	    myProperties.put("user", services.getUserID());
	    myProperties.put("url", services.getCloudServiceURL().toExternalForm());
	    boolean saveDataFile = true;
	    try {
		   if(!baseDir.exists()){
		      baseDir.mkdirs();
		   } else {
			   if(ownerDataFile.exists()){
			      FileInputStream in = new FileInputStream(ownerDataFile);
			      Properties oldProps = new Properties();
			      oldProps.load(in);
			      in.close();
			      if(myProperties.get("user").equals(oldProps.get("user")) &&
			    		  myProperties.get("url").equals(oldProps.get("url")))
			    	  saveDataFile = false;
			      else emptyDirectory(baseDir);
			   } else emptyDirectory(baseDir);
		   }
		   if(saveDataFile){
			  //ownerDataFile = new File(baseDir, "owner.txt");
		      FileWriter out = new FileWriter(ownerDataFile);
		      myProperties.store(out,null);
		      out.close();
		   }
	    } catch (Exception e){
	    	e.printStackTrace();
	    	throw new RuntimeException("Failure setting up temporary file cache space: "+e.getMessage());
	    }
		return baseDir;
	}

	private void emptyDirectory(File baseDir) throws Exception{
		File[] files = baseDir.listFiles();
        for(int i = 0; i < files.length; i++){
        	if(files[i].isDirectory())emptyDirectory(files[i]);
        	files[i].delete();
        }
	}

	private String getSubmissionURL(){
		JSONArray rslts;
		try {
			rslts = services.retrieveData("/startBlob.jsp");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("sendFileToBackingCache (1): malformed URL");
		}
		try {
			if(!rslts.getString(0).equals("ok"))
				throw new RuntimeException("startBlob fails: "+rslts.getString(1));
			return rslts.getString(1);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("sendFileToBackingCache (1): JSONException");
		}
	}
	
	protected void sendFileToBackingCache(File theFile, int cacheNo){
		String submitURL = getSubmissionURL();
		sendDataToBackingCache(submitURL,theFile,cacheNo);
	}
	
	// http://indiwiz.com/2009/02/11/multi-part-content-upload-in-apache-http-components-http-client/

	private void sendDataToBackingCache(String submitURL, File theFile, int cacheNo) {
		
		HttpPost method = new HttpPost(submitURL);
		MultipartEntity entity = new MultipartEntity();
		try {
			entity.addPart("cache", new StringBody(cloudCacheName, Charset.forName("UTF-8")));
			String fileName = theFile.getName();
			entity.addPart("fileName", new StringBody(fileName, Charset.forName("UTF-8")));
			String cacheNoAsString = Integer.toString(cacheNo);
			entity.addPart("cacheNo", new StringBody(cacheNoAsString, Charset.forName("UTF-8")));
			entity.addPart("type", new StringBody(getMyType(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("sendDataToBackingCache fails: "+e.getLocalizedMessage());
		}
		FileBody fileBody = new FileBody(theFile);
		entity.addPart("theFile", fileBody);
		method.setEntity(entity);
		
		try {
			HttpResponse response = services.runHttpPost(method);
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(
			        		response.getEntity().getContent()));
			JSONArray rslts = services.getJSONResults(in);
			if(rslts.getString(0).equals("ok")) return;
			throw new RuntimeException("sendDataToBackingCache fails (BlobUploadServlet): "+rslts.getString(1));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException("sendDataToBackingCache fails (ClientProtocolException): "+e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("sendDataToBackingCache fails (IOException): "+e.getLocalizedMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("sendDataToBackingCache fails (JSONException): "+e.getLocalizedMessage());
		}
	}
	
	protected void getFileFromBackingCache(File theFile, int cacheNo){
		JSONArray parameters = new JSONArray();
		parameters.put("blob");
		parameters.put(cloudCacheName);
		parameters.put(cacheNo);
		parameters.put(getMyType());
		
		fetchFile("/fetchBlob", parameters, theFile);
	}
	
	protected void getThumbnailFromBackingCache(File theFile, int cacheNo){
		JSONArray parameters = new JSONArray();
		parameters.put("thumb");
		parameters.put(cloudCacheName);
		parameters.put(cacheNo);
		parameters.put(getMyType());
		
		fetchFile("/fetchBlob", parameters, theFile);
	}
	private void fetchFile(String relativeURLstring, JSONArray parameters, File theFile){
		HttpPost method;
		try {
			method = services.makeCloudHttpPost(relativeURLstring);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("json", parameters.toString()));
			method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = services.runHttpPost(method);
			int status = response.getStatusLine().getStatusCode();
			if(status != 200){
				response.getEntity().getContent().close();
				return;
			}
			
			InputStream is = response.getEntity().getContent();
			OutputStream os = new FileOutputStream(theFile);
			PlinyPlugin.copyInputStream(is, os);
		} catch (Exception e) {
			e.printStackTrace();
			RuntimeException rexcept = new RuntimeException("Cache fetching of file failed.: "+e.getLocalizedMessage());
			throw rexcept;
		}
	}
	
	protected void sendThumbnailToBackingCache(File theFile, int cacheNo) {
		try {
			URL thumbnailSaveURL = new URL(services.getCloudServiceURL(),"/saveThumb");
			sendDataToBackingCache(thumbnailSaveURL.toString(), theFile, cacheNo);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("Unexpected URL failure in sendThumbnailtoBackingCache: "+e.getLocalizedMessage());
		}
	}

}
