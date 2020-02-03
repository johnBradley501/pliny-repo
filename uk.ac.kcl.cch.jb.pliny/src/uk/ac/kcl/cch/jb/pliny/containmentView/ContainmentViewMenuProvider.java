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

package uk.ac.kcl.cch.jb.pliny.containmentView;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import uk.ac.kcl.cch.jb.pliny.containmentView.actions.AddBothAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.actions.AddChildrenAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.actions.AddParentsAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.actions.CreateArchiveAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.actions.OpenSelectedItemsAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.actions.SpecifyTypeExclusionAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;

public class ContainmentViewMenuProvider extends ContextMenuProvider {

	/**
	 * provides a GEF-style contextual menu provider (GEF's
	 * abstract class ContextMenuProvider) for the
	 * ContainmentView.  GEF can use this to create a new
	 * contextual menu whenever the user asks for one.
	 * 
	 * @author John Bradley
	 */
	
	private ContainmentViewPage page;
	
	/**
	 * creates an instance of the contextual menu when required by GEF.
	 * One must provide a reference to the viewPage to which it applies.
	 * 
	 * @param page ContainmentViewPage to which it applies.
	 */
	public ContainmentViewMenuProvider(ContainmentViewPage page) {
		super(page.getGraphicalViewer());
		this.page = page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef..ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */

	public void buildContextMenu(IMenuManager menu) {
		String groupName = GEFActionConstants.GROUP_REST;
		menu.add(new Separator(groupName));
        menu.add(new Separator(GEFActionConstants.MB_ADDITIONS));
			
		Vector items = page.getSelectedContainmentItems();
        if(items.size() == 0)return;
        
        menu.appendToGroup(groupName, new OpenSelectedItemsAction(items));
        
        boolean haveParentOption = false, haveChildOption = false;
        Iterator it = items.iterator();
        while(it.hasNext()){
        	ContainmentItem item = (ContainmentItem)it.next();
        	haveParentOption |= !item.getShowingAllParents();
        	haveChildOption |= !item.getShowingAllChildren();
        }
        if(haveParentOption)menu.appendToGroup(groupName,new AddParentsAction(page.getContainmentSet(), items));
        if(haveChildOption)menu.appendToGroup(groupName, new AddChildrenAction(page.getContainmentSet(), items));
        if(haveParentOption && haveChildOption)
        	menu.appendToGroup(groupName, new AddBothAction(page.getContainmentSet(), items));
        
        MenuManager typesMenu = buildTypeListMenu();
        menu.appendToGroup(groupName, typesMenu);
        
        if(items.size() < 2)return;
        menu.appendToGroup(groupName, new CreateArchiveAction(items, page.getContainmentSet().getStartingResource()));
        
	}
	
	private MenuManager buildTypeListMenu(){
        MenuManager typeListMenu = new MenuManager("Included types");
        LOTypeQuery q = new LOTypeQuery();
        q.addOrder("name");
        //q.setOrderString("name");
        Vector rslt = q.executeQuery();
        Iterator it = rslt.iterator();
        ContainmentSet containmentSet = page.getContainmentSet();
        while (it.hasNext()) {
			LOType thisType = (LOType) it.next();
			SpecifyTypeExclusionAction action = new SpecifyTypeExclusionAction(containmentSet,thisType);
			typeListMenu.add(action);
		}
        return typeListMenu;
	}

}
