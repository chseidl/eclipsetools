package de.christophseidl.util.swt.controls;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public abstract class SelectSingleResourceComposite<T extends IResource> extends SelectResourceComposite<T> {
	private Group mainGroup;
	private Text resourceStringText;
	private Button browseButton;

	private T resource;
	
	public SelectSingleResourceComposite(Composite parent, int style) {
		super(parent, style);
		
		initGUI();
		registerListeners();
	}

	private void initGUI() {
		try {
			GridLayout thisLayout = new GridLayout();
			this.setLayout(thisLayout);
			this.setSize(484, 100);
			{
				mainGroup = new Group(this, SWT.NONE);
				GridLayout mainGroupLayout = new GridLayout();
				mainGroupLayout.numColumns = 2;
				mainGroup.setLayout(mainGroupLayout);
				GridData mainGroupLData = new GridData();
				mainGroupLData.horizontalAlignment = GridData.FILL;
				mainGroupLData.verticalAlignment = GridData.FILL;
				mainGroupLData.grabExcessHorizontalSpace = true;
				mainGroupLData.grabExcessVerticalSpace = true;
				mainGroup.setLayoutData(mainGroupLData);
				mainGroup.setBounds(28, 46, 70, 82);
				{
					GridData filenameTextLData = new GridData();
					filenameTextLData.grabExcessHorizontalSpace = true;
					filenameTextLData.horizontalAlignment = GridData.FILL;
					filenameTextLData.verticalAlignment = GridData.FILL;
					resourceStringText = new Text(mainGroup, SWT.BORDER);
					resourceStringText.setLayoutData(filenameTextLData);
					resourceStringText.setEditable(false);
				}
				{
					browseButton = new Button(mainGroup, SWT.PUSH | SWT.CENTER);
					GridData browseButtonLData = new GridData();
					browseButtonLData.widthHint = 75;
					browseButton.setLayoutData(browseButtonLData);
					browseButton.setText("Browse...");
				}
			}
			this.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerListeners() {
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<IPath> paths = openResourceDialog();
				
				if (!paths.isEmpty()) {
					IPath path = paths.get(0);
					setResourcePath(path);
				}
			}
		});
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateDataFromUI();
			}
		});
	}
	
	public void setGroupName(String groupName) {
		mainGroup.setText(groupName);
	}
	
	private void updateDataFromUI() {
		if (!isDisposed()) {
			String filename = resourceStringText.getText();
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			resource = loadResourceFromString(filename, workspaceRoot);
		}
	}
	
	protected abstract T loadResourceFromString(String resourceString, IWorkspaceRoot workspaceRoot);
	
	protected void setResourcePath(IPath resourcePath) {
		resourceStringText.setText(resourcePath.toOSString());
	}
	
	protected T getResource() {
		updateDataFromUI();
		return resource;
	}
}
