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

package uk.ac.kcl.cch.jb.pliny.imageRes.parts;

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

import uk.ac.kcl.cch.jb.pliny.figures.ScalableImageFigure;
import uk.ac.kcl.cch.jb.pliny.imageRes.figures.ImageFigure;
import uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IScalableImagePart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart;
import uk.ac.kcl.cch.jb.pliny.parts.ParentOfOrderableAbstractGraphicalEditPart;
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
 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource ImageResource}, 
 * and trickers a rescaling of the underlying
 * image and annotation area when it changes.
 * 
 * @author John Bradley
 */
public class ImageResourcePart extends
		ParentOfOrderableAbstractGraphicalEditPart implements
		IScalableImagePart, PropertyChangeListener {

	private static final int scaleMultiplier = 1024; // to allow for working with integer division.
	private int imageScaleFactor = scaleMultiplier; // from displaysize->imagesize/1000
	private int imageShiftX = 100;
	private int imageShiftY = 100;
	private int currentZoomSize = 0;

	public ImageResourcePart(ImageResource data) {
		setModel(data);
		currentZoomSize = data.getZoomSize();
		// TODO Auto-generated constructor stub
	}
	
	public ImageResource getImageResource(){
		return (ImageResource)getModel();
	}
	
	//private void setScaleSize(){
	//	
	//}
	
	private void calculateScaleFactor(ScalableImageFigure myScalableFigure){
		if(getImageResource().getZoomSize() == 0){
		   if(myScalableFigure.getOriginalWidth()==myScalableFigure.getDisplayWidth())
			  imageScaleFactor = scaleMultiplier;
		   else
		      imageScaleFactor = myScalableFigure.getOriginalWidth() * scaleMultiplier /
		                         myScalableFigure.getDisplayWidth();
		} else
			  //imageScaleFactor = getImage().getZoomSize()*scaleMultiplier / 100;
		      //imageScaleFactor = scaleMultiplier * 100/ getImageResource().getZoomSize();
		      imageScaleFactor = scaleMultiplier * myScalableFigure.getOriginalWidth()/ getImageResource().getZoomSize();
	}

	protected IFigure createFigure() {
		//ImageFigure myFigure = new ImageFigure(getImage().getUrl(), imageShiftX, imageShiftY);
		ImageFigure myFigure = new ImageFigure(getImageResource(), imageShiftX, imageShiftY);
		ScalableImageFigure myScalableFigure = myFigure.getScalableImageFigure();
		ImageResource myImage = getImageResource();
		Rectangle r = myImage.getImagePosition();
		boolean rChanged = false;
		if(r.height <= 0){
			rChanged = true;
			r.height = myScalableFigure.getOriginalHeight();
		}
		if(r.width <= 0){
			rChanged = true;
			r.width = myScalableFigure.getOriginalWidth();
		}
		if(rChanged)myImage.setImagePosition(r);
		calculateScaleFactor(myScalableFigure);
		// for testing of conversion.
		//Rectangle rslt = positionForDisplay(new Rectangle(100,100,100,100));
		//Rectangle rslt2 = positionForImage(rslt);
		return myFigure;
	}
	protected ImageFigure getImageFigure(){
		return (ImageFigure) getFigure();
	}
	
	public Point shiftNoScaling(int x, int y){
		return new Point(x+imageShiftX, y+imageShiftY);
	}
	
	public Point alignForImage(int x, int y){
		return new Point(((x-imageShiftX) * imageScaleFactor/scaleMultiplier),
		                 ((y-imageShiftY) * imageScaleFactor/scaleMultiplier));
	}
	
	public Point alignForImage(Point pt){
		return alignForImage(pt.x, pt.y);
	}
	
	public Dimension scaleForImage(Dimension in){
		return new Dimension(in.width * imageScaleFactor/scaleMultiplier,
				in.height * imageScaleFactor/scaleMultiplier);
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
		return new Point(scaleMultiplier*x/imageScaleFactor+imageShiftX,
				scaleMultiplier*y/imageScaleFactor+imageShiftY);
	}
	
	public Point alignForDisplay(Point pt){
		return alignForDisplay(pt.x, pt.y);
	}
	
	public Dimension scaleForDisplay(Dimension in){
		return new Dimension(in.width * scaleMultiplier/imageScaleFactor,
				in.height * scaleMultiplier/imageScaleFactor);
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

	/**
	 * @return the Content pane for adding or removing child figures
	 */
	public IFigure getContentPane()
	{
		ImageFigure figure = (ImageFigure) getFigure();
		return figure.getImageContentsFigure();
	}
	
	protected List getModelChildren(){
		Vector children = new Vector(getImageResource().getMyDisplayedItems().getItems());
		// this.addReferencerList(children);
		return children;
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        //installEditPolicy(EditPolicy.CONTAINER_ROLE, new ImageResXYLayoutPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ScalableImageXYLayoutEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ResourceObjectsContainerEditPolicy());
	}
	
	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getImageResource().addPropertyChangeListener(this);
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getImageResource().removePropertyChangeListener(this);
			getImageFigure().dispose();
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
		if(arg0.getPropertyName()==Resource.ATTRIBUTES_PROP){
			int newZoomSize = getImageResource().getZoomSize();
			if(newZoomSize != currentZoomSize){
				currentZoomSize = newZoomSize;
				getImageFigure().setZoomSize(currentZoomSize);
				calculateScaleFactor(getImageFigure().getScalableImageFigure());
				updateChildren();
			}
		} else
			refreshChildren();

	}

}
