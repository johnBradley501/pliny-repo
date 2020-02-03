/*******************************************************************************
 * Copyright (c) 2007, 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *     John Bradley - modified to support IDataServer approach to data management
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.IObjectFetcher;
import uk.ac.kcl.cch.rdb2java.dynData.Rdb2javaCache;

/**
 * overrides the {@link uk.ac.kcl.cch.rdb2java.dynData Rdb2javaCache} used
 * by other rdb2java managed objects to meet the special needs of the
 * {@link Resource} class.  These special needs are two:
 * <ol>
 * <li>The {@link NoteLucened} item is derived from the base Resource
 * class and needs special handling so that if the item if fetched from
 * its DB key only that a NoteLucened object is returned rather than a
 * base Resource object.
 * <li>Similarly, other Pliny related plugins can have their own derived
 * version of the Resource class.  The 
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor IResourceExtensionProcessor}
 * provides a method to request the generation of such a class that the
 * rdb2java code can then fill with data, and ensures that the cache will
 * return this class rather than the base Resource class.
 * </ol>
 * <p>
 * The fact that this Resource is a special type needs to be indicated in the
 * DB data -- and this task is managed by the reference to the {@link ObjectType}.
 * 
 * @author John Bradley
 *
 */

public class ResourceCache extends Rdb2javaCache {
	//protected IObjectFetcher myDataType2 = new NoteQuery();
	//protected PreparedStatement fetchStmt = null;

	public ResourceCache(IObjectFetcher myDataType) {
		super(myDataType);
	}

	/**
	 * gets an item when both the DB key and the {@link ObjectType} associated
	 * with it is known.  This will fetch the data from the 
	 * {@link NoteLucened} cache if it is a Note type, will fetch
	 * the item from the cache if it is there, and if it is not
	 * will fetch the data from the DB (storing it in the cache), and
	 * using the object provided to it by 
     * {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#makeMyResource}
     * if applicable.
	 * 
	 * @param key DB key for the resource item.
	 * @param type type of item (key for the linked ObjectType.
	 * @return Resource-derived object with given key and type, or null
	 * if data for given key is not in the DB.
	 */
	
	public Object getItem(int key, int type){
		if(key <= 0)return null;
		Integer keyObj = new Integer(key);
		if(myCache.containsKey(keyObj))return myCache.get(keyObj);
		Object obj;
		if(type != 1)obj = loadObject(key);
		//else obj = myDataType2.loadObject(key);
		else obj = NoteLucened.getNoteLucenedItem(key);
		if(obj == null)return null;
		myCache.put(keyObj, obj);
		return obj;
	}
	
	/**
	 * this fetches a Resource when only the DB key for it is known.
	 * Note that if the Resource is a {@link NoteLucened} (has its
	 * ObjectType reference set as a Note) that this involves double
	 * read of the DB -- first to fetch it as a standard Resource, then,
	 * upon discovery that it is a Note, a second DB to get all the
	 * data for the NoteLucened version.
	 */
	
	public Object getItem(int key){
		if(key <= 0)return null;
		Integer keyObj = new Integer(key);
		if(myCache.containsKey(keyObj))return myCache.get(keyObj);
		Resource rs = (Resource)loadObject(key);
		if(rs == null)return null;

		if(rs.getObjectTypeKey() == 1){
		   if(rs instanceof NoteLucened)return rs;
		   //Object obj = myDataType2.loadObject(key);
		   Object obj = NoteLucened.getNoteLucenedItem(key);
		   if(obj == null)return null;
		   myCache.put(keyObj, obj);
		   return obj;
		} else {
			myCache.put(keyObj, rs);
			return rs;
		}
	}
	
	private Object loadObject(int ID){
      Object rslt = null;
      try {
	    ResultSet rs = Rdb2javaPlugin.getDataServer().fetchItem(myDataType, ID);
        while(rs.next()){
           Resource d = new Resource(true);
	       d.loadFromResultSet(rs);
	       d = fixClassIfNecessary(d, rs);
           rslt = d;
        }
        //stmt.close();
      } catch( Exception e) {
        e.printStackTrace(System.out);
      }
      return rslt;
	}

	private Resource fixClassIfNecessary(Resource d, ResultSet rs) throws SQLException {
		if(d.getObjectTypeKey() == 1){
			if(!Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity()){
				NoteLucened nlrslt = new NoteLucened(true);
				nlrslt.loadFromResultSet(rs);
				return nlrslt;
			}
			return d; // Notes are handled differently for DB-based retrieval   jb
		}
		IResourceExtensionProcessor proc = PlinyPlugin.getResourceExtensionProcessor(d.getObjectTypeKey());
		if(proc == null)return d;
		Resource rslt = proc.makeMyResource();
		if(rslt == null)return d;
		rslt.loadFromResultSet(rs);
		return rslt;
	}

	
}
