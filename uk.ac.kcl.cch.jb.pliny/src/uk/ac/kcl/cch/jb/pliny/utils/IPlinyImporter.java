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

package uk.ac.kcl.cch.jb.pliny.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 * code that wishes to act as an importer for Pliny data must implement
 * this interface.  It will be called by the importer Action.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerImportAction
 * 
 * @author John Bradley
 *
 */
public interface IPlinyImporter {
	
	/**
	 * the importer will call this method when it is time to get
	 * options from the user that will control this importer.  The
	 * return value is <code>true</code> if the user has confirmed
	 * that the import is to go ahead.  Simply return <code>true</code>
	 * and do nothing else if there are no options to be provided.
	 * If there are options, this code should invoke a wizard to get
	 * them from the user.
	 * 
	 * @param parentShell Shell to be used to support display of a wizard
	 * for options.
	 * @return <code>true</code> if user wants to proceed to actual import
	 */
	public boolean getOptions(Shell parentShell);
	
	/**
	 * the export Action will call this method to provide a String
	 * containing the fully qualified fileName that is to be used
	 * for the import.
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName);
	
	/**
	 * the exportAction will call this method when it is time to do
	 * the import.
	 * 
	 * @param monitor monitor to be used during the import
	 * @throws PlinyImportException all internally arising exceptions should be converted
	 * to this exception
	 */
	public void run(IProgressMonitor monitor) throws PlinyImportException;
}
