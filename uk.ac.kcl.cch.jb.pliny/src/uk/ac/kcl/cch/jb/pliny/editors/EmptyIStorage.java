package uk.ac.kcl.cch.jb.pliny.editors;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class EmptyIStorage implements IStorage {
	
	private static EmptyIStorage instance = new EmptyIStorage();
	
	public static EmptyIStorage getInstance(){
		return instance;
	}

	public InputStream getContents() throws CoreException {
		// TODO Auto-generated method stub
		return null; 
	}

	public IPath getFullPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
