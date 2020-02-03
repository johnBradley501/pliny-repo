package uk.ac.kcl.cch.jb.pliny.model;

import uk.ac.kcl.cch.rdb2java.dynData.IAuthorityListItem;

public interface IHasAttribute extends IAuthorityListItem{
	public String getAttributes();
	
	public void setAttributes(String parm);
}
