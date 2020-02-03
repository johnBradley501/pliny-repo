package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.ui.IWorkbenchPart;

public class SelectAllBelowAction extends SelectionAreaAction {

	public SelectAllBelowAction(IWorkbenchPart part) {
		super(part);
		setText("Select All Below");
	}

	@Override
	boolean test(GraphicalEditPart part) {
		Rectangle theStandard = getThePartRectangle();
		Rectangle theCandidate = part.getFigure().getBounds();
		return theStandard.y <= theCandidate.y;
	}

}
