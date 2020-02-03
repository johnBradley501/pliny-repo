package uk.ac.kcl.cch.jb.pliny.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import uk.ac.kcl.cch.jb.pliny.model.LOType;

public class NewNoteButtonDragListener implements DragSourceListener {
	
	private Control theButton;
	
	public NewNoteButtonDragListener(Control theButton){
		this.theButton = theButton;
		DragSource source = new DragSource(theButton, DND.DROP_COPY);
		source.setTransfer(new Transfer[] {ClipboardHandler.TRANSFER});
		source.addDragListener(this);
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		event.doit = true;
		LOType item = LOType.getCurrentType();
		if(item != null){
			PlinyDragSourceListener.setCurrentObject(item);
			event.data = item;
			return;
		}
		PlinyDragSourceListener.setCurrentObject(null);
		event.doit = false;
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		if (ClipboardHandler.TRANSFER.isSupportedType(event.dataType)){
			event.data = LOType.getCurrentType();
		}

	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		PlinyDragSourceListener.setCurrentObject(null);
	}

}
