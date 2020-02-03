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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This action supports the "see referrer" button that appears in the toolbar
 * associated with Pliny editors and views, and generates a drop-down list of
 * resources that refer to the editor/view's resource.
 * <p>
 * Although this action generates a button it is a pulldown type.  The main
 * icon (which when clicked invokes the run() method actually does nothing but
 * display a dialog box to the user telling them to click on the dropdown
 * icon instead.
 * 
 * @author John Bradley
 *
 */
public class DisplayReferrerAction extends Action {
    //private MenuManager dropDownMenuMgr;
    //private IEditorPart targetEditor;
	private IResourceDrivenPart myPart;
    private IWorkbenchPage thePage;
    private Resource referrerResource = null;
    private int ownerKey = 0;
    
    private class LaunchAction extends Action{
    	private Resource theResource;
    	private int pageNo;
    	
    	public LaunchAction(LinkableObject lo){
    		super();
    		this.theResource = lo.getDisplayedIn();
    		this.pageNo = lo.getDisplPageNo();
    		String text = theResource.getName();
    		if(pageNo != 0)text = text+" ("+pageNo+")";
    		this.setText(text);
    		this.setImageDescriptor(
    				ImageDescriptor.createFromImage(theResource.getObjectType().getIconImage()));
    		this.setToolTipText("list of resources displaying this item.");
    	}
    	
    	public void run(){
    		try {
    			if(thePage == null)
    				thePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
				theResource.openEditor(thePage, pageNo);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private class DummyAction extends Action {
    	public DummyAction(String name){
    		super(name);
    	}
    }

    /**
     * this constructor is used by Editors since its action icon
     * must appear in the main toolbar with other editor action icons.
     *
     */
	public DisplayReferrerAction() {
		super("show referrer",IAction.AS_DROP_DOWN_MENU);
		this.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/referencersOn.gif"));
        setMenuCreator(menuCreator);
	}
	
	public DisplayReferrerAction(Resource theResource, Resource referrerOwnerResource){
		referrerResource = theResource;
		if(referrerOwnerResource != null)
		    ownerKey = referrerOwnerResource.getALID();
	}
	
    /**
     * this constructor is used by Pliny's AnnotationViewer since its action icon
     * must appear among the view's action icons.
     *
     * @param myPart IViewPart the owning part for this action.
     */
	public DisplayReferrerAction(IViewPart myPart){
		this();
		thePage = myPart.getSite().getPage();
    	if(!(myPart instanceof IResourceDrivenPart))return;
    	this.myPart = (IResourceDrivenPart)myPart;
	}
	
	public void buildMenu(IMenuManager manager){
		boolean itemdisplayed = false;
		Resource myResource = referrerResource;
		if(myResource == null) myResource = myPart.getMyResource();
		if(myResource != null){
	    	Iterator it = myResource.getMySurrogates().getItems().iterator();
	    	while(it.hasNext()){
	    		LinkableObject lo = (LinkableObject)it.next();
	    		if(lo.getSurrogateFor() != null  && lo.getDisplayedIn() != null && lo.getDisplayedIn().getALID() != ownerKey){
	    		  //buildOpenAction(manager, lo.getDisplayedIn());
	    		  buildOpenAction(manager, lo);
	    		  itemdisplayed = true;
	    		}
	    	}
		}
    	if(!itemdisplayed)manager.add(new DummyAction("no items to display"));
	}

	private void buildOpenAction(IMenuManager manager, LinkableObject lo) {
		manager.add(new LaunchAction(lo));
	}
	
    private IMenuCreator menuCreator = new IMenuCreator() {
        private MenuManager dropDownMenuMgr = null;

        public MenuManager getManager(){
        	if(dropDownMenuMgr == null){
        		dropDownMenuMgr = new MenuManager();
        		dropDownMenuMgr.setRemoveAllWhenShown(true);
        		//dropDownMenuMgr.add(new DummyAction("one"));
        		dropDownMenuMgr.addMenuListener(new IMenuListener(){

        			public void menuAboutToShow(IMenuManager manager) {
        				buildMenu(manager);
        			}
        			
        		});
        	}
        	return dropDownMenuMgr;
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
         */
        public Menu getMenu(Control parent) {
            return getManager().createContextMenu(parent);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
         */
        public Menu getMenu(Menu parent) {
            Menu menu = new Menu(parent);
            IContributionItem[] items = getManager().getItems();
            for (int i = 0; i < items.length; i++) {
                IContributionItem item = items[i];
                IContributionItem newItem = item;
                if (item instanceof ActionContributionItem) {
                    newItem = new ActionContributionItem(
                            ((ActionContributionItem) item).getAction());
                }
                newItem.fill(menu, -1);
            }
            return menu;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IMenuCreator#dispose()
         */
        public void dispose() {
            if (dropDownMenuMgr != null) {
                dropDownMenuMgr.dispose();
                dropDownMenuMgr = null;
            }
        }
    };
    
    /**
     * sets the active part based on the current page of the {@link uk.ac.kcl.cch.jb.pliny.views.AnnotationView}
     * (which is a PageBookView).
     * 
     * @param targetEditor
     */
    public void setActivePart(IWorkbenchPart targetEditor){
    	if(targetEditor == null){
    		thePage = null;
    		myPart = null;
    		return;
    	}
    	thePage = targetEditor.getSite().getPage();
    	//dropDownMenuMgr.removeAll();
    	if(!(targetEditor instanceof IResourceDrivenPart))return;
    	myPart = (IResourceDrivenPart)targetEditor;
    }

    /**
     * this run method is invoked when the user clicks on the icon associated
     * with this action and displays a message telling them
     * to click the dropdown item instead.
     */
	public void run(){
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.createContextMenu(thePage.getActivePart().getSite().getShell());
		
		buildMenu(menuManager);
		//Shell parentShell = Display.getDefault().getActiveShell();
		//MessageDialog.openInformation(parentShell,
		//		"How to use this item",
		//		"Click on the down arrow just to the right of the button to display "+
		//		"the list of items that display a surrogate of the item '"+
		//		myPart.getMyResource().getName()+"'");
	}
}
