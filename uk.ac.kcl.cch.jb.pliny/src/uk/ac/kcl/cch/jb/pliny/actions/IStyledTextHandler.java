package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyledText;

public interface IStyledTextHandler extends IAction {
   public void setStyledText(StyledText theObject);
}
