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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.requests.CreationFactory;

import uk.ac.kcl.cch.jb.pliny.commands.CreateLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * The DnD creation factory for 
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.PlinyObjectTransferDropTargetListener}
 * 
 * @author John Bradley
 *
 */

public class DnDFactory implements CreationFactory {
	
	private Object createdObject;
	//private Resource surrogate;

	public DnDFactory() {
		createdObject = null;
	}

	public Object getNewObject() {
		return createdObject;
	}

	public Object getObjectType() {
		return createdObject.getClass();
	}
	
	public void setupObject(Object template){
		if(template instanceof LinkableObject)
			handleLinkableObject((LinkableObject)template);
		else if(template instanceof Resource)
			handleResource((Resource)template);
		//else if(template instanceof Favourite)   // this should be handled by next test for IHasResource   JB
		//	handleResource(((Favourite)template).getResource());
		else if(template instanceof IHasResource)
			handleResource(((IHasResource)template).getResource());
		else if(template instanceof LOType){createdObject = template;}
		else throw new RuntimeException("unexpected type in DnDFactory/setupObject: "+template);
	}

	private void handleResource(Resource resource) {
		if(resource instanceof VirtualResource)
			((VirtualResource)resource).makeMeReal();
		LinkableObject rslt = new LinkableObject(true);
		Rectangle r = new Rectangle(0,0,0,0);
	    r.setSize(CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION);
		rslt.setDisplayRectangle(r);
		rslt.setIsOpen(resource instanceof NoteLucened);
		//rslt.setShowingMap(!(resource instanceof NoteLucened));
		rslt.setShowingMap(false);
		rslt.reIntroduceMe();
		
		rslt.setLoType(LOType.getCurrentType());
		rslt.setSurrogateFor(resource);
		rslt.setSurrPageNo(resource.getCurrentPage());
		//System.out.println("DnDFactory: resource: "+resource+", size: "+rslt.getDisplayRectangle()+
		//		", isOpen: "+rslt.getIsOpen());
		createdObject = rslt;
	}

	private void handleLinkableObject(LinkableObject object) {
		LinkableObject rslt = new LinkableObject(true);
		Rectangle r = new Rectangle(0,0,0,0);
		Rectangle givenR = object.getDisplayRectangle();
		Dimension d = givenR.getSize();
		r.setSize(d);
		rslt.setDisplayRectangle(r);
		rslt.setIsOpen(object.getIsOpen());
		rslt.setShowingMap(object.getShowingMap());
		rslt.reIntroduceMe();
		
		rslt.setLoType(object.getLoType());
		rslt.setSurrogateFor(object.getSurrogateFor());
		rslt.setSurrPageNo(object.getSurrPageNo());
		createdObject = rslt;
		
	}
	
	public void clearObject(){
		createdObject = null;
	}

}
