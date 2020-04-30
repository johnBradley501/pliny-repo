package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TitleTextCellEditor extends TextCellEditor {
	private PlinyDirectEditManager manager;
   public TitleTextCellEditor(Composite composite, int options, PlinyDirectEditManager manager ){
	   super(composite, options);
	   this.manager = manager;
   }
   
   // see https://stackoverflow.com/questions/37986843/tableviewer-how-to-set-caret-position-for-the-cell-with-editing-support-and-remo

   @Override
   protected void doSetFocus(){
     super.doSetFocus();

     if (text != null) {
        text.setSelection(0, 0);
      }
   }
   
   protected void focusLost(){
	   super.focusLost();
	   manager.announceFocusLost();
   }
}
