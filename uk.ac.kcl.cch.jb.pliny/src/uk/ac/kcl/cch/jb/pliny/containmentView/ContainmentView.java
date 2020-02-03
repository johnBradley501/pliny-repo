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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.IncludedTypeManager;
import uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This is an PageBookView that manages Pliny's Containment View.
 * With PageBookViews, Eclipse provides a linking mechanism between the
 * main editor pane and this view so that is notified each
 * time the user changes to a new Editor on a new Object, and can change
 * its display to synchronize with the editor pane's focus. 
 * Specifially w.r.t. this ContainmentView, for those
 * objects that are Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s it can generate its display.
 * A all PageBookViews are supposed to do, it creates one instance of 
 * <code>ContainmentViewPage</code> for
 * each Resource it is given during its operation, and (by means of
 * inherited code) will switch between them as the user switches betwee
 * different Pliny Resources.
 * <p>
 * Much of the code to generate the display and lay out the data
 * is recycled from the example project <code>com.realpersist.gef.schemaeditor</code>.
 * We thank the authors of this project for their example.  The code here
 * does not, however, actually inherit any code from this project, and it need
 * not be included in the build path for this item to work.
 * </p>
 * <p>
 * Linked editors that can change their resources during their operation
 * (identifed as being instances of {@link uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart})
 * provide particualar challenges as the display must completely change
 * each time the main editor changes its resource.  To handle this this
 * ViewPart will link to an editor of this type and use the service
 * it provides to be told when a resource changes.
 * </p>
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewMenuProvider
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewPage
 * 
 * @author John Bradley
 */

public class ContainmentView extends PageBookView
implements PropertyChangeListener{

	private static String defaultText =
		"ContainmentView: cannot show containment structure for current object";

	private IWorkbenchPart currentPart = null;
	private IncludedTypeManager typeManager = null;

	public ContainmentView() {
		super();
		typeManager = new IncludedTypeManager();
	}

	
	public void dispose(){
		super.dispose();
		if(typeManager != null)typeManager.dispose();
		typeManager = null;
	}
	
	/**
	 * gets the IncludedTypeManager in effect at present, or creates one
	 * if there is none at present, and returns it.
	 * The IncludedTypeManager controls which kind of LinkableObjects
	 * are to be displayed -- according to their LOType.
	 * 
	 * @return IncludedTypeManager the current manager.
	 */
	
	public IncludedTypeManager getTypeManager(){
		if(typeManager == null)typeManager = new IncludedTypeManager();
		return typeManager;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(defaultText);
        return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart part) {
		if(!(part instanceof IResourceDrivenPart))return null;
		Resource resource = ((IResourceDrivenPart)part).getMyResource();
		//if(resource instanceof VirtualResource && resource.getALID() == 0)return null;
		return new PageRec(part, new ContainmentViewPage(resource, getPageBook(), this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		if(pageRecord.page instanceof ContainmentViewPage)
			pageRecord.page.dispose();
		pageRecord.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null)
            return page.getActiveEditor();

        return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		if(part instanceof EditorPart)return true;
		if(part instanceof IResourceDrivenPart)return true;
		return false;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		currentPart = part;
		if(getCurrentPage() instanceof ContainmentViewPage){
			ContainmentViewPage myPage = (ContainmentViewPage)getCurrentPage();
			myPage.updateCommandStackActions();
			myPage.updateUndoActions();
		}
		if(part instanceof IResourceChangeablePart)
			((IResourceChangeablePart)part).addPropertyChangeListener(this);
	}
	  
	public void partDeactivated(IWorkbenchPart part){
		super.partDeactivated(part);
		if(currentPart == part)currentPart = null;
		if(part instanceof IResourceChangeablePart)
			((IResourceChangeablePart)part).removePropertyChangeListener(this);
	}
	
	public Object getAdapter(Class adapter){
		Object rslt = null;
		if(!(getCurrentPage() instanceof ContainmentViewPage))return super.getAdapter(adapter);
		ContainmentViewPage myCurrentPage = (ContainmentViewPage)getCurrentPage();
		if(myCurrentPage != null)rslt = myCurrentPage.getAdapter(adapter);
		if(rslt != null)return rslt;
		return super.getAdapter(adapter);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == IResourceChangeablePart.CHANGE_EVENT &&
		getCurrentPage() instanceof ContainmentViewPage){
			Resource newResource = (Resource)arg0.getNewValue();
		   ((ContainmentViewPage)getCurrentPage()).updateResource(newResource);
		}
		
	}


}
