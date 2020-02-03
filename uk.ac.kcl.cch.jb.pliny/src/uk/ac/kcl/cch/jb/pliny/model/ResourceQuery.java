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

import java.io.*;
import java.util.*;
//import java.sql.*;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

public class ResourceQuery extends BaseQuery {
   static private ResourceCache myCache = null;
	   
   public Rdb2javaCache getMyCache(){
      if(myCache == null)myCache = new ResourceCache(this);
      return myCache;
   }

   public ResourceQuery(){
      super();
   }

   public String getMySelectEntities(){return Resource.getSelectEntities();}
   public String getMySQLFrom(){return Resource.getSQLFrom();}
   public String getMyTableJoins(){return Resource.getTableJoins();}
   //public Connection getMyConnection(){return PlinyPlugin.getDefault().getConnection();}
   //public void returnMyConnection(Connection con){PlinyPlugin.getDefault().returnConnection(con);}
   public ILoadableFromResultSet getMyNewDataObject(){return (ILoadableFromResultSet)new Resource(true);}
   public String getMyKeyName(){return "Resource.resourceKey";}
   public String getEntityName() {return "Resource";}
}
