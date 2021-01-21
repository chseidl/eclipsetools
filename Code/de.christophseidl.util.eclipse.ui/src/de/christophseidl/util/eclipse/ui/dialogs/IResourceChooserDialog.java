package de.christophseidl.util.eclipse.ui.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public interface IResourceChooserDialog<T extends IResource> {
	public static final boolean SAVE = true;
	public static final boolean OPEN = false;
	
	public void setPath(IPath path);
	public String open();
}
