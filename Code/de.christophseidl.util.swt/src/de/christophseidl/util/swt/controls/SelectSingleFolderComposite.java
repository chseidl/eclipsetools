package de.christophseidl.util.swt.controls;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class SelectSingleFolderComposite extends SelectSingleResourceComposite<IFolder> {
	public SelectSingleFolderComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	public IFolder getFolder() {
		return getResource();
	}
	
	public void initializeFromActiveResource(IResource resource) {
		if (resource instanceof IFolder) {
			IFolder folder = (IFolder) resource;
			setInitialResource(folder);
			return;
		}
		
		IContainer parent = resource.getParent();
		IPath path = parent.getFullPath();
		setResourcePath(path);
		
		if (parent instanceof IFolder) {
			IFolder parentFolder = (IFolder) parent;
			setInitialResource(parentFolder);
			return;
		}
		
		setInitialResource(null);
	}
	
	protected abstract boolean createResourceDialogAllowNewFolderName();
	
	@Override
	protected SelectionDialog createResourceSelectionDialog(IFolder initialResource) {
		Shell shell = getShell();
		IContainer rootElement = initialResource != null ? initialResource : ResourcesPlugin.getWorkspace().getRoot();
		boolean allowNewFilename = createResourceDialogAllowNewFolderName();
		String message = createResourceDialogTitle();
		
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(shell, rootElement, allowNewFilename, message);
		
		dialog.showClosedProjects(false);
		
		return dialog;
	}
}
