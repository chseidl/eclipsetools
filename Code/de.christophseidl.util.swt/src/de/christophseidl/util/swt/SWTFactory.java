package de.christophseidl.util.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SWTFactory {
	private static final int defaultButtonStyleFlags = SWT.PUSH | SWT.CENTER;
	
	public static Button createDefaultButton(Composite parent, String text) {
		return createDefaultButton(parent, text, 75);
	}
	
//	public static Button createDefaultButtonWithoutWidth(Composite parent, String text) {
//		return createDefaultButton(parent, text, -1);
//	}
	
	public static Button createDefaultButton(Composite parent, String text, int width) {
		return doCreateDefaultButton(parent, defaultButtonStyleFlags, text, false, width);
	}
	
	public static Button createDefaultButton(Composite parent, String text, boolean fillHorizontal) {
		return doCreateDefaultButton(parent, defaultButtonStyleFlags, text, fillHorizontal, -1);
	}
	
//	public static Button createDefaultButton(Composite parent, int styleFlags, String text, boolean fillHorizontal) {
//		return doCreateDefaultButton(parent, SWT.PUSH | styleFlags, text, fillHorizontal, -1);
//	}
	
	private static Button doCreateDefaultButton(Composite parent, int styleFlags, String text, boolean fillHorizontal, int width) {
		Button button = new Button(parent, defaultButtonStyleFlags);
		
		GridData buttonLayoutData = new GridData();
		buttonLayoutData.horizontalAlignment = fillHorizontal ? GridData.FILL : GridData.CENTER;
		
		if (width > 0) {
			buttonLayoutData.widthHint = width;
		}
		
		button.setLayoutData(buttonLayoutData);
		
		button.setText(text);
		
		return button;
	}
	
	public static Button createSpanningButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
		
		GridData buttonLayoutData = new GridData();
		buttonLayoutData.horizontalAlignment = GridData.FILL;
		button.setLayoutData(buttonLayoutData);
		
		button.setText(text);
		
		return button;
	}
	
	public static Composite createStructureComposite(Composite parent, int numColumns) {
		Composite composite = createComposite(parent, numColumns, false, true, false);
		
		removeMargin(composite);
		
		return composite;
	}
	
	public static Composite createStructureComposite(Composite parent, int numColumns, boolean makeEqualWidth, boolean grabHorizontal, boolean grabVertical) {
		Composite composite = createComposite(parent, numColumns, false, true, false);
		
		removeMargin(composite);
		
		return composite;
	}
	
	private static void removeMargin(Composite composite) {
		//TODO: Not working as expected. 
		GridLayout gridLayout = (GridLayout) composite.getLayout();
		
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;
		
//		GridData gridData = (GridData) composite.getLayoutData();
//		
//		gridData.horizontalIndent = 0;
//		gridData.verticalIndent = 0;
	}
	
	public static Composite createComposite(Composite parent, int numColumns) {
		return createComposite(parent, numColumns, false, true, false);
	}
	
	public static Composite createComposite(Composite parent, int numColumns, boolean makeEqualWidth, boolean grabHorizontal, boolean grabVertical) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		composite.setLayout(new GridLayout(numColumns, makeEqualWidth));
		
		GridData compositeLayoutData = new GridData();
		compositeLayoutData.horizontalAlignment = GridData.FILL;
		compositeLayoutData.verticalAlignment = GridData.FILL;
		compositeLayoutData.grabExcessHorizontalSpace = grabHorizontal;
		compositeLayoutData.grabExcessVerticalSpace = grabVertical;
		composite.setLayoutData(compositeLayoutData);
		
		return composite;
	}
	
	public static Composite createDummyComposite(Composite parent) {
		return createDummyComposite(parent, false, false);
	}
	
	public static Composite createHorizontalDummyComposite(Composite parent) {
		return createDummyComposite(parent, true, false);
	}
	
	public static Composite createVerticalDummyComposite(Composite parent) {
		return createDummyComposite(parent, false, true);
	}
	
	public static Composite createDummyComposite(Composite parent, boolean grabHorizontal, boolean grabVertical) {
		Composite dummyComposite = new Composite(parent, SWT.NONE);
		
		GridLayout dummyCompositeLayout = new GridLayout();
		dummyCompositeLayout.makeColumnsEqualWidth = true;
		dummyComposite.setLayout(dummyCompositeLayout);
		
		GridData dummyCompositeLayoutData = new GridData();
		dummyCompositeLayoutData.horizontalAlignment = GridData.FILL;
		dummyCompositeLayoutData.verticalAlignment = GridData.FILL;
		dummyCompositeLayoutData.grabExcessHorizontalSpace = grabHorizontal;
		dummyCompositeLayoutData.grabExcessVerticalSpace = grabVertical;
		dummyComposite.setLayoutData(dummyCompositeLayoutData);
		
		return dummyComposite;
	}
	
	public static Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.WRAP);
		label.setText(text);
		
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		
		return label;
	}
	
	public static Text createText(Composite parent) {
		return createText(parent, null);
	}
	
	public static Text createText(Composite parent, String initialText) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		
		if (initialText != null) {
			text.setText(initialText);
		}
		
		return text;
	}
	
	public static void setGridData(Control control, Integer horizontalAlignment, Integer verticalAlignment, Boolean grabExcessHorizontalSpace, Boolean grabExcessVerticalSpace) {
		Object layoutData = control.getLayoutData();
		GridData gridData = null;
		
		if (layoutData instanceof GridData) {
			gridData = (GridData) layoutData;
		} else {
			 gridData = new GridData();
			 control.setLayoutData(gridData);
		}

		if (horizontalAlignment != null) {
			gridData.horizontalAlignment = horizontalAlignment;
		}
		
		if (verticalAlignment != null) {
			gridData.verticalAlignment = verticalAlignment;
		}
		
		if (grabExcessHorizontalSpace != null) {
			gridData.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
		}
		
		if (grabExcessVerticalSpace != null) {
			gridData.grabExcessVerticalSpace = grabExcessVerticalSpace;
		}
	}
}
