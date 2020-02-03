/*******************************************************************************
 * Copyright (c) 2003, 2005, 2007 IBM Corporation, John Bradley and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     John Bradley - modifications to fit Pliny model
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.controls.ToolbarLayout;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

public class BrowserViewer extends Composite {
	protected static final String PREF_INTERNAL_WEB_BROWSER_HISTORY = "internalWebBrowserHistory"; //$NON-NLS-1$
    /**
     * Style parameter (value 1) indicating that the URL and Go button will be
     * on the local toolbar.
     */
    public static final int LOCATION_BAR = 1 << 1;

    /**
     * Style parameter (value 2) indicating that the toolbar will be available
     * on the web browser. This style parameter cannot be used without the
     * LOCATION_BAR style.
     */
    public static final int BUTTON_BAR = 1 << 2;
	 
	protected static final String PROPERTY_TITLE = "title"; //$NON-NLS-1$

    private static final int MAX_HISTORY = 50;

    public Clipboard clipboard;
    public Combo combo;
    protected boolean showToolbar = false;
    protected boolean showURLbar = false;
    protected ToolItem back;
    protected ToolItem forward;
    protected BusyIndicator busy;
    protected boolean loading;
    protected static java.util.List history;
    protected Browser browser;
    //protected BrowserText text;
    protected boolean newWindow;
    //protected IBrowserViewerContainer container;
    protected BrowserEditor container;
    protected String title;
    protected int progressWorked = 0;
    protected List propertyListeners;
    protected IBrowserToolbarContribution[] toolbarContributions;

    protected String currentHome = null;
	private static final String homePreferenceName="browserEditor.home";

    /**
     * Creates a new Web browser given its parent and a style value describing
     * its behavior and appearance.  This code is essentially taken from
     * the plugin org.eclipse.ui.browser, but is not accessible -- so needed
     * to be duplicated here.
     * <p>
     * The style value is either one of the style constants defined in the class
     * header or class <code>SWT</code> which is applicable to instances of
     * this class, or must be built by <em>bitwise OR</em>'ing together (that
     * is, using the <code>int</code> "|" operator) two or more of those
     * <code>SWT</code> style constants. The class description lists the style
     * constants that are applicable to the class. Style bits are also inherited
     * from superclasses.
     * </p>
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param toolbarContributions 
     */
    
    public BrowserViewer(Composite parent, BrowserEditor container, IBrowserToolbarContribution[] toolbarContributions){
        super(parent, SWT.NONE);
        initViewer(parent, container, toolbarContributions, LOCATION_BAR | BUTTON_BAR);
    }
    
    public BrowserViewer(Composite parent, BrowserEditor container, IBrowserToolbarContribution[] toolbarContributions, int style){
        super(parent, SWT.NONE);
        initViewer(parent, container, toolbarContributions, style);
    }
    
    private void initViewer(Composite parent, BrowserEditor container, IBrowserToolbarContribution[] toolbarContributions, int style){
        this.container = container;
        this.toolbarContributions = toolbarContributions;
        Shell parentShell = Display.getDefault().getActiveShell();
		  
        if ((style & LOCATION_BAR) != 0)
            showURLbar = true;

        if ((style & BUTTON_BAR) != 0)
            showToolbar = true;

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
        clipboard = new Clipboard(parent.getDisplay());
        
        if (showToolbar || showURLbar) {
            Composite toolbarComp = new Composite(this, SWT.NONE);
            toolbarComp.setLayout(new ToolbarLayout());
            toolbarComp.setLayoutData(new GridData(
                  GridData.VERTICAL_ALIGN_BEGINNING
                  | GridData.FILL_HORIZONTAL));

            if (showToolbar)
                createToolbar(toolbarComp);
            
			if (showURLbar)
            createLocationBar(toolbarComp);

			if (showToolbar | showURLbar) {
			    busy = new BusyIndicator(toolbarComp, SWT.NONE);
			    busy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			    /*
			    busy.addMouseListener(new MouseListener() {
					public void mouseDoubleClick(MouseEvent e) {
						// ignore
					}

					public void mouseDown(MouseEvent e) {
						setURL("http://www.eclipse.org"); //$NON-NLS-1$
					}

					public void mouseUp(MouseEvent e) {
						// ignore
					}
			    });
			    */
			}
			//PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
            //  ContextIds.WEB_BROWSER); 
        }

        // create a new SWT Web browser widget, checking once again to make sure
        // we can use it in this environment
        //if (WebBrowserUtil.canUseInternalWebBrowser())
        /* before version 3.3?   JB
        try {
            this.browser = new Browser(this, SWT.NONE);
        }
        catch (SWTError e) {
        	e.printStackTrace();
            MessageDialog.openError(parentShell, "Open","Could not open the browser");
            return;
        }*/
        try {
            this.browser = new Browser(this, SWT.MOZILLA); // tries to get Mozilla browser, if possible JB
        }
        catch (SWTError e) {
        	// System.out.println("Mozilla Failed:);");
        	// e.printStackTrace(System.out);
            try {
            	this.browser = new Browser(this, SWT.NONE);
            }
            catch (SWTError e2){
        	   e.printStackTrace();
               MessageDialog.openError(parentShell, "Open","Could not open the browser");
               return;
            }
        }

        if (showURLbar)
            updateHistory();
        if (showToolbar)
            updateBackNextBusy();

        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(browser,
        //       ContextIds.WEB_BROWSER);

        addBrowserListeners();
        //listen();
    }

    /**
     * Returns the underlying SWT browser widget.
     * 
     * @return the underlying browser
     */
    public Browser getBrowser() {
        return browser;
    }

    /**
     * Navigate to the home URL.
     */
    
    static public String getHomeFromPreferences(){
        String rslt = PlinyPlugin.getDefault().getPluginPreferences().getString(homePreferenceName);
        if(rslt.length() == 0){
        	rslt = "http://pliny.cch.kcl.ac.uk";
        	saveHomeToPreferences(rslt);
        }
        return rslt;
    }
    
    static private void saveHomeToPreferences(String url) {
    	PlinyPlugin.getDefault().getPluginPreferences().setValue(homePreferenceName,url);
	}

	public void home() {
    	if(currentHome == null){
    		currentHome = getHomeFromPreferences();
    	}
    	setURL(currentHome);
    }

	public void setHome(String url) {
		currentHome = url;
		saveHomeToPreferences(url);
	}

    /**
     * Loads a URL.
     * 
     * @param url
     *            the URL to be loaded
     * @return true if the operation was successful and false otherwise.
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the url is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #getURL()
     */
    public void setURL(String url) {
       setURL(url, true);
    }

    protected void updateBackNextBusy() {
        back.setEnabled(isBackEnabled());
        forward.setEnabled(isForwardEnabled());
        busy.setBusy(loading);

        //if (backNextListener != null)
        //    backNextListener.updateBackNextBusy();
    }

    /*
    protected void updateLocation() {
        if (locationListener != null)
            locationListener.historyChanged(null);

        if (locationListener != null)
            locationListener.locationChanged(null);
    }
    */

    /**
     *
     */
    private void addBrowserListeners() {
        if (browser==null) return;
        // respond to ExternalBrowserInstance StatusTextEvents events by
        // updating the status line
        browser.addStatusTextListener(new StatusTextListener() {
            public void changed(StatusTextEvent event) {
					//System.out.println("status: " + event.text); //$NON-NLS-1$
                if (container != null) {
                    IStatusLineManager status = container.getActionBars()
                            .getStatusLineManager();
                    status.setMessage(event.text);
                }
            }
        });

        // Add listener for new window creation so that we can instead of
        // opening a separate
        // new window in which the session is lost, we can instead open a new
        // window in a new
        // shell within the browser area thereby maintaining the session.
        browser.addOpenWindowListener(new OpenWindowListener() {
            public void open(WindowEvent event) {
            	//if((event.size == null) && (event.location == null) &&
            	//		(event.addressBar) && (event.toolBar)){
            	   URL nullURL = null;
            	   BrowserEditor part = null;
				   try {
					part = (BrowserEditor)container.getEditorSite().getPage().
					    openEditor(new BrowserEditorInput(nullURL), BrowserEditor.BROWSER_ID);
				   } catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				   }
				   if(part != null)event.browser = part.webBrowser.browser;
            	/*} else {
                Shell shell2 = new Shell(getDisplay());
                shell2.setLayout(new FillLayout());
                shell2.setText("Web Browser");
                shell2.setImage(getShell().getImage());
                if (event.location != null)
                    shell2.setLocation(event.location);
                if (event.size != null)
                    shell2.setSize(event.size);
				int style = 0;
				if (event.addressBar)
					style += LOCATION_BAR;
				if (event.toolBar)
					style += BUTTON_BAR;
                BrowserViewer browser2 = new BrowserViewer(shell2, container,toolbarContributions, style);
                browser2.newWindow = true;
                event.browser = browser2.browser;
            	} */
            }
        });
		  
		  browser.addVisibilityWindowListener(new VisibilityWindowListener() {
				public void hide(WindowEvent e) {
					// ignore
				}
				
				public void show(WindowEvent e) {
					Browser browser2 = (Browser)e.widget;
					if (browser2.getParent().getParent() instanceof Shell) {
						Shell shell = (Shell) browser2.getParent().getParent();
						if (e.location != null)
							shell.setLocation(e.location);
						if (e.size != null)
							shell.setSize(shell.computeSize(e.size.x, e.size.y));
						shell.open();
					}
				}
			});

        browser.addCloseWindowListener(new CloseWindowListener() {
            public void close(WindowEvent event) {
                // if shell is not null, it must be a secondary popup window,
                // else its an editor window
                if (newWindow)
                    getShell().dispose();
                else
                    container.close();
            }
        });

        browser.addProgressListener(new ProgressListener() {
            public void changed(ProgressEvent event) {
					//System.out.println("progress: " + event.current + ", " + event.total); //$NON-NLS-1$ //$NON-NLS-2$
                if (event.total == 0)
                    return;

                boolean done = (event.current == event.total);

                int percentProgress = event.current * 100 / event.total;
                if (container != null) {
                    IProgressMonitor monitor = container.getActionBars()
                            .getStatusLineManager().getProgressMonitor();
                    if (done) {
                        monitor.done();
                        progressWorked = 0;
                    } else if (progressWorked == 0) {
                        monitor.beginTask("", event.total); //$NON-NLS-1$
                        progressWorked = percentProgress;
                    } else {
                        monitor.worked(event.current - progressWorked);
                        progressWorked = event.current;
                    }
                }

                if (showToolbar) {
                    if (!busy.isBusy() && !done)
                        loading = true;
                    else if (busy.isBusy() && done) // once the progress hits
                        // 100 percent, done, set
                        // busy to false
                        loading = false;

						  //System.out.println("loading: " + loading); //$NON-NLS-1$
                    updateBackNextBusy();
                    updateHistory();
                }
            }

            public void completed(ProgressEvent event) {
                if (container != null) {
                    IProgressMonitor monitor = container.getActionBars()
                            .getStatusLineManager().getProgressMonitor();
                    monitor.done();
                }
                if (showToolbar) {
                    loading = false;
                    updateBackNextBusy();
                    updateHistory();
                }
            
                VirtualBrowserResource myResource = (VirtualBrowserResource)container.getCurrentResource();
                if(myResource.getALID() != 0){
            	    //PlinyPlugin.getDefault().createThumbnail(getBrowser(), myResource);
                	myResource.createThumbnail(getBrowser());
                }
            }
            
        });

        if (showToolbar) {
            browser.addLocationListener(new LocationListener() {
                public void changed(LocationEvent event) {
                   if (!event.top)
                        return;
                   container.updateLocation(event.location);
                   if (combo != null) {
                        if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
                            combo.setText(event.location);
                            addToHistory(event.location);
                            updateHistory();
                        }// else
                        //    combo.setText(""); //$NON-NLS-1$
                    }
                }

                public void changing(LocationEvent event) {
                    // do nothing
                }
            });
        }

        browser.addTitleListener(new TitleListener() {
            public void changed(TitleEvent event) {
            	     if(title != null && title.equals(event.title))return;
            	     //System.out.println("TitleChanged: "+event.title);
					 String oldTitle = title;
					 title = event.title;
					 container.updateTitle(title);
					 firePropertyChangeEvent(PROPERTY_TITLE, oldTitle, title);
            }
        });
    }
	 
	 /**
		 * Add a property change listener to this instance.
		 *
		 * @param listener java.beans.PropertyChangeListener
		 */
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			if (propertyListeners == null)
				propertyListeners = new ArrayList();
			propertyListeners.add(listener);
		}

		/**
		 * Remove a property change listener from this instance.
		 *
		 * @param listener java.beans.PropertyChangeListener
		 */
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			if (propertyListeners != null)
				propertyListeners.remove(listener);
		}

		/**
		 * Fire a property change event.
		 */
		protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
			if (propertyListeners == null)
				return;

			PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
			//Trace.trace("Firing: " + event + " " + oldValue);
			try {
				int size = propertyListeners.size();
				PropertyChangeListener[] pcl = new PropertyChangeListener[size];
				propertyListeners.toArray(pcl);
				
				for (int i = 0; i < size; i++)
					try {
						pcl[i].propertyChange(event);
					} catch (Exception e) {
						// ignore
					}
			} catch (Exception e) {
				// ignore
			}
		}

    /**
     * Navigate to the next session history item. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #back
     */
    public boolean forward() {
        if (browser==null)
            return false;
        return browser.forward();
    }

    /**
     * Navigate to the previous session history item. Convenience method that
     * calls the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #forward
     */
    public boolean back() {
        if (browser==null)
            return false;
        return browser.back();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the previous
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's back command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @see #back
     */
    public boolean isBackEnabled() {
        if (browser==null)
            return false;
        return browser.isBackEnabled();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the next
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's forward command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @see #forward
     */
    public boolean isForwardEnabled() {
        if (browser==null)
            return false;
        return browser.isForwardEnabled();
    }

    /**
     * Stop any loading and rendering activity. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void stop() {
        if (browser!=null)
            browser.stop();
    }

    /**
     * 
     */
    private boolean navigate(String url) {
        //Trace.trace(Trace.FINER, "Navigate: " + url); //$NON-NLS-1$
        if (url != null && url.equals(getURL())) {
            refresh();
            return true;
        }
        if (browser!=null)
            return browser.setUrl(url);
        return false;
    }
 
    /**
     * Refresh the current page. Convenience method that calls the underlying
     * SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void refresh() {
        if (browser!=null)
            browser.refresh();
		  try {
			  Thread.sleep(50);
		  } catch (Exception e) {
			  // ignore
		  }
    }

    private void setURL(String url, boolean browse) {
        //Trace.trace(Trace.FINEST, "setURL: " + url + " " + browse); //$NON-NLS-1$ //$NON-NLS-2$
        if (url == null) {
            home();
            return;
        }

        if ("eclipse".equalsIgnoreCase(url)) //$NON-NLS-1$
            url = "http://www.eclipse.org"; //$NON-NLS-1$
        else if ("wtp".equalsIgnoreCase(url)) //$NON-NLS-1$
            url = "http://www.eclipse.org/webtools/"; //$NON-NLS-1$

        if (browse)
            navigate(url);

        addToHistory(url);
        updateHistory();
    }
    
    private List getInternalWebBrowserHistory() {
    	String temp = 
    	   PlinyPlugin.getDefault().getPreferenceStore().
    	   getString(PREF_INTERNAL_WEB_BROWSER_HISTORY);
		StringTokenizer st = new StringTokenizer(temp, "|*|"); //$NON-NLS-1$
		List l = new ArrayList();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			l.add(s);
		}
		return l;
    }
    
	public static void setInternalWebBrowserHistory(List list) {
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String s = (String) iterator.next();
				sb.append(s);
				sb.append("|*|"); //$NON-NLS-1$
			}
		}
 	    PlinyPlugin.getDefault().getPreferenceStore().
		setValue(PREF_INTERNAL_WEB_BROWSER_HISTORY,
				sb.toString());
 	    PlinyPlugin.getDefault().savePluginPreferences();
	}


    protected void addToHistory(String url) {
        if (history == null)
            history = getInternalWebBrowserHistory();
        int found = -1;
        int size = history.size();
        for (int i = 0; i < size; i++) {
            String s = (String) history.get(i);
            if (s.equals(url)) {
                found = i;
                break;
            }
        }

        if (found == -1) {
            if (size >= MAX_HISTORY)
                history.remove(size - 1);
            history.add(0, url);
            setInternalWebBrowserHistory(history);
        } else if (found != 0) {
            history.remove(found);
            history.add(0, url);
            setInternalWebBrowserHistory(history);
        }
    }

    /**
     *
     */
    public void dispose() {
        super.dispose();

        showToolbar = false;

        if (busy != null)
            busy.dispose();
        busy = null;

        browser = null;
        if (clipboard!=null)
        	clipboard.dispose();
        clipboard=null;

        removeSynchronizationListener();
    }
    
    private Image getImage(String path, String name){
    	String fullPath = "icons/browser/"+path+"16/"+name+".gif";
    	ImageDescriptor id = PlinyPlugin.getImageDescriptor(fullPath);
    	return id.createImage();
    }

    private ToolBar createLocationBar(Composite parent) {
        combo = new Combo(parent, SWT.DROP_DOWN);
        new URLDropTargetListener(combo, this);

        updateHistory();

        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent we) {
                try {
                    if (combo.getSelectionIndex() != -1)
                        setURL(combo.getItem(combo.getSelectionIndex()));
                } catch (Exception e) {
                    // ignore
                }
            }
        });
        combo.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                setURL(combo.getText());
            }
        });
        
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

        ToolItem go = new ToolItem(toolbar, SWT.NONE);
        go.setImage(getImage("elcl","nav_go"));
        go.setHotImage(getImage("clcl","nav_go"));
        go.setDisabledImage(getImage("dlcl", "nav_go"));
        go.setToolTipText("go to the selected URL");
        go.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setURL(combo.getText());
            }
        });
		  
		  return toolbar;
    }

    private ToolBar createToolbar(Composite parent) {
		  ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
		  
        // create back and forward actions
        back = new ToolItem(toolbar, SWT.NONE);
        back.setImage(getImage("elcl", "nav_backward"));
        back.setHotImage(getImage("clcl", "nav_backward"));
        back.setDisabledImage(getImage("dlcl", "nav_backward"));
        back.setToolTipText("Back to the previous page");
        back.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                back();
            }
        });

        forward = new ToolItem(toolbar, SWT.NONE);
        forward.setImage(getImage("elcl", "nav_forward"));
        forward.setHotImage(getImage("clcl", "nav_forward"));
        forward.setDisabledImage(getImage("dlcl", "nav_forward"));
        forward.setToolTipText("Forward to the next page");
        forward.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                forward();
            }
        });

        // create refresh, stop, and print actions
        ToolItem stop = new ToolItem(toolbar, SWT.NONE);
        stop.setImage(getImage("elcl", "nav_stop"));
        stop.setHotImage(getImage("clcl", "nav_stop"));
        stop.setDisabledImage(getImage("dlcl", "nav_stop"));
        stop.setToolTipText("Stop loading the current page");
        stop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                stop();
            }
        });

        ToolItem refresh = new ToolItem(toolbar, SWT.NONE);
        refresh.setImage(getImage("elcl", "nav_refresh"));
        refresh.setHotImage(getImage("clcl", "nav_refresh"));
        refresh.setDisabledImage(getImage("dlcl", "nav_refresh"));
        refresh.setToolTipText("Refresh the current page");
        refresh.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

        ToolItem homeIcon = new ToolItem(toolbar, SWT.DROP_DOWN);
        homeIcon.setImage(getImage("elcl", "nav_home"));
        homeIcon.setHotImage(getImage("clcl", "nav_home"));
        homeIcon.setDisabledImage(getImage("dlcl", "nav_home"));
        homeIcon.setToolTipText("Go to the Home Page");
        homeIcon.addSelectionListener(new HomeMenuSelectionListener(homeIcon, this));
    
        if((toolbarContributions != null) && (toolbarContributions.length > 0)){
        	new ToolItem(toolbar, SWT.SEPARATOR);
        	for(int i = 0; i < toolbarContributions.length; i++){
        		ToolItem contr = new ToolItem(toolbar, SWT.NONE);
        		toolbarContributions[i].setupToolItem(contr);
        		contr.addSelectionListener(new ContributedSelectionAdapter(i));
        	}
        }
		  
		  return toolbar;
    }
    
    private class ContributedSelectionAdapter extends SelectionAdapter {
    	private int i;
    	public ContributedSelectionAdapter(int i){
    		this.i = i;
    	}

    	public void widgetSelected(SelectionEvent event) {
    		runToolbarItem(i);
        }
    }
    
    public void runToolbarItem(int i){
    	IWorkbenchPage page = container.getEditorSite().getPage();
    	toolbarContributions[i].run(page, this);
    }

    /**
     * Returns the current URL. Convenience method that calls the underlying SWT
     * browser.
     * 
     * @return the current URL or an empty <code>String</code> if there is no
     *         current URL
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #setURL(String)
     */
    public String getURL() {
        if (browser!=null)
            return browser.getUrl();
        return null;
    }

    public boolean setFocus() {
        if (combo != null)
            combo.setFocus();
        else if (browser!=null)
            browser.setFocus();
        updateHistory();
        return super.setFocus();
    }

    /**
     * Update the history list to the global/shared copy.
     */
    protected void updateHistory() {
        if (combo == null)
            return;

        String temp = combo.getText();
        if (history == null)
            history = getInternalWebBrowserHistory();

        String[] historyList = new String[history.size()];
        history.toArray(historyList);
        combo.setItems(historyList);

        combo.setText(temp);
    }

    public BrowserEditor getContainer() {
        return container;
    }

    public void setContainer(BrowserEditor container) {
        this.container = container;
    }

    protected File file;
    protected long timestamp;
    protected Thread fileListenerThread;
    protected LocationListener locationListener2;
    protected Object syncObject = new Object();
    
    protected void addSynchronizationListener() {
   	 if (fileListenerThread != null)
   		 return;
   	 
   	 fileListenerThread = new Thread("Browser file synchronization") { //$NON-NLS-1$
   		 public void run() {
   			 while (fileListenerThread != null) {
   				 try {
   					 Thread.sleep(2000);
   				 } catch (Exception e) {
   					 // ignore
   				 }
   				 synchronized (syncObject) {
						 if (file != null && file.lastModified() != timestamp) {
	   					 timestamp = file.lastModified();
	   					 Display.getDefault().syncExec(new Runnable() {
	 							public void run() {
	 								refresh();
	 							}
	   					 });
						 }
					  }
   			 }
   		 }
   	 };
   	 fileListenerThread.setDaemon(true);
   	 fileListenerThread.setPriority(Thread.MIN_PRIORITY);
   	 
   	 locationListener2 = new LocationListener() {
          public void changed(LocationEvent event) {
         	 File temp = getFile(event.location);
         	 if (temp != null && temp.exists()) {
         		 synchronized (syncObject) {
         			 file = temp;
            		 timestamp = file.lastModified();
					 }
         	 } else
         		 file = null;
          }
          
          public void changing(LocationEvent event) {
             // do nothing
         }
       };
       browser.addLocationListener(locationListener2);
       
       File temp = getFile(browser.getUrl());
   	 if (temp != null && temp.exists()) {
   		file = temp;
      	timestamp = file.lastModified();
   	 }
   	 fileListenerThread.start();
    }

    protected static File getFile(String location) {
   	 if (location == null)
   		 return null;
   	 if (location.startsWith("file:/")) //$NON-NLS-1$
   		 location = location.substring(6);
   	 
   	 return new File(location);
    }

    protected void removeSynchronizationListener() {
   	 if (fileListenerThread == null)
   		 return;
   	 
   	 fileListenerThread = null;
   	 browser.removeLocationListener(locationListener2);
   	 locationListener2 = null;
    }
    
    //public void giveBusyIndicatorResource(Resource resource){
    //	busy.setResource(resource);
    //}
}
