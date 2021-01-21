package de.christophseidl.util.eclipse.ui;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class JFaceUtil {
	public static MessageDialog createYesNoCancelDialog(Shell parent, String title, String message) {
		return new MessageDialog(parent, title, null, message, MessageDialog.QUESTION_WITH_CANCEL, new String[]{IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
	}
	
	public static void alertInformation(String message) {
		alert(message, false);
	}
	
	public static void alertError(String message) {
		alert(message, true);
	}
		
	public static void alert(String message, boolean isError) {
		String title = isError ? "Error" : "Information";
		alert(title, message, isError);
	}
	
	public static void alert(String title, String message, boolean isError) {
		Display display = Display.getCurrent();
		Shell shell = display.getActiveShell();
		
		if (isError) {
			MessageDialog.openError(shell, title, message);
		} else {
			MessageDialog.openInformation(shell, title, message);
		}
	}
	
	public static ImageDescriptor getImageDescriptorFromClassBundle(String relativePath, Class<?> classFromBundle) {
		Bundle bundle = FrameworkUtil.getBundle(classFromBundle);
		return getImageDescriptorFromBundle(bundle, relativePath);
	}
	
	public static ImageDescriptor getImageDescriptorFromBundle(String bundleName, String relativePath) {
		Bundle bundle = Platform.getBundle(bundleName);
		return getImageDescriptorFromBundle(bundle, relativePath);
	}
	
	public static ImageDescriptor getImageDescriptorFromBundle(Bundle bundle, String relativePath) {
		URL fileURL = bundle.getEntry(relativePath);
		return ImageDescriptor.createFromURL(fileURL);
	}
}
