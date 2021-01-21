package de.christophseidl.util.gef.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;

import de.christophseidl.util.gef.editor.BaseGraphicalEditor;

public class RefreshViewerAction extends BaseAction {

	public static final String ID = RefreshViewerAction.class.getCanonicalName();
	
	public RefreshViewerAction(BaseGraphicalEditor editor) {
		super(editor);
	}
	
	@Override
	protected String createText() {
		return "Refresh";
	}

	@Override
	protected String createID() {
		return ID;
	}
	
	@Override
	protected String createIconPath() {
		return "icons/ActionRefresh.png";
	}
	
	@Override
	public void run() {
		BaseGraphicalEditor editor = getEditor();
		GraphicalViewer viewer = editor.getGraphicalViewer();
		EditPart rootEditPart = viewer.getRootEditPart();

		rootEditPart.refresh();
	}
}
