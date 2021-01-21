package de.christophseidl.util.gef.tool;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.swt.widgets.Display;

public class CreationAndDirectEditTool extends CreationTool {
	@Override
	protected void performCreation(int button) {
		super.performCreation(button);

		EditPartViewer viewer = getCurrentViewer();
		final Object model = getCreateRequest().getNewObject();
		if (model == null || viewer == null) {
			return;
		}

		final Object o = getCurrentViewer().getEditPartRegistry().get(model);
		if (o instanceof EditPart) {
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					EditPart part = (EditPart) o;
					Request request = new DirectEditRequest();
					part.performRequest(request);
				}
			});
		}
	}
	
	public static CreationToolEntry createToolEntry(String name, String description, CreationFactory factory) {
		CreationToolEntry creationAndDirectEditEntry = new CreationToolEntry(name, description, factory, null, null);
		creationAndDirectEditEntry.setToolClass(CreationAndDirectEditTool.class); 
		return creationAndDirectEditEntry;
	}
}
