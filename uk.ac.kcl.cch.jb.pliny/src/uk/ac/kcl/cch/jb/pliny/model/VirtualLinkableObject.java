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

package uk.ac.kcl.cch.jb.pliny.model;

import java.util.Vector;

public class VirtualLinkableObject extends LinkableObject {
	
   public VirtualLinkableObject(){
	   super(true);
   }
   
   public LOType getLoType(){
	   if(getALID() == 0)return getHeldLoType();
	   return super.getLoType();
   }
   
   public void setLoType(LOType item){
	   if(getALID() == 0)heldLoType = item;
	   else super.setLoType(item);
   }
   
   public Resource getDisplayedIn(){
	   if(getALID() == 0)return getHeldDisplayedIn();
	   return super.getDisplayedIn();
   }
   
   public void setDisplayedIn(Resource item){
	   if(getALID() == 0)heldDisplayedIn = item;
	   else super.setDisplayedIn(item);
   }
   
   public Resource getSurrogateFor(){
	   if(getALID() == 0)return getHeldSurrogate();
	   return super.getSurrogateFor();
   }
   
   public void setSurrogateFor(Resource item){
	   if(getALID() == 0)heldSurrogate = item;
	   else super.setSurrogateFor(item);
   }
   
   public void reIntroduceMe(){
	   if(getALID() != 0)return;
	   super.reIntroduceMe();
	   if(heldSurrogate != null && heldSurrogate.getALID() == 0 && heldSurrogate instanceof VirtualResource)
		   ((VirtualResource)heldSurrogate).makeMeReal();
	   if(heldDisplayedIn != null && heldDisplayedIn.getALID() == 0 && heldDisplayedIn instanceof VirtualResource)
		   ((VirtualResource)heldDisplayedIn).makeMeReal();
	   restoreResLinks();
	   // the following lines of code fix a problem when the toLink and
	   // fromLink are referenced >while< an item is being added   ..jb
	   Vector emptyV = getLinkedTo().getItems();
	   emptyV = getLinkedFrom().getItems();
   }
   
   public void deleteMe(){
	   if(getALID() == 0)return;
	   backupAndClearResLinks();
	   super.deleteMe();
   }

}
