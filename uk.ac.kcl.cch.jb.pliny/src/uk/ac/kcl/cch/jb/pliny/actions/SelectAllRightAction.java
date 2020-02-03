package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.ui.IWorkbenchPart;

public class SelectAllRightAction extends SelectionAreaAction {

	public SelectAllRightAction(IWorkbenchPart part) {
		super(part);
		setText("Select All Right");
	}

	@Override
	boolean test(GraphicalEditPart part) {
		Rectangle theStandard = getThePartRectangle();
		Rectangle theCandidate = part.getFigure().getBounds();
		return theStandard.x <= theCandidate.x;
	}

}
