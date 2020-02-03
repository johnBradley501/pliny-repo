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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import uk.ac.kcl.cch.jb.pliny.commands.DeleteFavouriteCommand;
import uk.ac.kcl.cch.jb.pliny.commands.DeleteLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.commands.DeleteObjectTypeCommand;
import uk.ac.kcl.cch.jb.pliny.commands.DeleteResourceCommand;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.INamedObject;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * the delete action for the Resource Explorer
 * ({@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView}).
 * Makes use of the Explorer's CommandStack so that deletion
 * is undoable.  It also displays a MessageBox to confirm that 
 * the user wishes to do the deletion.  It also checks that
 * Resources are not being used in 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType}s or as one of the
 * {@link uk.ac.kcl.cch.jb.pliny.model.Favourite}s 
 * (Bookmarks) before deleting them.
 * <p>
 * It is capable of deleting
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType}s,
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource}s and
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} 
 * (the latter from Contains or Displayed By levels in the
 * Explorer's hierarchical display).
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerDeleteAction extends Action {
	
	private ResourceExplorerView myView;
	private IWorkbenchWindow window;
	private Shell parentShell;

	public ResourceExplorerDeleteAction(ResourceExplorerView view) {
		super();
		myView = view;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	private boolean confirmFromUser(INamedObject item, String typeName){
		MessageBox messageBox = new MessageBox(parentShell, 
				SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		messageBox.setText("Confirm deletion");
		messageBox.setMessage("Please confirm deletion of "+typeName+" '"+item.getName()+
				"' and all objects connected to it.");
		int rc = messageBox.open();
		return rc == SWT.OK;
	}
	
	private void closeEditorInstance(Resource thisone){
		//String id = thisone.getIdentifier();
		//if((id == null) || (id.equals("")){
		//	if(!(thisone instanceof Note))return;
		//	id = "note:"+thisone.getALID();
		//}
	    IEditorInput input = Resource.idString2EditorInput(thisone.getIdentifier());
	    if(input == null)return;
		IEditorPart editor = myView.getSite().getPage().findEditor(input);
		if(editor == null)return;
		//myView.getSite().getPage().closeEditor(editor, false);
		myView.getSite().getPage().closeEditor(editor, true);
	}
	
	public void run(){
		if(window == null){
			window = myView.getViewSite().getWorkbenchWindow();
		}
		IWorkbenchPage page = window.getActivePage();
		parentShell = window.getShell();
		if(page == null)return;
		Vector candidates = myView.getSelectedBaseObjects();
		Iterator it = candidates.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			//if(obj instanceof Note)
			//	handleDeleteNote((Note)obj);
			if(obj instanceof Resource)
				handleDeleteResource((Resource)obj);
			else if(obj instanceof ObjectType)
				handleDeleteObjectType((ObjectType)obj);
			else if(obj instanceof Favourite)
				handleFavouriteDelete((Favourite)obj);
			else if(obj instanceof LinkableObject)
				handleDeleteLinkableObjectType((LinkableObject)obj);
		}
	}

	private void handleFavouriteDelete(Favourite favourite) {
		myView.getCommandStack().execute(new DeleteFavouriteCommand(favourite));
	}

	private void handleDeleteLinkableObjectType(LinkableObject object) {
		if(!confirmFromUser(object.getSurrogateFor(), "surrogate"))return;
		myView.getCommandStack().execute(new DeleteLinkableObjectCommand(object));
	}

	private void handleDeleteObjectType(ObjectType type) {
		if(type.getResources().getItems().size() != 0){
			MessageBox messageBox = new MessageBox(parentShell, 
					SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Cannot delete");
			messageBox.setMessage("Object Type object '"+type.getName()+
					"' cannot be deleted because resources are still connected to it.");
			messageBox.open();
			return;
		}
		if(!confirmFromUser(type, "Object Type"))return;
		myView.getCommandStack().execute(new DeleteObjectTypeCommand(type));
	}

	private void handleDeleteResource(Resource resource) {
		MessageBox messageBox = new MessageBox(parentShell, 
				SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Cannot delete");

		if(resource.isFavourite()){
			messageBox.setMessage("Your Resource object '"+resource.getName()+
					"' cannot be deleted because it is one of your starting objects.\n"+
					"If you really wish to delete it, first remove it from there.");
			messageBox.open();
			return;
		}
		if(resource.getMyTargetRoles().getCount() > 0){
			Vector rslt = resource.getMyTargetRoles().getItems();
			LOType type = (LOType)rslt.get(0);
			messageBox.setMessage("Your Resource object '"+resource.getName()+
					"' cannot be deleted because it is used as a Target Role for '"+
					type.getName()+"'.\n"+
					"If you really wish to delete it, first remove it from there.");
			messageBox.open();
			return;
		}
		if(resource.getMySourceRoles().getCount() > 0){
			Vector rslt = resource.getMySourceRoles().getItems();
			LOType type = (LOType)rslt.get(0);
			messageBox.setMessage("Your Resource object '"+resource.getName()+
					"' cannot be deleted because it is used as a Source Role for '"+
					type.getName()+"'.\n"+
					"If you really wish to delete it, first remove it from there.");
			messageBox.open();
			return;
		}
		String resourceType = "resource";
		if(resource instanceof NoteLucened)resourceType = "note";
		if(!confirmFromUser(resource, resourceType))return;
		closeEditorInstance(resource);
		myView.getCommandStack().execute(new DeleteResourceCommand(resource, resourceType));
	}


}
