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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.kcl.cch.rdb2java.dynData.IDataInserter;

public class CloudDataInserter implements IDataInserter {
	
	private String entityName;
	private CloudServices service;

	public CloudDataInserter(String entityName, CloudServices service){
		this.entityName = entityName;
		this.service = service;
	}

	/*
   [
      "insert",
      "<entity>",
      {
      }
   ]
	 */
	@Override
	public int doInsert(Map data) {
		JSONArray jdata = new JSONArray();
		jdata.put("insert");
		jdata.put(entityName);
		checkForDates(data);
		jdata.put(new JSONObject(data));
		
		JSONArray rslt = service.retrieveData(jdata);
		try {
			if(!rslt.getString(0).equals("ok"))throw new RuntimeException("result from doInsert not 'ok'");
			return rslt.getInt(1);
		} catch (JSONException e) {
			throw new RuntimeException("In doInsert(), JSON reports: "+e.getMessage());
		}
	}

	private void checkForDates(Map data) {
		Iterator it = data.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			Object value = data.get(key);
			if(value instanceof Date)data.put(key, ((Date)value).getTime());
		}
	}

}
