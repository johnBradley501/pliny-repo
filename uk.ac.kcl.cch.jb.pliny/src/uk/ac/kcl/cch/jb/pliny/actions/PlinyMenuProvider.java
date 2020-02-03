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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.figures.LinkableObjectFigure;
import uk.ac.kcl.cch.jb.pliny.model.IHasLoType;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * An extension to GEF's ContextMenuProvider for Pliny
 * applications which supports
 * the presentation of a contextual menu in GEF environments.
 * <p>
 * It examines the selected GEF AbstractEditParts, and for each one
 * which is a {@link uk.ac.kcl.cch.rdb2java.dynData.BaseObject} it
 * looks to see if it is typeable (has a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType}), and if one or more
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} has been
 * selected.  Based on these observations it decides whether or not
 * to show the menu options that allow the user to set a type, and
 * whether or not to show the options related to selected items
 * (minimize, maximize and open).
 * 
 * @author John Bradley
 *
 */

public class PlinyMenuProvider extends ContextMenuProvider {

	protected EditPartViewer viewer;
	protected CommandStack commandStack;
	protected IWorkbenchPart myPart;
	
	public PlinyMenuProvider(IWorkbenchPart myPart, EditPartViewer viewer, CommandStack commandStack) {
		super(viewer);
		this.viewer = viewer;
		this.commandStack = commandStack;
		this.myPart = myPart;
	}
	
	private MenuManager buildSetType(Vector items){
		LOTypeQuery q = new LOTypeQuery();
		q.addOrder("name");
		//q.setOrderString("name");
		Vector list = q.executeQuery();
		if(list.size() <= 1)return null;
		MenuManager setTypeManager = new MenuManager("Set type to...");
		Iterator it = list.iterator();
		while(it.hasNext()){
			LOType type = (LOType)it.next();
			setTypeManager.add(new SetObjectToTypeAction(items, commandStack, type));
		}
		return setTypeManager;
	}
	
	public void dispose(){
		super.dispose();
	}
	
	private Vector getGroupableObjects(Vector items){
		Set itemset = new HashSet();
		Vector suitableLos = new Vector();
		Vector rslt = new Vector();
		Integer parent = null;
		Iterator it = items.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof LinkableObject){
				if(((LinkableObject)obj).getSurrogateFor() != null){
					itemset.add(new Integer(((LinkableObject)obj).getSurrogateFor().getALID()));
					suitableLos.add(obj);
				}
			}
		}
		if(suitableLos.size() <= 1)return null;
		it = suitableLos.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof LinkableObject){
				LinkableObject lo = (LinkableObject)obj;
				Integer parentKey = new Integer(lo.getDisplayedIn().getALID());
				if(!itemset.contains(parentKey)){
					if(parent != null && parent.intValue() != parentKey.intValue())return null; // two parents found
					parent = parentKey;
					rslt.add(lo);
				}
			}
		}
		if(rslt.size() <= 1)return null;
		return rslt;
	}

	public void buildContextMenu(IMenuManager menu) {
		if(LinkableObjectFigure.getReferrerResource() != null){
			Resource referrerResource = LinkableObjectFigure.getReferrerResource();
			Resource referrerOwnerResource = LinkableObjectFigure.getReferrerOwnerResource();
			// System.out.println("Referrer: "+referrerResource);
			DisplayReferrerAction action = new DisplayReferrerAction(referrerResource, referrerOwnerResource);
			action.buildMenu(menu);
			LinkableObjectFigure.clearReferrerResource();
		} else {
			String groupName = GEFActionConstants.GROUP_REST;
			String typingGroup = "typing.group";
			menu.add(new Separator(groupName));
			menu.add(new Separator(typingGroup));
			menu.add(new Separator(GEFActionConstants.MB_ADDITIONS));

			Vector items = getSelectedBaseObjects();
			if(items.size() == 0)return;

			Iterator it = items.iterator();
			boolean hasLoTypeObject = false;
			boolean hasLinks = false;
			boolean hasLinkableObjects = false;
			boolean hasLinkToNotesDisplayingText = false;
			//      int groupCount = 0;
			while(it.hasNext()){
				Object item = it.next();
				hasLoTypeObject |= item instanceof IHasLoType;
				if(item instanceof LinkableObject){
					hasLinkableObjects = true;
					//      		if(((LinkableObject)item).getSurrogateFor() != null)++groupCount;
					LinkableObject lo = (LinkableObject)item;
					if(lo.getSurrogateFor() instanceof NoteLucened){
						hasLinkToNotesDisplayingText |= lo.getIsOpen() && (!lo.getShowingMap());
					}
				} else if(item instanceof Link)hasLinks = true;
			}
			if(hasLinkableObjects){
				menu.appendToGroup(groupName, new MinimizeSelectedAction(items, commandStack, false));
				menu.appendToGroup(groupName, new MinimizeSelectedAction(items, commandStack, true));
				menu.appendToGroup(groupName, new OpenSelectedEditorAction(items));
			}
			if(hasLinks)
				menu.appendToGroup(groupName, new ReverseLinkAction(items, commandStack));
			if(hasLoTypeObject){
				//menu.add(new Separator(typingGroup));
				menu.appendToGroup(typingGroup, new SetTypeToCurrentAction(items, commandStack));
				MenuManager typeManager = buildSetType(items);
				//if(typeManager != null)menu.add(typeManager);
				if(typeManager != null)menu.appendToGroup(typingGroup, typeManager);
			}
			//      if(groupCount >= 2)
			//      	menu.appendToGroup(groupName, new GroupLinkableObjectsAction(items, commandStack));
			Vector groupItems = getGroupableObjects(items);
			if(groupItems != null){
				menu.appendToGroup(groupName, new GroupLinkableObjectsAction(groupItems, commandStack));
				menu.appendToGroup(groupName, new AlignItemsAction(groupItems, commandStack, true));
				menu.appendToGroup(groupName, new AlignItemsAction(groupItems, commandStack, false));
			}
			if(hasLinkToNotesDisplayingText){
				menu.appendToGroup(groupName, new OptimizeLoSizeAction(getSelectedEditParts(), commandStack));
			}
			SelectAllBelowAction sabAction = new SelectAllBelowAction(myPart);
			if(sabAction.isEnabled()){
				MenuManager selectMenu = new MenuManager("&Select Range", "select");
				selectMenu.add(sabAction);
				SelectionAction act = new SelectAllAboveAction(myPart);
				selectMenu.add(act);
				act = new SelectAllRightAction(myPart);
				selectMenu.add(act);
				act = new SelectAllLeftAction(myPart);
				selectMenu.add(act);
				menu.appendToGroup(groupName, selectMenu);
			}
		}
	}
	
	private Vector getSelectedBaseObjects(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof AbstractEditPart){
				Object model = ((AbstractEditPart)item).getModel();
				if(model instanceof BaseObject)
				rslts.add(model);
			}
		}
		return rslts;
	}
	
	private Vector getSelectedEditParts(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof AbstractEditPart)rslts.add(item);
		}
		return rslts;
	}

}
