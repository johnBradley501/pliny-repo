package uk.ac.kcl.cch.jb.pliny.dnd;

import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;

import uk.ac.kcl.cch.jb.pliny.model.LOType;

public class TypeManagerDragListener implements DragSourceListener {

	private TableViewer viewer;
	
	public TypeManagerDragListener(TableViewer viewer){
		this.viewer = viewer;
		DragSource source = new DragSource(viewer.getControl(), DND.DROP_COPY);
		source.setTransfer(new Transfer[] {ClipboardHandler.TRANSFER});
		source.addDragListener(this);
	}
	
	public void dragFinished(DragSourceEvent event) {
		PlinyDragSourceListener.setCurrentObject(null);
	}

	public void dragSetData(DragSourceEvent event) {
		if (ClipboardHandler.TRANSFER.isSupportedType(event.dataType)){
			IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
			if(selection.size() != 1)event.detail = DND.DROP_NONE;
			else event.data = selection.getFirstElement();
		}
	}

	public void dragStart(DragSourceEvent event) {
		//System.out.println("TypeManagerDragListener#dragStart: started");
		event.doit = true;
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (selection.size() != 1){
			event.doit = false;
			return;
		}
		Object item = selection.getFirstElement();
		if(item instanceof LOType){
			PlinyDragSourceListener.setCurrentObject(item);
			return;
		}
		PlinyDragSourceListener.setCurrentObject(null);
		event.doit = false;

	}

}
