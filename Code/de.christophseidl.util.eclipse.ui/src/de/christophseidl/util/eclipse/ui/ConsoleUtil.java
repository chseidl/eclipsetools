package de.christophseidl.util.eclipse.ui;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleUtil {
//	public static void showConsole(MessageConsole console) throws PartInitException {
//		IWorkbench workbench = PlatformUI.getWorkbench();
//		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
//		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//		
////		workbenchWindow.getShell().
//		
////		IWorkbenchWindow window = win.getService(IWorkbenchWindow.class);
//		
//		String id = IConsoleConstants.ID_CONSOLE_VIEW;
//		IConsoleView view = (IConsoleView) workbenchPage.showView(id);
//		view.display(console);
//	}
	
	public static MessageConsole findOrCreateConsole(String name) {
		MessageConsole console = findConsole(name);
		
		if (console != null) {
			return console;
		}
		
		return createConsole(name);
	}
	
	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = plugin.getConsoleManager();
		IConsole[] existingConsoles = consoleManager.getConsoles();
		
		for (int i = 0; i < existingConsoles.length; i++) {
			if (name.equals(existingConsoles[i].getName())) {
				return (MessageConsole) existingConsoles[i];
			}
		}
		
		return null;
	}
	
	public static MessageConsole createConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = plugin.getConsoleManager();
		MessageConsole newConsole = new MessageConsole(name, null);
		consoleManager.addConsoles(new IConsole[] { newConsole });
		return newConsole;
	}
}
