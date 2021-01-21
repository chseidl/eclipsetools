package de.christophseidl.util.gef.editor;

import java.util.EventObject;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import de.christophseidl.util.gef.action.RefreshViewerAction;

public abstract class BaseGraphicalEditor extends GraphicalEditor {
	
	public BaseGraphicalEditor() {
		DefaultEditDomain editDomain = createEditDomain();
		setEditDomain(editDomain);
	}
	
	protected DefaultEditDomain createEditDomain() {
		return new DefaultEditDomain(this);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		Layout compositeLayout = createGraphicalViewerCompositeLayout();
		composite.setLayout(compositeLayout);
		
		addControlsBeforeGraphicalViewer(composite);
		
		// Create the regular graphical viewer composite
		super.createPartControl(composite);
		Control[] controls = composite.getChildren();
		
		if (controls == null) {
			//TODO: throw
		}
		
		Control actualPartControl = controls[controls.length - 1];
		Object layoutData = createGraphicalViewerCanvasLayoutData();
		actualPartControl.setLayoutData(layoutData);
		
		addControlsAfterGraphicalViewer(composite);
	}
	
	protected void addControlsBeforeGraphicalViewer(Composite composite) {
//		Label label = new Label(composite, SWT.NONE);
//		label.setText("Before");
//		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	}
	
	/**
	 * Be sure to override this in conjunction with <code>createGraphicalViewerCanvasLayoutData()</code>.
	 */
	protected Layout createGraphicalViewerCompositeLayout() {
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}
	
	/**
	 * Be sure to override this in conjunction with <code>createGraphicalViewerCompositeLayout()</code>.
	 */
	protected Object createGraphicalViewerCanvasLayoutData() {
		return new GridData(SWT.FILL, SWT.FILL, true, true);
	}
	
	protected void addControlsAfterGraphicalViewer(Composite composite) {
//		Scale slider = new Scale(composite, SWT.NONE);
//		slider.setMaximum(10);
//		slider.setMinimum(0);
//		slider.setPageIncrement(1);
//		slider.setSelection(7);
//		slider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}
	
	@Override
	protected void createGraphicalViewer(Composite parent) {
		//Here because there is no factory method for the graphical viewer!
		GraphicalViewer viewer = doCreateGraphicalViewer();

		// Rest is copied from GraphicalEditor
		viewer.createControl(parent);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		hookGraphicalViewer();
		initializeGraphicalViewer();
		
		// Until here
		registerListeners();
	}
	
	protected abstract GraphicalViewer doCreateGraphicalViewer();
	
	//Increase visibility
	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}
	
	protected void registerListeners() {
	}
	
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		GraphicalViewer viewer = getGraphicalViewer();
		
		viewer.setRootEditPart(createRootEditPart());
		viewer.setEditPartFactory(createEditPartFactory());
		
		ActionRegistry actionRegistry = getActionRegistry();
		registerActions(actionRegistry);
		
		registerContextMenu(viewer, actionRegistry);
		
		KeyHandler keyHandler = createKeyHandler(viewer);
		viewer.setKeyHandler(keyHandler);
		
		registerKeyboardShortcuts(keyHandler, actionRegistry);
	}

	protected void registerActions(ActionRegistry actionRegistry) {
	}

	final protected void registerContextMenu(GraphicalViewer viewer, ActionRegistry actionRegistry) {
		ContextMenuProvider contextMenuProvider = createContextMenuProvider(viewer, actionRegistry);
		viewer.setContextMenu(contextMenuProvider);
	}
	
	protected abstract ContextMenuProvider createContextMenuProvider(GraphicalViewer viewer, ActionRegistry actionRegistry);
	
	protected KeyHandler createKeyHandler(GraphicalViewer viewer) {
		return new GraphicalViewerKeyHandler(viewer);
	}
	
	protected void registerKeyboardShortcuts(KeyHandler keyHandler, ActionRegistry actionRegistry) {
		IAction refreshViewerAction = actionRegistry.getAction(RefreshViewerAction.ID);
		keyHandler.put(KeyStroke.getPressed(SWT.F5, SWT.NONE), refreshViewerAction);
	}
	
	
	protected abstract RootEditPart createRootEditPart();
	
	protected abstract EditPartFactory createEditPartFactory();
	
	
	public void executeCommand(Command command) {
		CommandStack commandStack = getCommandStack();
		commandStack.execute(command);
	}
	
	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(PROP_DIRTY);
		super.commandStackChanged(event);
	}
	
	public void refreshVisuals() {
		GraphicalViewer viewer = getGraphicalViewer();
		
		if (viewer == null) {
			return;
		}
		
		Control control = viewer.getControl();
		
		if (control == null) {
			return;
		}
		
		control.redraw();
	}
}
