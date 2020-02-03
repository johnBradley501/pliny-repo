package uk.ac.kcl.cch.jb.pliny.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;
import uk.ac.kcl.cch.rdb2java.dynData.ILoadableFromResultSet;

public class ModelDataIterator implements Iterator {
	
	private BaseQuery bq;
	private ResultSet rs;
	private ILoadableFromResultSet nextRslt = null;

	public ModelDataIterator(BaseQuery bq){
		this.bq = bq;
		rs = Rdb2javaPlugin.getDataServer().prepareFullValueResultSet(bq);
		getNextItem();
    }
	
	private void getNextItem(){
		try {
			if(rs.next()){
				nextRslt = bq.getMyNewDataObject();
				nextRslt.loadFromResultSet(rs);
				if(nextRslt instanceof Resource){ // special handling for REsource needed. jb
					nextRslt = handleResourceType((Resource)nextRslt, rs);
					
				}
			} else {
				nextRslt = null;
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Resource handleResourceType(Resource d, ResultSet rs) throws SQLException{
		Resource rslt = null;
		if(d.getObjectTypeKey() == 1) // We can ignore (here) that a Note is really a 2-table fetching process.
			return d;
		IResourceExtensionProcessor proc = PlinyPlugin.getResourceExtensionProcessor(d.getObjectTypeKey());
		if(proc == null)return d;
		rslt = proc.makeMyResource();
		if(rslt == null)return d;
		rslt.loadFromResultSet(rs);
		return rslt;
	}

	public boolean hasNext() {
		return nextRslt != null;
	}

	public Object next() {
		ILoadableFromResultSet rslt = nextRslt;
		getNextItem();
		return rslt;
	}

	public void remove() {
		// TODO Auto-generated method stub

	}
	
	public void dispose(){
		if(rs == null)return;
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = null;
	}

}
