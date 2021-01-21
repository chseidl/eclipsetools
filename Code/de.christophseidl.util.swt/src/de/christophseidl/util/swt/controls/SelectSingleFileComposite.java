package de.christophseidl.util.swt.controls;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class SelectSingleFileComposite extends SelectSingleResourceComposite<IFile> {
	
	public SelectSingleFileComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	public IFile getFile() {
		return getResource();
	}
	
	protected abstract String[] createFileChooserDialogFilterExtensions();

	@Override
	protected SelectionDialog createResourceSelectionDialog(IFile initialResource) {
		Shell shell = getShell();
		String message = createResourceDialogTitle();
		IAdaptable rootElement = initialResource != null ? initialResource : ResourcesPlugin.getWorkspace().getRoot();
		
		//TODO: Filter extensions
//		String[] filterExtensions = createFileChooserDialogFilterExtensions();
		
		ResourceSelectionDialog dialog = new ResourceSelectionDialog(shell, rootElement, message);
		
		//TODO: Allow only single file
		
		return dialog;
	}
}
