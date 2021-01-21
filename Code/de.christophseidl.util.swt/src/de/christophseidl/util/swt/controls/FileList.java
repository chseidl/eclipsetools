package de.christophseidl.util.swt.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class FileList extends List {

	public FileList(Composite parent) {
		super(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	@Override
	protected void checkSubclass() {
		//Overriding checkSubclass allows this class to extend org.eclipse.swt.widgets.Composite
	}
}
