package uk.ac.kcl.cch.jb.pliny.data;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.Preferences;

import uk.ac.kcl.cch.jb.pliny.GeneralPreferencesPage;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.cloud.CloudServices;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.SigninException;

public class DataServerSetupManager {
	   static final String PASSWORD = "password";
	   static final String SERVER = "server";
	   static final String USER = "user";

	   static final String STORAGE_TYPE = "storage_type";
	   static final String LOCAL_STORAGE = "local";
	   static final String CLOUD_STORAGE = "cloud";
	   
	   private PlinyPlugin plinyPlugin;
	   private String dbName;
	   
	public DataServerSetupManager(String dbName, PlinyPlugin pp){
		plinyPlugin = pp;
		this.dbName = dbName;
	}
	
	public IDataServerWithCaching establishDataServer(){
		IPreferencesService service = Platform.getPreferencesService();
		//boolean autologin = service.getBoolean(PlinyPlugin.PLUGIN_ID,
		//		GeneralPreferencesPage.AUTO_LOGIN, false, null);
		boolean autologin = service.getBoolean(PlinyPlugin.PLUGIN_ID,
				GeneralPreferencesPage.AUTO_LOGIN, true, null); // changed to hide autologin on the first startup.  jb
		if(!autologin){
			Dialog dialog = new DataServerDialog(null);
			int dresult = dialog.open();
		}
		IDataServerWithCaching rslt = null;
		int tries = 4;
		while(rslt == null && tries > 0){
			tries--;
			Preferences preferences = ConfigurationScope.INSTANCE.getNode(PlinyPlugin.PLUGIN_ID);
			String storageType = preferences.get(STORAGE_TYPE, LOCAL_STORAGE);
			if(storageType.equals(LOCAL_STORAGE)){
				rslt = new DBServices(dbName, plinyPlugin);
			} else {
				String user = preferences.get(USER, "");
				String server = preferences.get(SERVER,"");
				String password = preferences.get(PASSWORD,"");

				rslt = new CloudServices(server, user, password);
				if(rslt == null){
					Dialog dialog = new DataServerDialog(null);
					int dresult = dialog.open();
				}
			}
			Rdb2javaPlugin.setDataServer(rslt);
			try {
				rslt.start();
			} catch (SigninException e) {
				Shell parentShell = Display.getDefault().getActiveShell();
				MessageBox message = new MessageBox(parentShell, SWT.OK | SWT.ICON_ERROR);
				message.setMessage("The data connection failed. Tries remaining are "+tries+". Reason given was: "+e.getMessage());
				message.open();
				if(rslt != null)rslt.dispose();
				rslt = null;
				
				Dialog dialog = new DataServerDialog(null);
				int dresult = dialog.open();
			}
		}
		return rslt;
	}

}
