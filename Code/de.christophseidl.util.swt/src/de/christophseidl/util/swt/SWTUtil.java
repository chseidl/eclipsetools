package de.christophseidl.util.swt;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil {
	public static void setEnabledRecursively(Control control, boolean enabled) {
		control.setEnabled(enabled);
		
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			
			Control[] children = composite.getChildren();
			
			for (Control child : children) {
				setEnabledRecursively(child, enabled);
			}
		}
	}
	
	/**
	 * Children will be disposed after calling this.
	 */
	public static void removeAllChildren(Composite composite) {
		Control[] children = composite.getChildren();
		
		for (Control child : children) {
			child.dispose();
		}
	}
	
	public static void centerOverCurrentDisplay(Shell shell) {
		Display display = Display.getCurrent();
		centerOverDisplay(shell, display);
	}
	
	public static void centerOverParentDisplay(Shell shell) {
		Display display = shell.getDisplay();
		centerOverDisplay(shell, display);
	}
	
	public static void centerOverDisplay(Shell shell, Display display) {
		Rectangle displayBounds = display.getBounds();
		centerOverBounds(shell, displayBounds);
	}
	
	public static void centerOverShell(Shell shell, Shell parentShell) {
		Rectangle parentBounds = parentShell.getBounds();
		centerOverBounds(shell, parentBounds);
	}
	
	public static void centerOverParent(Shell shell) {
		Composite parent = shell.getParent();
		Rectangle parentBounds = parent.getBounds();
		centerOverBounds(shell, parentBounds);
	}
	
	private static void centerOverBounds(Shell shell, Rectangle centerBounds) {
		Rectangle shellBounds = shell.getBounds();
		
		int x = centerBounds.x + (centerBounds.width - shellBounds.width) / 2;
	    int y = centerBounds.y + (centerBounds.height - shellBounds.height) / 2;
	    
	    shell.setLocation(x, y);
	}
}
