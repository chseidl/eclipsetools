package de.christophseidl.util.gef.util;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class LabelCellEditorLocator implements CellEditorLocator {

	private Label label;

	public LabelCellEditorLocator(Label label) {
		this.label = label;
	}

	@Override
	public void relocate(CellEditor cellEditor) {
		Text text = (Text) cellEditor.getControl();
		
		Rectangle labelBounds = label.getTextBounds().getCopy();
		Point preferredSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		label.translateToAbsolute(labelBounds);
		
		int width = Math.max(labelBounds.width, preferredSize.x) + 1;
		int height = Math.max(labelBounds.height, preferredSize.y) + 1;
		
		text.setBounds(labelBounds.x - 1, labelBounds.y - 1, width, height);
	}

}
