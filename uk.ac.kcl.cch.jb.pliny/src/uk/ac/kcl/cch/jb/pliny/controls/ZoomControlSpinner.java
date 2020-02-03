package uk.ac.kcl.cch.jb.pliny.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import uk.ac.kcl.cch.jb.pliny.commands.ChangeImageZoomValueCommand;
import uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
/**
 * this object uses the SWT spinner object 
 * to provide a control that can control the size
 * of an image in a Pliny image-oriented editor.
 * <p>
 * An editor that wishes to use this control must implement the interface
 * {@link uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageEditor IZoomableImageEditor}.
 * <p>
 * This object tracks the Resource object which must implement interface
 * {@link uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageResource IZoomableImageResource}
 * which gives access to the zoom parameter for that resource, and allows it to be
 * reset.  This means that if the zoom parameter is changed elsewhere the position
 * of this tracker can be moved to track it.
 * <p>
 * There is also code here to handle situations where the Editor is of a kind
 * where the Resource can be changed during a single editor session (thereby
 * changing the image to be displayed, and the current zoom size).  To make
 * sure this code is invoked the editor must implement the interface
 * {@link uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart}
 * 
 * @author John Bradley
 */

public class ZoomControlSpinner implements MouseListener, FocusListener,
		PropertyChangeListener {

	private Spinner slider;
	private IZoomableImageResource myResource;
	private IZoomableImageEditor targetEditor;
	private int factor; //, maxSize;
	private static final int scaleMultiplier = 1000; // to allow for working with integer division.
    private static final int maxSlider = 100;
	
	public ZoomControlSpinner(Composite parent, IZoomableImageEditor targetEditor){
		this.targetEditor = targetEditor;
		slider = new Spinner(parent, SWT.BORDER);
		slider.addMouseListener(this);
		slider.addFocusListener(this);
		slider.addListener(SWT.DefaultSelection, new Listener(){

			@Override
			public void handleEvent(Event event) {
				setZoomValue();
			}
			
		});
		myResource = targetEditor.getMyImageResource();
		if(myResource != null)myResource.addPropertyChangeListener(this);
		setupSlider();
		if(targetEditor instanceof IResourceChangeablePart){
			((IResourceChangeablePart)targetEditor).addPropertyChangeListener(this);
		}
	}
	
	public void refresh(){
		setupSlider();
	}
	
	public Control getControl(){
		return slider;
	}
	
	public void dispose(){
		if(slider != null && !slider.isDisposed()){
		   slider.removeMouseListener(this);
		   slider.dispose();
		}
		if(myResource != null)myResource.removePropertyChangeListener(this);
		if(targetEditor != null && targetEditor instanceof IResourceChangeablePart){
			((IResourceChangeablePart)targetEditor).removePropertyChangeListener(this);
		}
	}

	private int zoomToSlider(int zoom){
		return (zoom-99)*scaleMultiplier/factor;
	}
	
	private int sliderToZoom(int slider){
		return 100+factor*slider/scaleMultiplier;
	}
	
	private void calculateFactor(){
		Rectangle imgPosition = myResource.getImagePosition();
		//Dimension imgSize = myResource.getImagePosition().getSize();
		Dimension imgSize = imgPosition.getSize();
		if(imgSize.width == 0){
			factor = 0;
			slider.setEnabled(false);
			return;
		}
		if(imgSize.width < 100){
			if(slider != null)slider.setEnabled(false);
			return;
		}
		factor = (imgSize.width-100)*scaleMultiplier/maxSlider;
	}

	private void setupSlider(){
		if(slider == null || slider.isDisposed())return;
		if(myResource == null)slider.setEnabled(false);
		else {
			slider.setEnabled(true);
			calculateFactor();
			if(factor == 0)return;
			//slider.setValues(
			//		zoomToSlider(myResource.getZoomSize()),
			//		0,maxSlider,8,16,16);
			slider.setValues(
					zoomToSlider(myResource.getZoomSize()),
					10,maxSlider,0,5,5);
			if(slider.isEnabled()){
			   slider.setSelection(zoomToSlider(myResource.getZoomSize()));
			   slider.redraw();
			}
		}
	}
	
	private void setZoomValue(){
		int sliderValue = slider.getSelection();
		int currentValue = sliderToZoom(sliderValue);
		if(currentValue == myResource.getZoomSize())return;
		targetEditor.getCommandStack().execute(new ChangeImageZoomValueCommand(myResource, currentValue));
	}

	public void mouseDoubleClick(MouseEvent e) {
		// JB not used
	}

	public void mouseDown(MouseEvent e) {
		// JB not used
	}

	public void mouseUp(MouseEvent e) {
		setZoomValue();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName==Resource.ATTRIBUTES_PROP){
			if(factor == 0 && myResource.getImagePosition().height > 0)
				setupSlider();
			if(factor != 0){
			   int currentValue = zoomToSlider(myResource.getZoomSize());
			   if(currentValue != slider.getSelection())
				   slider.setSelection(currentValue);
			}
		} else if(propName == IResourceChangeablePart.CHANGE_EVENT){
			if(myResource != null)myResource.removePropertyChangeListener(this);
			myResource = (IZoomableImageResource)arg0.getNewValue();
			if(myResource != null)myResource.addPropertyChangeListener(this);
			setupSlider();
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		System.out.println("Spinner focus Gained");
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		System.out.println("Spinner focus Lost");
		setZoomValue();
		
	}

}
