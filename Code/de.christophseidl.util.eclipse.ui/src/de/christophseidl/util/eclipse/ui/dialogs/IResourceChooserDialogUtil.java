package de.christophseidl.util.eclipse.ui.dialogs;

import org.eclipse.swt.SWT;

public class IResourceChooserDialogUtil {
	public static int createStyle(boolean saveOrOpen, boolean allowMultiple) {
		int style = SWT.NULL;
		
		if (saveOrOpen == IResourceChooserDialog.SAVE) {
			style |= SWT.SAVE;
		} else {
			style |= SWT.OPEN;
		}
		
		if (allowMultiple) {
			style |= SWT.MULTI;
		}
		
		return style;
	}
	
	public static String createFileTitle(String providedTitle, boolean saveOrOpen) {
		return createTitle(providedTitle, saveOrOpen, "File");
	}
	
	public static String createFolderTitle(String providedTitle, boolean saveOrOpen) {
		return createTitle(providedTitle, saveOrOpen, "Folder");
	}
	
	private static String createTitle(String providedTitle, boolean saveOrOpen, String resourceType) {
		if (providedTitle != null && !providedTitle.isEmpty()) {
			return providedTitle;
		}
		
		return (saveOrOpen == IResourceChooserDialog.SAVE ? "Save" : "Open") + " " + resourceType;
	}
}
