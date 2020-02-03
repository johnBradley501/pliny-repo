package uk.ac.kcl.cch.jb.pliny.actions;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

public interface ITextFileGenerator {
	public boolean getOptions(Shell parentShell);
	public void run(IProgressMonitor monitor) throws FileNotFoundException, IOException;
}
