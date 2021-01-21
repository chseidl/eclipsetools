package de.christophseidl.util.gef.util;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;

public class LabelDirectEditManager extends DirectEditManager {
	private Label label;

	public LabelDirectEditManager(GraphicalEditPart source, Class<?> editorType, CellEditorLocator locator, Label label) {
	    super(source, editorType, locator);
	    
	    this.label = label;
	  }

	@Override
	protected void initCellEditor() {
		String initialLabelText = label.getText();
		CellEditor cellEditor = getCellEditor();
		
		//TODO: Doesn't seem to have an effect.
		CellEditor.LayoutData layoutData = cellEditor.getLayoutData();
		layoutData.verticalAlignment = SWT.CENTER;
		
		cellEditor.setValue(initialLabelText);
	}

}
