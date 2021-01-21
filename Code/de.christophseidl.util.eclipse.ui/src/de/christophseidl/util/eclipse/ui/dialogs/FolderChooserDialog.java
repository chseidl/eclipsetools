package de.christophseidl.util.eclipse.ui.dialogs;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class FolderChooserDialog extends DirectoryDialog implements IResourceChooserDialog<IFolder> {

	public FolderChooserDialog(Shell shell) {
		this(shell, null);
	}
	
	public FolderChooserDialog(Shell shell, String title) {
		this(shell, title, OPEN);
	}
	
	public FolderChooserDialog(Shell shell, String title, boolean saveOrOpen) {
		this(shell, title, saveOrOpen, false);
	}
	
	public FolderChooserDialog(Shell shell, String title, boolean saveOrOpen, boolean allowMultiple) {
		super(shell, IResourceChooserDialogUtil.createStyle(saveOrOpen, allowMultiple));
		
		setText(IResourceChooserDialogUtil.createFileTitle(title, saveOrOpen));
	}
	
	@Override
	protected void checkSubclass() {
	}
	
	@Override
	public void setPath(IPath path) {
		if (path == null) {
			return;
		}
		
		//TODO: ?!?!
		String filename = path.toOSString();
		setFilterPath(filename);
	}
}
