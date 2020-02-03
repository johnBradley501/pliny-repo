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

/**
 * code that wishes to act as an exporter for Pliny data must implement
 * this interface. It will be called by the exporter Action.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.actions.PlinyExportAction
 * 
 * @author John Bradley
 *
 */

public interface IPlinyExporter {
	/**
	 * call this method to provide the data neededS
	 * to allow the export to proceed.
	 * 
	 * @param provider The source of the data to be exported
	 * @param fileName The fully qualified name of the file to receive the exported data.
	 */

	public void prepareRun(IPlinyExporterDataProvider provider, String fileName);
	
	/**
	 * cll this method when it is time to do
	 * the export.
	 * 
	 * @param monitor monitor to be used during the export
	 * @throws PlinyExportException all internally arising exceptions should be converted
	 * to this exception
	 */
	public void run(IProgressMonitor monitor) throws PlinyExportException;

	/**
	 * call this method when then export action is finished.
	 */
	public void finishRun();
}
