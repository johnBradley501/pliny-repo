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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

import uk.ac.kcl.cch.jb.pliny.parts.IScalableImagePart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart;
import uk.ac.kcl.cch.jb.pliny.parts.ParentOfOrderableAbstractGraphicalEditPart;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.figures.PdfPageFigure;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource;
import uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsContainerEditPolicy;
import uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy;

/**
 * the GEF EditPart that acts as the root for the ImageEditor's 
 * annotation area.
 * <p>
 * This class displays a Scalable image, and therefore implements
 * {@link uk.ac.kcl.cch.jb.pliny.parts.IScalableImagePart IScaleableImagePart}
 * to provide scaling services for coordinates
 * that the displayed image, and the reference objects use to position
 * and size themselves.  Furthermore, 
 * this class tracks the setting of the Zoom size stored in the
 * associated 
 * {@link uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource PdfResource}, 
 * and trickers a rescaling of the underlying
 * PDF page image and annotation area when it changes.
 * 
 * @author John Bradley
 */
public class PdfResourcePart extends ParentOfOrderableAbstractGraphicalEditPart
		implements IScalableImagePart, PropertyChangeListener {

	private int imageShiftX = 100;
	private int imageShiftY = 0;
	private float scale = 1.0f;

	public PdfResourcePart(PdfResource data) {
		setModel(data);
		scale = data.getScale();
	}
	
	public PdfResource getPdfResource(){
		return (PdfResource)getModel();
	}

	protected IFigure createFigure() {
		PdfPageFigure myFigure = new PdfPageFigure(getPdfResource(), imageShiftX, imageShiftY);
		return myFigure;
	}
	
	protected PdfPageFigure getPdfPageFigure(){
		return (PdfPageFigure)getFigure();
	}
	
	public Point shiftNoScaling(int x, int y){
		return new Point(x+imageShiftX, y+imageShiftY);
	}
	
	public Point alignForImage(int x, int y){
		return new Point((int)((x-imageShiftX) / scale),
		                 (int)((y-imageShiftY) / scale));
	}
	
	public Point alignForImage(Point pt){
		return alignForImage(pt.x, pt.y);
	}
	
	public Dimension scaleForImage(Dimension in){
		return new Dimension((int)(in.width / scale),
				(int)(in.height / scale));
	}
	
	public Rectangle positionForImage(Rectangle in){
		Rectangle rslt = in.getCopy();
		rslt.setLocation(alignForImage(rslt.getTopLeft()));
		return rslt;
	}
	
	public Rectangle scaleForImage(Rectangle in){
		Rectangle rslt = positionForImage(in);
		rslt.setSize(scaleForImage(rslt.getSize()));
		return rslt;
	}
	
	public Point alignForDisplay(int x, int y){
		return new Point((int)(x*scale)+imageShiftX,
				(int)(y*scale)+imageShiftY);
	}
	
	public Point alignForDisplay(Point pt){
		return alignForDisplay(pt.x, pt.y);
	}
	
	public Dimension scaleForDisplay(Dimension in){
		return new Dimension((int)(in.width*scale),
				(int)(in.height*scale));
	}
	
	public Rectangle positionForDisplay(Rectangle in){
		Rectangle rslt = in.getCopy();
		rslt.setLocation(alignForDisplay(rslt.getTopLeft()));
		return rslt;
	}
	
	public Rectangle scaleForDisplay(Rectangle in){
		Rectangle rslt = positionForDisplay(in);
		rslt.setSize(scaleForDisplay(rslt.getSize()));
		return rslt;
	}
	
	public IFigure getContentPane(){
		PdfPageFigure figure = (PdfPageFigure)getFigure();
		return figure.getImageContentsFigure();
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ScalableImageXYLayoutEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ResourceObjectsContainerEditPolicy());
	}
	
	protected List getModelChildren(){
		Vector children = new Vector(getPdfResource().getMyPagedDisplayedItems().getItems());
		// this.addReferencerList(children);
		return children;
	}
	
	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getPdfResource().addPropertyChangeListener(this);
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getPdfResource().removePropertyChangeListener(this);
		}
	}

	protected void updateChildren(){
		List myChildren = getChildren();
		Iterator it = myChildren.iterator();
		while(it.hasNext()){
			AbstractGraphicalEditPart child = (AbstractGraphicalEditPart)it.next();
			if(child instanceof LinkableObjectBasePart)
				((LinkableObjectBasePart)child).refreshVisuals();
		}
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == PdfResource.CURRENTPAGE_EVENT){
			getPdfPageFigure().updateImage();
			refreshChildren();
		} else if(propName == PdfResource.SCALECHANGE_EVENT){
			scale = getPdfResource().getScale();
			getPdfPageFigure().updateImage();
			updateChildren();
		} else
			refreshChildren();

	}

}
