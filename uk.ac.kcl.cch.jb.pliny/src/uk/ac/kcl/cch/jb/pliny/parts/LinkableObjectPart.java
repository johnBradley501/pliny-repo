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

package uk.ac.kcl.cch.jb.pliny.parts;

import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.figures.LinkableObjectFigure;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
// import uk.ac.kcl.cch.jb.pliny.model.ReferencerList;
//import uk.ac.kcl.cch.jb.pliny.model.ReferencerManager;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.TextContentHolder;
import uk.ac.kcl.cch.jb.pliny.policies.NameDirectEditPolicy;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * extends {@link LinkableObjectBasePart} to support the display
 * of reference objects in unscaleable reference areas.
 * <p>
 * This extends the functionally it inherits by adding:
 * <ul>
 * <li>figure support for the reference object;
 * <li>edit policy support to allow for direct editing of the name of the
 * reference object 
 * ({@link uk.ac.kcl.cch.jb.pliny.policies.NameDirectEditPolicy NameDirectEditPolicy}),
 * or the passing of the request to the textual content;
 * <li>specifying model children either 
 * {@link uk.ac.kcl.cch.jb.pliny.model.MapContentHolder MapContentHolder} or
 * {@link uk.ac.kcl.cch.jb.pliny.model.TextContentHolder TextContentHolder} 
 * depending upon whether the current state is to display
 * the object's contents, or the object's own reference area; 
 * <li>handling the toggling between map/content and minimized/expanded status and
 * <li>supporting the user request to open the current reference object's
 * surrogate in a full editor.
 * 
 * </ul>
 * 
 * @author John Bradley
 *
 */

