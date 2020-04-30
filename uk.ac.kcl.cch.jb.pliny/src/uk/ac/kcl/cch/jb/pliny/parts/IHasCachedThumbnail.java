package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;

public interface IHasCachedThumbnail extends ICachingResource {
	public ImageDescriptor getMyThumbnailDescriptor();

}
