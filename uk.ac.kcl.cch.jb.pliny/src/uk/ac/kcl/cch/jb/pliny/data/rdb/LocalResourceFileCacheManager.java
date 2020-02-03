package uk.ac.kcl.cch.jb.pliny.data.rdb;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager;
import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;

public class LocalResourceFileCacheManager implements IFileCacheManager {
	
	private static final int bufLength = 1024*1024;
	
	private File cachePath = null;
	private Plugin owner = null;
	private String myType = null;
	private String thumbFileNamePrefix = "t";
	
	public LocalResourceFileCacheManager(Plugin owner, String cacheName, String myType){
		this.owner = owner;
		cachePath = new File(owner.getStateLocation().toString(), cacheName);
		if(!cachePath.exists()){
			cachePath.mkdirs();
		}
		this.myType = myType;
	}
	
	public LocalResourceFileCacheManager(Plugin owner){
		this.owner = owner;
	}
	
	protected void setupTemporaryCacheArea(File cacheArea, String cacheName, String myType){
		this.cachePath = new File(cacheArea, cacheName);
		if(!cachePath.exists()){
			cachePath.mkdirs();
		}
		this.myType = myType;
		
	}
	
	protected String getMyType(){
		return myType;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#createCacheItem(java.net.URL, int, java.lang.String, java.lang.String)
	 *
	//@Override
	public boolean createCacheItem(URL url, int cacheNo, String ext){
		File theFile = new File(cachePath,myType+cacheNo+"."+ext);
		boolean success = false;
		try {
			PlinyPlugin.copyInputStream(url.openStream(), new FileOutputStream(theFile));
			success = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if((!success) && theFile.exists())
			theFile.delete();
		else sendFileToBackingCache(theFile, cacheNo);
		return success;
	}
	 */
	
	// http://www.java2s.com/Tutorial/Java/0300__SWT-2D-Graphics/WriteanImagetoaPNGfile.htm
	
	public boolean createCacheItem(ImageData data, ICachingResource resource){
		IPath p = new Path(cachePath.toString()).append(myType+resource.getCacheNumber()+"."+resource.getExtension());
		String fName = p.toString();
		
	    ImageLoader loader = new ImageLoader();
	    loader.data = new ImageData[] {data};
	    loader.save(fName, data.type);
	    return true;
	}
	
	@Override
	public boolean createCacheItem(InputStream in, ICachingResource resource){
		File theFile = new File(cachePath,myType+resource.getCacheNumber()+"."+resource.getExtension());
		boolean success = false;
		try {
			PlinyPlugin.copyInputStream(in, new FileOutputStream(theFile));
			success = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if((!success) && theFile.exists())
			theFile.delete();
		else sendFileToBackingCache(theFile, resource.getCacheNumber());
		return success;
	}
	
	protected void getFileFromBackingCache(File theFile, int cacheNo){
		// nothing to do here.  jb
	}

	
	protected void sendFileToBackingCache(File theFile, int cacheNo) {
		// nothing to do in here.  jb
	}
	
	protected void getThumbnailFromBackingCache(File theFile, int cacheNo){
		// nothing to do here.  jb
	}

	
	protected void sendThumbnailToBackingCache(File theFile, int cacheNo) {
		// nothing to do in here.  jb
	}

	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#getCacheFile(int, java.lang.String, java.lang.String)
	 */
	@Override
	public File getCacheFile(int cacheNo, String ext){
		String fileName = myType+cacheNo+"."+ext;
		File theFile = new File(cachePath, fileName);
		if(!theFile.exists())getFileFromBackingCache(theFile, cacheNo);
		
		return theFile;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#getCacheFile(uk.ac.kcl.cch.jb.pliny.model.ICachingResource, java.lang.String)
	 */
	@Override
	public File getCacheFile(ICachingResource resource){
		return getCacheFile(resource.getCacheNumber(),resource.getExtension());
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#createThumbnail(uk.ac.kcl.cch.jb.pliny.model.ICachingResource, java.awt.image.BufferedImage)
	 */
	@Override
	public ImageDescriptor createThumbnail(ICachingResource resource, BufferedImage thumbImage){
		String thumbName = thumbFileNamePrefix+resource.getCacheNumber()+".jpg";

		File thumbFile = new File(cachePath,thumbName);
		try {
			ImageIO.write(thumbImage,"jpg", thumbFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if(thumbFile.exists()){
			sendThumbnailToBackingCache(thumbFile, resource.getCacheNumber());
			return ImageDescriptor.createFromFile(null, thumbFile.getAbsolutePath());
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#createThumbnail(uk.ac.kcl.cch.jb.pliny.model.ICachingResource, org.eclipse.swt.graphics.ImageData)
	 */
	@Override
	public ImageDescriptor createThumbnail(ICachingResource resource, ImageData thumbData){
		String thumbName = thumbFileNamePrefix+resource.getCacheNumber()+".jpg";

		File thumbFile = new File(cachePath,thumbName);
		String thumbPath = null;
		try {
			thumbPath = thumbFile.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{thumbData};
		loader.save(thumbPath, SWT.IMAGE_JPEG);
		sendThumbnailToBackingCache(thumbFile, resource.getCacheNumber());
		/*
		 * One would think that having created and saved the image, and having the
		 * imageData, that the createFromImageData could be returned.  Indeed, this
		 * >usually< works, but it doesn't seem to when the image goes through the
		 * depth==1 code above.  Getting the image by getting the ImageDescriptor
		 * from the file (via a URL) seems to work in both cases!   ... jb
		 */
		URL theUrl = null;
		try {
			theUrl = new URL("file:///"+thumbPath);
		} catch (MalformedURLException e) {
			return ImageDescriptor.createFromImageData(thumbData);
		}
		return ImageDescriptor.createFromURL(theUrl);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.data.rdb.IFileCacheManager#getThumbnailFile(uk.ac.kcl.cch.jb.pliny.model.ICachingResource)
	 */
	@Override
	public File getThumbnailFile(ICachingResource resource){
		String thumbName = thumbFileNamePrefix+resource.getCacheNumber()+".jpg";
		File theFile = new File(cachePath, thumbName);
		if(!theFile.exists())getThumbnailFromBackingCache(theFile, resource.getCacheNumber());
		return theFile;
	}

	@Override
	public ImageDescriptor createThumbnail(ICachingResource resource,
			InputStream in) {
		String thumbName = thumbFileNamePrefix+resource.getCacheNumber()+".jpg";
		File thumbFile = new File(cachePath,thumbName);
		try {
			PlinyPlugin.copyInputStream(in, new FileOutputStream(thumbFile));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		sendThumbnailToBackingCache(thumbFile, resource.getCacheNumber());
		return ImageDescriptor.createFromFile(null, thumbFile.getAbsolutePath());

	}

	@Override
	public void setThumbFileNamePrefix(String p) {
		thumbFileNamePrefix = p;
	}
}
