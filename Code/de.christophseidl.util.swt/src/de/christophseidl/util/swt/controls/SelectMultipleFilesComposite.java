package de.christophseidl.util.swt.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import de.christophseidl.util.swt.SWTFactory;

public abstract class SelectMultipleFilesComposite extends SelectResourceComposite<IFile> {
	private Group mainGroup;
	private FileList fileList;
	
	private Button addButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button removeButton;

	//Data
	private List<IFile> files;
	
	public SelectMultipleFilesComposite(Composite parent, int style) {
		this(parent, style, "Files");
	}
	
	public SelectMultipleFilesComposite(Composite parent, int style, String groupName) {
		super(parent, style);
		files = new ArrayList<IFile>();
		
		assembleUI(groupName);
		registerListeners();
	}

	protected abstract String[] createFileChooserDialogFilterExtensions();
	
	@Override
	protected SelectionDialog createResourceSelectionDialog(IFile initialResource) {
		Shell shell = getShell();
		String message = createResourceDialogTitle();
		IAdaptable rootElement = initialResource != null ? initialResource : ResourcesPlugin.getWorkspace().getRoot();

		//TODO: Filter extensions
//		String[] filterExtensions = createFileChooserDialogFilterExtensions();
		
		//TODO: Allow multiple
		return new ResourceSelectionDialog(shell, rootElement, message);
	}
	
	private void assembleUI(String groupName) {
		setLayout(new GridLayout());

		assembleMainGroup(this, groupName);
		assembleFileList();
		assembleButtonBar();
		
		updateButtonEnabledState();
		layout();
	}

	private void assembleMainGroup(Composite parent, String groupName) {
		mainGroup = new Group(parent, SWT.NONE);
		mainGroup.setText(groupName);
		
		mainGroup.setLayout(new GridLayout(2, false));
		
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		mainGroup.setLayoutData(layoutData);
	}

	private void assembleFileList() {
		fileList = new FileList(mainGroup);
		
		GridData fileListLayoutData = new GridData();
		fileListLayoutData.verticalAlignment = GridData.FILL;
		fileListLayoutData.horizontalAlignment = GridData.FILL;
		fileListLayoutData.grabExcessVerticalSpace = true;
		fileListLayoutData.grabExcessHorizontalSpace = true;
		fileList.setLayoutData(fileListLayoutData);

		List<IFile> initiallySelectedFiles = createInitiallySelectedFiles();
		
		for (IFile file : initiallySelectedFiles) {
			IPath filePath = file.getFullPath();
			addPath(filePath);
		}
	}
	
	private void addPath(IPath path) {
		fileList.add(path.toOSString());
	}
	
	private void assembleButtonBar() {
		Composite buttonBar = new Composite(mainGroup, SWT.NONE);
		
		buttonBar.setLayout(new GridLayout());
		
		GridData buttonBarLayoutData = new GridData();
		buttonBarLayoutData.verticalAlignment = GridData.FILL;
		buttonBarLayoutData.horizontalAlignment = GridData.FILL;
		buttonBarLayoutData.grabExcessVerticalSpace = true;
		buttonBarLayoutData.grabExcessHorizontalSpace = false;
		buttonBar.setLayoutData(buttonBarLayoutData);
		
		doAssembleButtonBar(buttonBar);
		
		buttonBar.layout();
	}
	
	protected void assembleAddButton(Composite buttonBar) {
		addButton = SWTFactory.createDefaultButton(buttonBar, "Add...");
	}
	
	protected void assembleMoveUpButton(Composite buttonBar) {
		moveUpButton = SWTFactory.createDefaultButton(buttonBar, "Move Up");
	}
	
	protected void assembleMoveDownButton(Composite buttonBar) {
		moveDownButton = SWTFactory.createDefaultButton(buttonBar, "Move Down");
	}
	
	protected void assembleRemoveButton(Composite buttonBar) {
		removeButton = SWTFactory.createDefaultButton(buttonBar, "Remove");
	}
	
	protected void doAssembleButtonBar(Composite buttonBar) {	
		assembleAddButton(buttonBar);
		SWTFactory.createVerticalDummyComposite(buttonBar);
		assembleMoveUpButton(buttonBar);
		
		assembleMoveDownButton(buttonBar);
		SWTFactory.createVerticalDummyComposite(buttonBar);
		assembleRemoveButton(buttonBar);
	}
	
	private void registerListeners() {
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddFiles();
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onRemoveSelectedFiles();	
			}
		});
		
		fileList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnabledState();
			}
		});
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateDataFromUI();
			}
		});
	}
	
	private void onAddFiles() {
		List<IPath> paths = openResourceDialog();
		
		for (IPath path : paths) {
			addPath(path);
		}
	}
	
	private void onRemoveSelectedFiles() {
		int[] selectedIndices = fileList.getSelectionIndices();
		fileList.remove(selectedIndices);
		
		updateButtonEnabledState();
	}
	
	public List<IFile> getFiles() {
		updateDataFromUI();
		
		return files;
	}
	
	private void updateDataFromUI() {
		if (!isDisposed()) {
			files.clear();
		
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			
			String[] items = fileList.getItems();
			
			for (String item : items) {
				IPath path = new Path(item);
				IFile file = workspaceRoot.getFile(path);
				files.add(file);
			}
		}
	}
	
	protected abstract List<IFile> createInitiallySelectedFiles();
	
	private void updateButtonEnabledState() {
		boolean isFileSelected = fileList.getSelectionCount() > 0;
		doUpdateButtonEnabledState(isFileSelected);
	}
	
	protected void doUpdateButtonEnabledState(boolean isFileSelected) {
		moveUpButton.setEnabled(isFileSelected);
		moveDownButton.setEnabled(isFileSelected);
		
		removeButton.setEnabled(isFileSelected);
	}
}
