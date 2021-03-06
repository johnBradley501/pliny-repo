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

package uk.ac.kcl.cch.jb.pliny.containmentView.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet;
import uk.ac.kcl.cch.jb.pliny.model.LOType;

/**
 * one of these must be generated by the ContainmentView's
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewMenuProvider ContainmentViewMenuProvider},
 * for each 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOtype} currently known to the system, and it
 * allows the user to control whether the type is to be included in the
 * Containment View's display or not.
 * 
 * @author John Bradley
 *
 */
public class SpecifyTypeExclusionAction extends Action{
	
   private ContainmentSet containmentSet;
   private boolean include;
   private LOType type;
	
   /**
    * generates an instance of the action to include or exclude
    * the given {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOtype} from the types to be displayed in the Containment
    * View. Although it doesn't look like it, this action eventually
    * connects with the <code>IncludedTypeManager</code> (accessible through
    * the current <code>ContainmentSet</code> which records whether or not
    * a particular <code>LOType</code> is to be included or not, and based on that
    * it decides whether the action should be to include or exclude.
    * 
    * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.IncludedTypeManager
    *  
    * @param containmentSet ContainmentSet the currently active ContainmentSet
    * @param type the LOType to which this action applies.
    */
   public SpecifyTypeExclusionAction(ContainmentSet containmentSet, LOType type){
	   super("",IAction.AS_CHECK_BOX);
	   this.include = !containmentSet.isIncluded(type);
	   this.containmentSet = containmentSet;
	   this.type = type;
	   
	   String typeName = type.getName();
	   if(typeName == null || typeName.length()== 0)typeName="(default)";
	   if(include)setText("include '"+typeName+"'");
	   else setText("exclude '"+typeName+"'");
	   
	   Image icon = null;
	   if(!include)icon = type.getColourIcon();
	   else icon = type.getBackColorIcon();
	   this.setImageDescriptor(ImageDescriptor.createFromImage(icon));
	   
	   this.setChecked(!include);
   }
   
   public void run(){
	   if(include)containmentSet.includeType(type);
	   else containmentSet.excludeType(type);
   }
}
