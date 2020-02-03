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

public class LinkQuery extends BaseQuery {
   static private Rdb2javaCache myCache = null;
	   
   public Rdb2javaCache getMyCache(){
      if(myCache == null)myCache = new Rdb2javaCache(this);
      return myCache;
   }

   public LinkQuery(){
      super();
   }

   //public boolean getDoDistinct(){return doDistinct;}
   //public void setDoDistinct(boolean parm){doDistinct = parm;}

   public String getMySelectEntities(){return Link.getSelectEntities();}
   public String getMySQLFrom(){return Link.getSQLFrom();}
   public String getMyTableJoins(){return Link.getTableJoins();}
   //public Connection getMyConnection(){return PlinyPlugin.getDefault().getConnection();}
   //public void returnMyConnection(Connection con){PlinyPlugin.getDefault().returnConnection(con);}
   public ILoadableFromResultSet getMyNewDataObject(){return (ILoadableFromResultSet)new Link(true);}
   public String getMyKeyName(){return "Link.linkKey";}
   public String getEntityName() {return "Link";}
}
