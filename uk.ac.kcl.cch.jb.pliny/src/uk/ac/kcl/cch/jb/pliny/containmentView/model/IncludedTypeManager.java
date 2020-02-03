/*******************************************************************************
 * Copyright (c) 2007 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.containmentView.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * an object from this class manages information about which links
 * of which 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType}
 * should be included or excluded from the Containment
 * View.
 * <p>This object interacts with an Eclipse preference set so that
 * user setting of what's in and what's out can be preserved between
 * Pliny sessions.
 * 
 * @author John Bradley
 *
 */

public class IncludedTypeManager extends PropertyChangeObject 
implements PropertyChangeListener {
   private Set typesOut = new HashSet();
   private Preferences prefs;
   
   private static final String prefName = "IncludedTypeManager.containmentTypesOut";
   public static final String CHANGE_TYPE_PROPERTY = "includedTypeManager.typeChange";
   
   /**
    * creates an instance of this manager.  A beginning list of LOTypes (by their
    * ALID) that should be excluded is fetched from PlinyPlugin's
    * preference set, and used to create an initial list of what's in
    * and what's out. Subsequent changes will then be stored back
    * to this preference set as well.
    *
    */
   public IncludedTypeManager(){
	   prefs = PlinyPlugin.getDefault().getPluginPreferences();
	   setup();
   }
   
   /**
    * creates an instance of this manager.  A beginning list of LOTypes (by their
    * ALID) that should be excluded is fetched from the given
    * preference set, and used to create an initial list of what's in
    * and what's out.  Subsequent changes will then be stored back
    * to this preference set as well.
    *
    * @param prefs Preferences the set of preferences in which information
    * about what's in and what's out shold be stored
    */
   public IncludedTypeManager(Preferences prefs){
	   this.prefs = prefs;
	   setup();
   }
   
   private void setup() {
	   Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
	   
	   String whatsIn = prefs.getString(prefName);
	   if(whatsIn == null || whatsIn.trim().length() == 0){
		   doOriginalBuild();
		   return;
	   }
	   String items[] = whatsIn.trim().split(",");
	   for(int i = 0; i<items.length; i++){
		   int typeKey = Integer.parseInt(items[i]);
		   if(typeKey != 0){
		      LOType theType = LOType.getItem(typeKey);
		      if(theType != null)typesOut.add(theType);
		   }
	   }
   }
   
   private void doOriginalBuild(){
	   LOType theType = LOType.getBibRefType();
	   if(theType == null)return;
	   prefs.setValue(prefName, "0,"+theType.getALID());
	   typesOut.add(theType);
   }

   public void dispose(){
	   Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
   }
   
   /**
    * is the given Type marked as 'to be included' by this manager?
    * 
    * @param theType
    * @return boolean if <code>true</code>, include data from this type.
    */
   
   public boolean isIncluded(LOType theType){
	   return !typesOut.contains(theType);
   }
   
   /**
    * instructs this manager to mark that the given Type is to be
    * marked as 'to be included' by this manager.
    * 
    * @param theType LOType type that should be included.
    */
   
   public void includeType(LOType theType){
	   if(typesOut.contains(theType)){
		   typesOut.remove(theType);
		   generateExcludeList();
		   this.firePropertyChange(CHANGE_TYPE_PROPERTY, null, theType);
	   }
   }
   
   /**
    * instructs this manager to mark that the given Type is to be
    * marked as 'to be excluded' by this manager.
    * 
    * @param theType LOType type that should be excluded.
    */
   
   
   public void excludeType(LOType theType){
	   if(typesOut.contains(theType))return;
	   typesOut.add(theType);
	   generateExcludeList();
	   this.firePropertyChange(CHANGE_TYPE_PROPERTY, theType, null);
   }
   
   private void generateExcludeList(){
	   Iterator it = typesOut.iterator();
	   StringBuffer buf = new StringBuffer();
	   buf.append("0");
	   while (it.hasNext()) {
		  LOType element = (LOType) it.next();
		  buf.append(","+element.getALID());
	   }
	   prefs.setValue(prefName, buf.toString());
   }

   /**
    * This class tracks the deletion of LOTypes from the Pliny system, so
    * that any information it holds about the deleted item can be removed from its store.
    */
   
	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName.equals("Delete-LOType")){
			LOType theType = (LOType)arg0.getOldValue();
			typesOut.remove(theType);
		}
		
	}
   
}
