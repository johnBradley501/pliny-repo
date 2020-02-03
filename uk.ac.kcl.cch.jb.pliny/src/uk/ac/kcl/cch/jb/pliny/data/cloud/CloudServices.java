/*******************************************************************************
 * Copyright (c) 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.data.cloud;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.IDataServerWithCaching;
import uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.ObjectTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Plugin;
import uk.ac.kcl.cch.jb.pliny.model.PluginQuery;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery.OrderParam;
import uk.ac.kcl.cch.rdb2java.dynData.CountItem;
import uk.ac.kcl.cch.rdb2java.dynData.IDataInserter;
import uk.ac.kcl.cch.rdb2java.dynData.IDataServer;
import uk.ac.kcl.cch.rdb2java.dynData.ILoadableFromResultSet;
import uk.ac.kcl.cch.rdb2java.dynData.IObjectFetcher;
import uk.ac.kcl.cch.rdb2java.dynData.IPersistentQuery;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;
import uk.ac.kcl.cch.rdb2java.dynData.SigninException;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery.ConstraintValue;

public class CloudServices extends PropertyChangeObject implements IDataServerWithCaching{
	
	private String cloudServiceURLstr="http://localhost:8888";
	private URL cloudServiceURL = null;
	private static final String storageServiceURL= "/storage";
	private static final String checkSignonURL = "/signon-json.jsp";
	private URL myUrl = null;
	private String userID;
	private String userPassword;
	private HttpClient client;

	private Map<String, String[]> attriNames = new HashMap<String, String[]>();
	
	public CloudServices(String cloudServiceURL, String userID, String userPassword){
		if(cloudServiceURL != null)this.cloudServiceURLstr = cloudServiceURL;
		this.userID = userID;
		this.userPassword = userPassword;
		client = new DefaultHttpClient();
	}
	
	public String getUserID(){return userID;}
	public String getCloudService(){return cloudServiceURLstr;}
	
	public void start() throws SigninException{
		try {
			cloudServiceURL = new URL(cloudServiceURLstr);
			handleSignin();
			myUrl = new URL (cloudServiceURL, storageServiceURL);
			checkCloudData();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new SigninException("URL "+storageServiceURL+"for cloud fails:"+e.getMessage());
		}

	}
	
	public URL getCloudServiceURL(){return cloudServiceURL;}

	private void handleSignin() throws SigninException{
		JSONArray a;
		try {
			a = retrieveData(new URL(cloudServiceURL, checkSignonURL));
		} catch (MalformedURLException e) {
			throw new SigninException("MalformedURL: "+e.getMessage());
		}
		if(a == null)throw new SigninException("Cloud signin failed for "+cloudServiceURL);
		String loginURL;
		try {
			String status = a.getString(0);
			JSONObject data = a.getJSONObject(1);
			if(status.equals("loggedin")){
				String currentUserId = data.getString("userId");
				if(currentUserId.equals(userID)) return;
				String logoutURL = data.getString("logout");
				retrieveData(new URL(cloudServiceURL, logoutURL));
			}
			loginURL = data.getString("login");
		} catch (JSONException e){
			e.printStackTrace();
			throw new SigninException("JSON Exception: "+e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new SigninException("Malformed URL: "+e.getMessage());
		}
		if(cloudServiceURLstr.startsWith("http://localhost"))
			realHandleTestSignin(loginURL);
		else realHandleSignin(loginURL);
	}
	
	public HttpResponse runHttpPost(HttpPost post) throws ClientProtocolException, IOException{
		return client.execute(post);
		
	}
	
	public HttpPost makeCloudHttpPost(String relativeURLString) throws MalformedURLException{
		URL theURL = new URL(cloudServiceURL, relativeURLString);
		return new HttpPost(theURL.toExternalForm());
	}

	private void realHandleTestSignin(String loginURL) throws SigninException {
		//MalformedURLException, IOException, JSONException{
		try{
			URL theLoginURL = new URL(cloudServiceURL, loginURL); // "/test.jsp")
			HttpPost post = new HttpPost(theLoginURL.toExternalForm());
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", userID));
			nameValuePairs.add(new BasicNameValuePair("Passwd", userPassword));
			nameValuePairs.add(new BasicNameValuePair("action", "Log In"));
			nameValuePairs.add(new BasicNameValuePair("continue", "/signon-json.js"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							response.getEntity().getContent()));
			in.close();

			JSONArray rslts = retrieveData(new URL(cloudServiceURL, checkSignonURL));
			if(rslts == null)throw new RuntimeException("login failed for "+theLoginURL);
			String status = rslts.getString(0);
			if(!status.equals("loggedin"))throw new RuntimeException("Cloud test signon failed (3) for "+this.userID);
		} catch (Exception e){
			throw new SigninException(e.getMessage());
		}
	}

	private void realHandleSignin(String loginURLstr) throws SigninException {
		//MalformedURLException, IOException, JSONException{
		
		// http://www.vogella.de/articles/ApacheHttpClient/article.html
		// https://developers.google.com/accounts/docs/AuthForInstalledApps
		
		HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("Email", userID));
		nameValuePairs
				.add(new BasicNameValuePair("Passwd", userPassword));
		nameValuePairs.add(new BasicNameValuePair("accountType", "GOOGLE"));
		nameValuePairs.add(new BasicNameValuePair("source","pliny-cloud"));
		nameValuePairs.add(new BasicNameValuePair("service", "ah")); //"ac2dm"));

		try {
		   post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		   HttpResponse response = client.execute(post);
		   BufferedReader rd = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));

			String line = "";
			String authKey = null;
			while ((line = rd.readLine()) != null) {
				// System.out.println(line);
				if (line.startsWith("Auth=")) {
					authKey = line.substring(5);
				}

			}
			if(authKey == null)throw new RuntimeException("Cloud signon failed (2) for "+this.userID);
			
		    // https://appengine.google.com/_ah/conflogin?continue=http%3A%2F%2Fpliny-cloud.appspot.com%2Fsignon.jsp&pli=1&auth=DQAAAI...

			String continueString="continue="+URLEncoder.encode(cloudServiceURLstr+checkSignonURL,"UTF-8");
			String otherParams="&pli-1&auth="+URLEncoder.encode(authKey, "UTF-8");

			JSONArray a = retrieveData(new URL("https://appengine.google.com/_ah/conflogin?"+continueString+otherParams));
			if(a == null)throw new RuntimeException("retrieveData failed for "+checkSignonURL);
			String status = a.getString(0);
			if(!status.equals("loggedin"))throw new RuntimeException("Cloud signon failed (1) for "+this.userID);
		} catch(Exception e){
			throw new SigninException(e.getMessage());
		}
	}
	
	private void checkCloudData() {
		PluginQuery pq = new PluginQuery();
		Vector plugins = pq.executeQuery();
		if(plugins.size() == 0){
			Plugin plugin = new Plugin(true);
			plugin.setIdString("uk.ac.kcl.cch.jb.pliny");
			plugin.reIntroduceMe();
			
			CloudDataInserter dataInserter = new CloudDataInserter("ObjectType", this);
			HashMap map = new HashMap();
			map.put("name", "Note");
			map.put("pluginKey", plugin.getALID());
			map.put("idString", "");
			map.put("editorId", "uk.ac.kcl.cch.jb.pliny.noteEditor");
			map.put("iconId", "");
			dataInserter.doInsert(map);
			
			map.put("name", "Web Browser");
			map.put("editorId", "uk.ac.kcl.cch.jb.pliny.browserEditor");
			dataInserter.doInsert(map);
			
			dataInserter = new CloudDataInserter("LOType", this);
			map = new HashMap();
			map.put("name", "");
			map.put("titleForeColour", 0);
			map.put("titleBackColour", 65280);
			map.put("bodyForeColour", 0);
			map.put("bodyBackColour", 15794160);
			map.put("sourceRoleKey", 0);
			map.put("targetRoleKey", 0);
			dataInserter.doInsert(map);
			
			map = new HashMap();
			map.put("source document", "");
			map.put("titleForeColour", 16777215);
			map.put("titleBackColour", 255);
			map.put("bodyForeColour", 0);
			map.put("bodyBackColour", 15790335);
			map.put("sourceRoleKey", 0);
			map.put("targetRoleKey", 0);
			dataInserter.doInsert(map);
		} else {
			ObjectTypeQuery otq = new ObjectTypeQuery();
			otq.executeQuery(); // loads them all from cloud
		}
	}

	@Override
	public ResultSet fetchItem(IObjectFetcher of, int ID) {
		
		String entityName = of.getEntityName();
		if(entityName.equals("Note"))
			entityName = "Resource"; // to deal with single entity model of Notes in the cloud   jb
		
		String[] attrList = attriNames.get(entityName);
		if(attrList == null){
			attrList = CloudQueryResultSet.processSelectedEntities(of.getMySelectEntities());
			attriNames.put(entityName, attrList);
		}
		
		JSONArray query = new JSONArray();
		query.put("get");
		query.put(entityName);
		query.put(ID);
		
		//submit, and process the result
		JSONArray jsonResult = retrieveData(query);
		try {
			if(!jsonResult.getString(0).equals("OK"))return null;
			ResultSet rslt = new CloudQueryResultSet(jsonResult.getJSONObject(3), attrList);
			return rslt;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*
   [
      "query",
      "<entity>"
      [
         ["<property>" "<comp op>" <value>]
         ...
      ]
      [
         ["<property>" "<sort direction">]
         ...
      ]
   ]
	 */
	
	private JSONArray prepareQueryParameters(BaseQuery bq, String type){
		JSONArray query = new JSONArray();
		query.put(type);
		query.put(bq.getEntityName());
		
		JSONArray constraints = new JSONArray();
		query.put(constraints);
		Vector queryParams = bq.getQueryParams();
		int parmNo = 0;
		for(ConstraintValue item: bq.getConstraints()){
			JSONArray conVal = new JSONArray();
			constraints.put(conVal);
			conVal.put(item.getAttrName());
			conVal.put(item.getOp());
			Object val = null;
			if(item.isParam())val = queryParams.get(parmNo++);
			else if(item.getIValue() != null)val = item.getIValue();
			else if(item.getValue() != null)val = item.getValue();
			else if(item.getDValue() != null)val = item.getDValue().getTime();
			conVal.put(val);
		}
		
		JSONArray ordering = new JSONArray();
		for(OrderParam item: bq.getOrderParams()){
			JSONArray orderVal = new JSONArray();
			ordering.put(orderVal);
			orderVal.put(item.getAttrName());
			orderVal.put(item.getDirection());
		}
		query.put(ordering);
	    return query;
	}

	@Override
	public ResultSet prepareFullValueResultSet(BaseQuery bq) {
		JSONArray query = prepareQueryParameters(bq, "query");
		
		//submit, and process the result
		JSONArray jsonResult = retrieveData(query);
		
		CloudQueryResultSet rs = new CloudQueryResultSet(jsonResult, bq.getMySelectEntities());
		return rs;
		}

	
	@Override
	public Vector runQuery(BaseQuery bq) {
    	boolean isResourceQuery = bq instanceof ResourceQuery; // needs special handling  .jb
		ResultSet rs = prepareFullValueResultSet(bq);
		Vector rslt = new Vector();
		try {
			while(rs.next()){
				ILoadableFromResultSet obj  = bq.getMyNewDataObject();
				obj.loadFromResultSet(rs);
				Object cachedObject = bq.checkCache(obj.getALID());
				if(cachedObject != null)rslt.add(cachedObject);
				else if(isResourceQuery)handleResourceItem(bq, rs, rslt);
				else {
					bq.getMyCache().addNewItem(obj.getALID(), obj);
					rslt.add(obj);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rslt;
	}
	
	private void handleResourceItem(BaseQuery bq, ResultSet rs, Vector rslt){
	    Resource theResource = null;
		try {
		    int objectTypeKey = rs.getInt("objectTypeKey");
		    if(objectTypeKey == 1 /* Note */)theResource = new NoteLucened(true);
		    else {
				IResourceExtensionProcessor proc = PlinyPlugin.getResourceExtensionProcessor(objectTypeKey);
                if(proc == null)theResource = new Resource(true);
                else theResource = proc.makeMyResource();
		    }
			theResource.loadFromResultSet(rs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		bq.getMyCache().addNewItem(theResource.getALID(), theResource);
		rslt.add(theResource);
	}
	
	public JSONArray retrieveData(JSONArray parms){
		return retrieveData(parms, myUrl.toExternalForm());
	}
	
	public JSONArray retrieveData(JSONArray parms, String urlString){
		HttpPost post = new HttpPost(urlString);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("json",
					parms.toString()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			JSONArray rslts = getJSONResults(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
			return rslts;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONArray getJSONResults(BufferedReader d) throws IOException, JSONException{
		StringBuffer buf = new StringBuffer();
		String str;
	    while (null != ((str = d.readLine()))){
	    	buf.append(str + "\n");
	    }
	    d.close ();
		JSONTokener jsonTokener = new JSONTokener(buf.toString());
		return new JSONArray(jsonTokener);
	}
	
	public JSONArray retrieveData(String relURL) throws MalformedURLException{
		URL theURL = new URL(cloudServiceURL, relURL);
		return retrieveData(theURL);
	}
	
	private JSONArray retrieveData(URL theURL){
		try {
			HttpGet get = new HttpGet(theURL.toExternalForm());
			HttpResponse response = client.execute(get);
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(
			        		response.getEntity().getContent()));
			JSONArray rslts = getJSONResults(in);
			return rslts;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IPersistentQuery makePersistentQuery(BaseQuery bq, String foreignKeyName) {
		return new CloudPersistentQuery(this, bq, foreignKeyName);
	}

	@Override
	public void dispose() {
		// nothing needs to be done here (JB)
	}

	@Override
	public int runCountQuery(BaseQuery bq) {
		JSONArray query = prepareQueryParameters(bq, "count");
		JSONArray jsonResult = retrieveData(query);
		
		try {
			return jsonResult.getInt(0);
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public IDataInserter makeDataInserter(String tableName) {
		return new CloudDataInserter(tableName, this);
	}

	@Override
	public boolean isNoteInSeparateEntity() {
		return false;
	}

	@Override
	public void doDelete(String entityName, String keyName, int key) {
		/*
   [
      "delete",
      "<entity>",
      <entityKey>
   ]
		 */
		JSONArray jdata = new JSONArray();
		jdata.put("delete");
		jdata.put(entityName);
		jdata.put(key);
		
		JSONArray rslt = retrieveData(jdata);
		try {
			if(rslt == null || rslt.length() == 0 || !(rslt.getString(0).equals("ok")))
				throw new RuntimeException("doDelete() fails with non'ok' result");
		} catch (JSONException e) {
			throw new RuntimeException("doDelete() fails with non'ok' result");
		}
	}

	@Override
	public void doFkAssignment(String table, String column, int from, int to) {
		// JB I don't believe this is actually used anywhere.
		throw new RuntimeException("doFkAssignment is not implemented in Cloud Services");
	}
	
	/*
   [
       "update",
      "<entity>",
      <entityKey>
       {
       }
   ]
	 */
	
	private void doUpdate(String tableName, int keyVal, String attr, Object val){
		JSONArray params = new JSONArray();
		params.put("update");
		params.put(tableName);
		params.put(keyVal);
		JSONObject updval = new JSONObject();
		params.put(updval);
		try {
			updval.put(attr, val);
		} catch (JSONException e) {
			throw new RuntimeException("JSONException in doUpdate: "+e.getMessage());
		}
		
		JSONArray rslt = retrieveData(params);
		try {
			if(rslt == null || rslt.length() == 0 || !(rslt.getString(0).equals("ok")))
				throw new RuntimeException("doUpdate() fails with non'ok' result");
		} catch (JSONException e) {
			throw new RuntimeException("doUpdate() fails with non'ok' result");
		}
	}

	@Override
	public void doUpdate(String tableName, String attr, String keyName, int keyVal, int newValue) {
		doUpdate(tableName, keyVal, attr, newValue);
	}

	@Override
	public void doUpdate(String tableName, String attr, String keyName,	int keyVal, String newValue) {
		doUpdate(tableName, keyVal, attr, newValue);
	}

	@Override
	public void doUpdate(String tableName, String attr, String keyName,
			int keyVal, Timestamp newValue) {
		doUpdate(tableName, keyVal, attr, newValue.getTime());
	}

	@Override
	public void doUpdate(String tableName, String attr, String keyName,
			int keyVal, Date newValue) {
		doUpdate(tableName, keyVal, attr, newValue.getTime());
	}

	@Override
	public void doUpdate(String tableName, String attr, String keyName,
			int keyVal, Time newValue) {
		doUpdate(tableName, keyVal, attr, newValue.getTime());
	}

	@Override
	public void doResourceFullnameAndInitUpdate(String fullName, String init,
			int key) {
		JSONArray params = new JSONArray();
		params.put("update");
		params.put("Resource");
		params.put(key);
		JSONObject updval = new JSONObject();
		params.put(updval);
		try {
			updval.put("fullName", fullName);
			updval.put("initChar", init);
		} catch (JSONException e) {
			throw new RuntimeException("JSONException in doUpdate: "+e.getMessage());
		}
		
		JSONArray rslt = retrieveData(params);
		try {
			if(rslt == null || rslt.length() == 0 || !(rslt.getString(0).equals("ok")))
				throw new RuntimeException("doUpdate() fails with non'ok' result");
		} catch (JSONException e) {
			throw new RuntimeException("doUpdate() fails with non'ok' result");
		}
	}

	@Override
	public Vector<CountItem> queryCount(String itemColName, String entityName,
			String keyName, String order) {
		return queryCount(itemColName, entityName, keyName, "", 0, order);
	}

	@Override
	public Vector<CountItem> queryCount(String itemColName, String entityName,
			String keyName, String constraintAttr, int constraintValue,
			String order) {
		boolean returnAsDate = itemColName.endsWith("Date"); // a kludge to convert returned number into a date object   jb
		JSONArray params = new JSONArray();
		params.put("counts");
		params.put(entityName);
		params.put(itemColName);
		params.put(order);
		params.put(constraintAttr);
		params.put(constraintValue);
		
		JSONArray rslt = retrieveData(params);
		Vector<CountItem> items = new Vector<CountItem>();
		try {
			if(rslt.length()< 2 || !(rslt.getString(0).equals("ok")))return null;
			JSONArray values = rslt.getJSONArray(1);
			for(int i = 0; i < values.length(); i++){
				JSONArray item = values.getJSONArray(i);
				CountItem ciObject = null;
				if(returnAsDate)
					ciObject = new CountItem(new Date(item.getLong(0)), item.getInt(1));
				else ciObject = new CountItem(item.getString(0), item.getInt(1));
				items.add(ciObject);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return items;
	}
    
    public void notifyCreate(String type, Object object){
    	firePropertyChange("Create-"+type, null, object);
    }
    
    public void notifyDelete(String type, Object object){
    	firePropertyChange("Delete-"+type, object, null);
    }

	@Override
	public IFileCacheManager createCacheManager(
			org.eclipse.core.runtime.Plugin owner, String cacheName, String myType) {
		// TODO Auto-generated method stub
		return new LocalCloudResourceFileCacheManager(this, owner, cacheName, myType);
	}

}
