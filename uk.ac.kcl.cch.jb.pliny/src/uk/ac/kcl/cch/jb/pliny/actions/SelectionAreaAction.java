package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

public abstract class SelectionAreaAction extends SelectionAction {
	private IWorkbenchPart part;
	private GraphicalEditPart thePart = null;
	private Rectangle theRectangle = null;
	private ISelection theSelection = null;
	
	public SelectionAreaAction(IWorkbenchPart part){
		super(part);
		this.part = part;
		update();
		theSelection = getSelection();
	}
	
	public GraphicalEditPart getSelectedPart(){
		if(!(theSelection instanceof StructuredSelection))return null;
		if (((StructuredSelection)theSelection).size() != 1)return null;
		Object item = ((StructuredSelection)theSelection).getFirstElement();
		if(!(item instanceof GraphicalEditPart))return null;
		return (GraphicalEditPart)item;
	}
	
	protected GraphicalEditPart getThePart(){
		return thePart;
	}
	
	protected Rectangle getThePartRectangle(){
		return theRectangle;
	}
	
	@Override
	protected boolean calculateEnabled() {
		return getSelectedPart() != null;
	}
	
	abstract boolean test(GraphicalEditPart part);
	
	boolean include(Object part){
		if (!(part instanceof GraphicalEditPart))return false;
		GraphicalEditPart gePart = (GraphicalEditPart)part;
		Object model = gePart.getModel();
		if(!(model instanceof LinkableObject))return false;
		LinkableObject lo = (LinkableObject)model;
		if(lo.getSurrogateFor() == null)return false;
		return test((GraphicalEditPart)part);
	}
	
	public void run(){
		GraphicalViewer viewer = (GraphicalViewer)part.getAdapter(GraphicalViewer.class);
		if(viewer == null)return;
		thePart = getSelectedPart();
		if(thePart == null)return;
		theRectangle = thePart.getFigure().getBounds();
		EditPart theParent = thePart.getParent();
		if(theParent == null || (!(theParent instanceof GraphicalEditPart)))return;
		Iterator it = theParent.getChildren().iterator();
		Set items = new HashSet();
		while(it.hasNext()){
			Object item = it.next();
			if((item instanceof GraphicalEditPart) && include(item)){
				items.add(item);
			}
		}
		theSelection = StructuredSelection.EMPTY;
		if(items.size() == 0)return;
		Vector allItems = new Vector(items);
		viewer.setSelection(new StructuredSelection(allItems));
	}

}
