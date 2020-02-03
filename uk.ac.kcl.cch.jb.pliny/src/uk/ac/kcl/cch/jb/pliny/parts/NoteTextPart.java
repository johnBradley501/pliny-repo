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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

import uk.ac.kcl.cch.jb.pliny.figures.NoteTextFigure;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.policies.NoteTextDirectEditPolicy;

/**
 * provides the GEF editpart the corresponds to the textual content
 * of a {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened} 
 * resource.
 * <p><b>Note:</b> that this element also needs and gets data from
 * the particular LinkableObject that causes this NoteLucened text
 * data to be shown.  Getting hold of the LinkableObject involves
 * some dubious code ('dubious' in that it seems to me to go against
 * some GEF design principles -- not that it doesn't always work)
 * in <code>createFigure()</code>.
 * 
 * 
 * @author John Bradley
 */

public class NoteTextPart extends AbstractGraphicalEditPart implements
		PropertyChangeListener, IDirectEditablePart {

	private PlinyDirectEditManager manager;
	private LinkableObject myLinkableObject = null;
	private LOType myType = null;
	
	public NoteTextPart(NoteLucened note){
		setModel(note);
	}
	
	public NoteLucened getNote(){
		return (NoteLucened)getModel();
	}

	protected IFigure createFigure() {
		EditPart grandP = getParent().getParent();
		Color background = ColorConstants.white, foreground = ColorConstants.black;
		if(grandP instanceof LinkableObjectBasePart){
		  myLinkableObject = ((LinkableObjectBasePart)grandP).getLinkableObject();
		  myType = myLinkableObject.getLoType();
		  background = myType.getBodyBackColour();
		  foreground = myType.getBodyForeColour();
		}
		return new NoteTextFigure(getNote().getContent(), background, foreground);
	}
	
	protected NoteTextFigure getNoteTextFigure(){
		return (NoteTextFigure)getFigure();
	}
	
	public Dimension getTextSize(){
		return getNoteTextFigure().getTextSize();
	}


	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NoteTextDirectEditPolicy());
		//installEditPolicy(EditPolicy.COMPONENT_ROLE,new NoteTextDirectEditPolicy()); 
	}

	public void refreshVisuals(){
		NoteTextFigure figure = getNoteTextFigure();
		NoteLucened model = (NoteLucened)getModel();
		figure.setText(model.getContent());
	}
	
	// needed for awareness of changes in NoteText added model object
	/**
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	public void activate()
	{
		super.activate();
		NoteLucened note = getNote();
		note.addPropertyChangeListener(this);
		if(myLinkableObject != null){
		   myLinkableObject.addPropertyChangeListener(this);
		   myType = myLinkableObject.getLoType();
		   if(myType != null)myType.addPropertyChangeListener(this);
		}
	}

	/**
	 * @see org.eclipse.gef.EditPart#deactivate()
	 */
	public void deactivate()
	{
		if(manager != null)manager.dispose();
		super.deactivate();
		NoteLucened note = getNote();
		note.removePropertyChangeListener(this);
		if(myLinkableObject != null){
		   myLinkableObject.removePropertyChangeListener(this);
		   myType = myLinkableObject.getLoType();
		   if(myType != null)myType.removePropertyChangeListener(this);
		}
	}	

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName == LinkableObject.TYPEKEY_PROP){
			if(myType != null)
				myType.removePropertyChangeListener(this);
			myType = myLinkableObject.getLoType();
			if(myType != null){
			    myType.addPropertyChangeListener(this);
			    getNoteTextFigure().setColours(myType.getBodyBackColour(),
					myType.getBodyForeColour());
			}
		} else if(pName == LOType.BODYBACKCOLOURINT_PROP ||
				pName == LOType.BODYFORECOLOURINT_PROP ||
				pName == LOType.TITLEBACKCOLOURINT_PROP ||
				pName == LOType.TITLEFORECOLOURINT_PROP){
			getNoteTextFigure().setColours(myType.getBodyBackColour(),
					myType.getBodyForeColour());
		}
		else refreshVisuals();
	}
	
	// needed for direct textual editing:
	
	private void performDirectEdit(){
		if(manager == null)
			manager = new PlinyDirectEditManager(
					this, new PlinyFieldLocator(getNoteTextFigure()), SWT.MULTI | SWT.WRAP);
		manager.show();
	}


	public void performRequest(Request request){
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT ||
			request.getType() == RequestConstants.REQ_OPEN)
			performDirectEdit();
	}

	public String getTextToEdit() {
		return getNoteTextFigure().getText();
	}

	public void setupText(Text text) {
		text.setBackground(myType.getBodyBackColour());
		text.setForeground(myType.getBodyForeColour());
	}
}