public class LinkableObjectPart extends LinkableObjectBasePart
		implements IButtonHolderPart, IDirectEditablePart, ITextEditCommittedListener{

	private PlinyDirectEditManager manager;
	//private boolean showingMap;
	private IFigure mainContents = null;

	
	public LinkableObjectPart(LinkableObject model) {
		super(model);
	}
	
	protected IFigure getContentsOfFigure(){
		Resource myResource = getLinkableObject().getSurrogateFor();
		if(myResource == null)return null;
		IResourceExtensionProcessor proc = PlinyPlugin.getResourceExtensionProcessor(myResource);
		if(proc == null)return null;
		return proc.getContentFigure(myResource);
	}

	protected IFigure createFigure() {
		Resource mySurrogate = getLinkableObject().getSurrogateFor();
		if(mySurrogate == null) // kludge code to avoid bug of linkable object with no surrogate  .jb
			return new Label("Missing Reference: "+getLinkableObject().getALID());

		mainContents = getContentsOfFigure();
		boolean loopedReference = isContainmentLoop(mySurrogate);
		int contentType = LinkableObjectFigure.TEXT_CONTENTS;
		if(loopedReference)contentType = LinkableObjectFigure.TEXT_CONTENTS_ONLY;
		return new LinkableObjectFigure(this, mainContents, contentType);
	}
	
	public LinkableObjectFigure getMyFigure(){
		return (LinkableObjectFigure)getFigure();
	}
	
	public void deactivate() {
		if(manager != null)manager.dispose();
		super.deactivate();
	}

	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NameDirectEditPolicy());
	}
	
	public List getModelChildren(){
		Vector children = new Vector();
		if(mainContents != null && (!getLinkableObject().getSurrogateFor().canDisplayMap()))return children;
		boolean loopedReference = isContainmentLoop(getLinkableObject().getSurrogateFor());
		if(!loopedReference && getLinkableObject().shouldShowMap())
			children.add(new MapContentHolder(getLinkableObject()));
		else if(getLinkableObject().getSurrogateFor() instanceof NoteLucened)
			children.add(new TextContentHolder((NoteLucened)getLinkableObject().getSurrogateFor()));
		return children;
	}

	public IFigure getContentPane(){
		return getMyFigure().getContentsFigure();
	}

	public void switchOpenStatus() {
		LinkableObject myObject = getLinkableObject();
		myObject.setIsOpen(!myObject.getIsOpen());
		//refresh();
	}
	
	public void switchMapStatus() {
		LinkableObject myObject = getLinkableObject();
		myObject.setShowingMap(!myObject.getShowingMap());
	}
	
	public boolean getMapStatus(){
		return getLinkableObject().getShowingMap();
	}

	protected Rectangle mapLocation(Rectangle r){
		return new Rectangle(r);
	}
	
	//private boolean referencerMoveNeeded(){
	//	if(!(getParent() instanceof IHasReferencerManager))return false;
	//	ReferencerManager manager = ((IHasReferencerManager)getParent()).getReferencerManager();
	//	return this.equals(manager.getCurrentPart());
	//}

	public void refreshVisuals(){
		IFigure myFigure = getFigure();
		LinkableObject model = getLinkableObject();
		Resource myResource = model.getSurrogateFor();

	    if((myFigure instanceof LinkableObjectFigure) && (myResource != null)){
		   LinkableObjectFigure figure = getMyFigure();
	       figure.setName(myResource.getName());
	       figure.setIsOpen(model.getIsOpen());
	       int cType = model.getShowingMap()?LinkableObjectFigure.AREA_CONTENTS:LinkableObjectFigure.TEXT_CONTENTS;
	       if(this.isContainmentLoop(myResource))cType = LinkableObjectFigure.TEXT_CONTENTS_ONLY;
	       figure.setContentType(cType);
	       figure.setMapIcon();
	    }
	    Rectangle r = mapLocation(model.getDisplayRectangle());
	    if(!model.getIsOpen())r.height = LinkableObjectFigure.MINIMIZED_HEIGHT;
	    if(!r.equals(getFigure().getBounds())){
	       ((GraphicalEditPart) getParent().getParent()).setLayoutConstraint(
		      this,
		      getFigure(),
		      r);
	       //if(referencerMoveNeeded())moveReferencer(r);
	    }
	}
	
	//private void moveReferencer(Rectangle r) {
	//	ReferencerList myReferencersList = ((IHasReferencerManager)getParent()).getReferencerManager().getReferencerListObject();
	//	//Point r1 = new Point(r.x+r.width,r.y);
	//	Rectangle r2 = new Rectangle(myReferencersList.getMyBounds());
	//	r2.x = r.x + r.width;
	//	r2.y = r.y;
	//	myReferencersList.setMyBounds(r2);
	//}

	public void openFullEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		try {
			getLinkableObject().getSurrogateFor().openEditor(page, getLinkableObject().getSurrPageNo());
		} catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Opening Failure",
			"This resource could not be opened for editing.");

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void performDirectEdit(){
		if(manager == null){
			manager = new PlinyDirectEditManager(
					this, new PlinyFieldLocator(getMyFigure().getTitleLabel()), SWT.CENTER);
			manager.addTextEditCommitListener(this);
		}
		manager.show();
		//System.out.println("LinkableObjectPart.performDirectEdit(): TraverseValue: "+manager.getTraverseDetail());
	}

	public String getTextToEdit() {
		return getMyFigure().getName();
	}

	public void setupText(Text text) {
		text.setBackground(getMyType().getTitleBackColour());
		text.setForeground(getMyType().getTitleForeColour());
		text.selectAll();
	}

	// needed for direct textual editing:
	private boolean directEditHitTest(Point requestLoc, IFigure figure)
	{
		//IFigure figure = getFigure();
		//IFigure figure = getMyFigure().getTitleLabel();
		if((requestLoc.x == -1) && (requestLoc.y == -1))return true;
		figure.translateToRelative(requestLoc);
		if (figure.containsPoint(requestLoc))
			return true;
		return false;
	}

	
	public void performRequest(Request request){
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT ||
			request.getType() == RequestConstants.REQ_OPEN)
		//if (request.getType() == RequestConstants.REQ_DIRECT_EDIT)
		{
			if(request instanceof SelectionRequest /* DirectEditRequest */){
				SelectionRequest myRequest = (SelectionRequest)request;
			if (directEditHitTest(myRequest.getLocation().getCopy(), getMyFigure().getTitleLabel()))
					performDirectEdit();
			else if(directEditHitTest(myRequest.getLocation().getCopy(), getMyFigure()))
				    openFullEditor();
			else passRequestToNoteTextObject(myRequest);
			}
		}
	}
	
	private void passRequestToNoteTextObject(SelectionRequest myRequest){
		List parts = getChildren();
		if(parts.size() == 0)return;
		if(!(parts.get(0) instanceof TextContentHolderPart)) return;
		TextContentHolderPart tchp = (TextContentHolderPart)parts.get(0);
		parts = tchp.getChildren();
		if(parts.size() == 0)return;
		if(!(parts.get(0) instanceof NoteTextPart)) return;
		NoteTextPart ntp = (NoteTextPart)parts.get(0);
		ntp.performRequest(myRequest);
	}

	public BaseObject getHeldObject() {
		return this.getLinkableObject();
	}

	public void setColourFromType(LOType type) {
		//getMyFigure().setColours(getMyType().getTitleBackColour(),
		//		getMyType().getTitleForeColour());
		getMyFigure().setColoursFromType(type);
	}
	
	// Do tools and requestss help out with the tranverse?
	// http://www.linuxtopia.org/online_books/eclipse_documentation/eclipse_gef_draw2d_plug-in/topic/org.eclipse.gef.doc.isv/reference/api/org/eclipse/gef/eclipse_gef_draw2d_Tool.html

	public void handleCommit(TextEditCommitEvent e) {
		if(e.getMyTraverseDetail() == SWT.TRAVERSE_TAB_NEXT){
			final SelectionRequest request = new SelectionRequest();
			request.setType(RequestConstants.REQ_DIRECT_EDIT);
			request.setLocation(new Point(-1,-1));
			passRequestToNoteTextObject(request);
		}
		
	}
}
