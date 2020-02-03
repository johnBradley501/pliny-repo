package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TitleTextCellEditor extends TextCellEditor {
	private PlinyDirectEditManager manager;
   public TitleTextCellEditor(Composite composite, int options, PlinyDirectEditManager manager ){
	   super(composite, options);
	   this.manager = manager;
   }
   
   protected void focusLost(){
	   super.focusLost();
	   manager.announceFocusLost();
   }
}
