/*******************************************************************************
 * Copyright (c) 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.data.cloud;

import java.util.Vector;

import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;
import uk.ac.kcl.cch.rdb2java.dynData.IPersistentQuery;

public class CloudPersistentQuery implements IPersistentQuery {

	private CloudServices cloudServices;
	private BaseQuery bq;
	
	public CloudPersistentQuery(CloudServices cloudServices, BaseQuery bq, String foreignKeyName) {
		this.cloudServices = cloudServices;
		this.bq = bq;
		bq.addConstraintParam(foreignKeyName, BaseQuery.FilterEQUAL);
	}

	@Override
	public Vector executeQuery(int ID) {
		bq.clearQueryParams();
		bq.addQueryParam(ID);
		return cloudServices.runQuery(bq);
	}

	@Override
	public int executeCount(int ID) {
		return -1; // not implemented
	}

}
