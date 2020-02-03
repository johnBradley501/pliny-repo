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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * items that participate as model items in the Resource Explorer's tree display
 * should implement this interface.
 * 
 * @author John Bradley
 */

public interface IResourceExplorerItem {
	
	/**
	 * returns text to be used as the textual label for the tree item
	 * display.
	 * 
	 * @return String text to be displayed.
	 */
     public String getText();
     
     /**
      * changes text to be displayed as the label to the given text.
      * This should result in changes in the backing Pliny model as well.
      * 
      * @param name text to be used as the name of this item.
      */
     public void setText(String name);
     
     /**
      * return <code>true</code> if this item's name can be edited within
      * the Resource Explorer.
      * 
      * @return <code>true</code> if editable.
      */
     public boolean canModify();
     
     /**
      * returns the Pliny model object associated with this item.  Return
      * <code>null</code> if there is not Pliny model object.
      * 
      * @return Object representing associated Pliny model object.
      */
     public Object getAssociatedObject();
     
     /**
      * return the page number data associated with the Pliny model
      * object associated wtih this item.  Return 0 if there is no associated
      * page number.
      * 
      * @return page number associated with the item.
      */
     public int getPageNumber();
     
     /**
      * performs data cleanup.  This will be called then the item is
      * no longer needed.
      *
      */
     public void dispose();
     
     /**
      * returns the Image to be used as the Icon displayed beside the item
      * in the Resource Explorer tree.  The image must be disposed of
      * by this object -- it will not be disposed by the caller.
      * 
      * @return SWT Image to act as the icon.
      */
     public Image getIcon();
     
     /**
      * returns the parent item of this item in the Resource Explorer display
      * tree.
      */
     public IResourceExplorerItem getParent();
     
     /**
      * returns <code>true</code> if this item has children items.
      */
     public boolean hasChildren();
     
     /**
      * returns number of children owned by this item.
      */
     public int getNumberChildren();
     
     /**
      * returns a list of IResourceExplorerItem items that are children
      * of this item.
      */
     public List getChildren();
     
}
