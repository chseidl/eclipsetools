package de.christophseidl.util.eclipse.ui.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileChooserDialog extends FileDialog implements IResourceChooserDialog<IFile> {

	public FileChooserDialog(Shell shell) {
		this(shell, null);
	}
	
	public FileChooserDialog(Shell shell, String title) {
		this(shell, title, OPEN);
	}
	
	public FileChooserDialog(Shell shell, String title, boolean saveOrOpen) {
		this(shell, title, saveOrOpen, null);
	}
	
	public FileChooserDialog(Shell shell, String title, boolean saveOrOpen, String[] providedFilterExtensions) {
		this(shell, title, saveOrOpen, providedFilterExtensions, false);
	}
	
	public FileChooserDialog(Shell shell, String title, boolean saveOrOpen, String[] providedFilterExtensions, boolean allowMultiple) {
		super(shell, IResourceChooserDialogUtil.createStyle(saveOrOpen, allowMultiple));
		
        setText(IResourceChooserDialogUtil.createFileTitle(title, saveOrOpen));
        setFilterExtensions(createFilterExtensions(providedFilterExtensions));
	}
	
	private static String[] createFilterExtensions(String[] providedFilterExtensions) {
		if (providedFilterExtensions == null) {
			return new String[]{"*.*"};
		}
		
		//TODO: Maybe always include *.*
		return providedFilterExtensions;
	}
	
	@Override
	protected void checkSubclass() {
	}

	@Override
	public void setPath(IPath path) {
		if (path == null) {
			return;
		}
		
		String filename = path.toOSString();
		setFileName(filename);
	}
}
