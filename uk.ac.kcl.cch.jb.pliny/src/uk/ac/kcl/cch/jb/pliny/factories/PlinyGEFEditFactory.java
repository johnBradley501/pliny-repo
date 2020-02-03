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

package uk.ac.kcl.cch.jb.pliny.factories;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.MessageForGEF;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
// import uk.ac.kcl.cch.jb.pliny.model.ReferencerList;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceHolder;
import uk.ac.kcl.cch.jb.pliny.model.TextContentHolder;
import uk.ac.kcl.cch.jb.pliny.parts.IHasConnectionLayer;
import uk.ac.kcl.cch.jb.pliny.parts.LinkPart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;
import uk.ac.kcl.cch.jb.pliny.parts.MapContentHolderPart;
import uk.ac.kcl.cch.jb.pliny.parts.MessagePart;
import uk.ac.kcl.cch.jb.pliny.parts.NoteTextPart;
// import uk.ac.kcl.cch.jb.pliny.parts.ReferencerPart;
import uk.ac.kcl.cch.jb.pliny.parts.RootResourceHolderPart;
import uk.ac.kcl.cch.jb.pliny.parts.RootResourcePart;
import uk.ac.kcl.cch.jb.pliny.parts.TextContentHolderPart;

/**
 * the basic GEF EditPartFactory for mapping Pliny model objects to
 * GEF Controller objects for reference/annotation areas.
 * 
 * @author John Bradley
 *
 */

public class PlinyGEFEditFactory implements EditPartFactory {

	protected IWorkbenchPart myPart;
	
	public PlinyGEFEditFactory(){
		this.myPart = null;
	}
	
	public PlinyGEFEditFactory(IWorkbenchPart myPart) {
		this.myPart = myPart;
	}
	
	public void setWorkbenchPart(IWorkbenchPart myPart){
		this.myPart = myPart;
	}

	public EditPart createEditPart(EditPart context, Object model){
		//String contextText = "null";
		//if(context != null)contextText = context.toString
		//System.out.println("createEditPart: model: "+model);
		EditPart part = null;
		if(model instanceof NoteLucened){
			if(context != null)
               part = new NoteTextPart((NoteLucened)model);
			else part = new RootResourcePart((Resource)model);
		}
		else if(model instanceof Resource)
			part = new RootResourcePart((Resource)model);
		else if(model instanceof ResourceHolder)
			part = new RootResourceHolderPart((ResourceHolder)model);
		else if(model instanceof LinkableObject)
			part = new LinkableObjectPart((LinkableObject)model);
		else if(model instanceof MapContentHolder)
			part = new MapContentHolderPart((MapContentHolder)model);
		else if(model instanceof TextContentHolder)
			part = new TextContentHolderPart((TextContentHolder)model);
		else if(model instanceof Link){
			IHasConnectionLayer myRoot = null;
			if(context instanceof IHasConnectionLayer)myRoot = (IHasConnectionLayer)context;
			else if(context.getParent() instanceof IHasConnectionLayer)
				myRoot = (IHasConnectionLayer)context.getParent();
			part = new LinkPart(myRoot, (Link)model);
		}
		//else if(model instanceof ReferencerList)
		//	part = new ReferencerPart((ReferencerList)model, myPart);
		else if(model instanceof MessageForGEF)
			part = new MessagePart((MessageForGEF)model);
		return part;
	}

}
