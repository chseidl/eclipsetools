package de.christophseidl.util.gef.action;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.christophseidl.util.eclipse.ui.JFaceUtil;
import de.christophseidl.util.gef.editor.BaseGraphicalEditor;

public abstract class BaseAction extends Action {
	private BaseGraphicalEditor editor;
	
	public BaseAction(BaseGraphicalEditor editor) {
		this.editor = editor;
		
		initialize();
		registerListeners();
	}
	
	private void initialize() {
		setId(createID());
		setText(createText());
		
		String iconPath = createIconPath();
		
		if (iconPath != null) {
			ImageDescriptor imageDescriptor = JFaceUtil.getImageDescriptorFromClassBundle(iconPath, getClass());
			setImageDescriptor(imageDescriptor);
		}
	}
	
	private void registerListeners() {
		GraphicalViewer viewer = editor.getGraphicalViewer();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnabledState();
			}
		});
	}
	
	protected abstract String createText();
	protected abstract String createID();
	
	protected String createIconPath() {
		return null;
	}
	
	public void updateEnabledState() {
	}
	
	@Override
	public abstract void run();
	
	protected BaseGraphicalEditor getEditor() {
		return editor;
	}
}
