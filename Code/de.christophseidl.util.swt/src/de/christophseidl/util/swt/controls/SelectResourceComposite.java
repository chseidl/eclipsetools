package de.christophseidl.util.swt.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class SelectResourceComposite<T extends IResource> extends Composite {
	private T initialResource;
	
	public SelectResourceComposite(Composite parent, int style) {
		super(parent, style);
	}

	
	public void setInitialResource(T initialResource) {
		this.initialResource = initialResource;
	}
	
	/**
	* Overriding checkSubclass allows this class to extend org.eclipse.swt.widgets.Composite
	*/	
	protected void checkSubclass() {
	}
	
	protected abstract String createResourceDialogTitle();
	protected abstract SelectionDialog createResourceSelectionDialog(T initialResource);
	
	protected List<IPath> openResourceDialog() {
		SelectionDialog resourceSelectionDialog = createResourceSelectionDialog(initialResource);
		
		int result = resourceSelectionDialog.open();
		
		if (result == SelectionDialog.OK) {
			return convertToPaths(resourceSelectionDialog.getResult());
		}
		
		return new ArrayList<IPath>();
	}
	
	private List<IPath> convertToPaths(Object[] result) {
		List<IPath> paths = new ArrayList<IPath>();
		
		if (result != null) {
			for (Object o : result) {
				IPath path = null;
				
				if (path == null && o instanceof IPath) {
					path = (IPath) o;
				}
				
				if (path == null && o instanceof IResource) {
					IResource resoure = (IResource) o;
					path = resoure.getFullPath();
				}
				
				if (path == null && o instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) o;
					path = (IPath) adaptable.getAdapter(IPath.class);
				}
				
				if (path != null) {
					paths.add(path);
				}
			}
		}
		
		return paths;
	}
}
