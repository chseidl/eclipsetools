package de.christophseidl.util.gef.editor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import de.christophseidl.util.eclipse.ResourceUtil;
import de.christophseidl.util.eclipse.ui.JFaceUtil;
import de.christophseidl.util.gef.util.ResourceTracker;

//TODO: How to actually use this in the inheritance hierarchy when having to save/load models of unknown data model type?
//Basically need something that say the data model can be handled as if it were a feature model, e.g., the API? view model could be extracted via getFeatureModel();
//TODO: Serializer should take as input also an instance of the API
//Maybe this should rather be a data source (just wording) but could then be confused with the file.

public abstract class IFileBasedGraphicalEditor<DM, VM> extends ViewModelBasedGraphicalEditor<DM, VM> {
	private IResourceChangeListener resourceTracker;

	public IFileBasedGraphicalEditor() {
		resourceTracker = new ResourceTracker(this) {
			@Override
			protected void editorInputFileChanged() {
				handleInputFileChanged();
			}

			@Override
			protected void editorInputFileMovedOrRenamed(IFile newEditorInputFile) {
				handleEditorInputFileMovedOrRenamed(newEditorInputFile);
			}

			@Override
			protected void editorInputFileDeleted() {
				handleEditorInputFileDeleted();
			}
		};
	}
	
	protected void registerResourceTracker() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(resourceTracker);
	}
	
	protected void unregisterResourceTracker() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(resourceTracker);
	}

	private void setFileNameAsPartName() {
		IFile inputFile = getInputFile();
		
		if (inputFile != null) {
			try {
				// Display filename in tab's header
				String fileName = inputFile.getName();
				String displayName = URLDecoder.decode(fileName, "UTF-8");
				setPartName(displayName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = getInputFile();
		performSave(file);
	}
	
	protected IFile getInputFile() {
		IEditorInput editorInput = getEditorInput();
		
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			return fileEditorInput.getFile();
		}
		
		return null;
	}

	//Also used for save as
	protected void performSave(IFile file) {
		if (file == null) {
			return;
		}

		try {
			DM dataModel = getDataModel();
			doSaveDataModel(dataModel, file);
			ResourceUtil.refreshResource(file);
			
			CommandStack commandStack = getCommandStack();
			commandStack.markSaveLocation();
		} catch (IOException e) {
			e.printStackTrace();
//			inputFile = null;
		}
	}
	
	protected abstract void doSaveDataModel(DM model, IFile file) throws IOException;
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		registerResourceTracker();		
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		setFileNameAsPartName();
	}
	
	@Override
	protected DM loadDataModel() {
		IFile inputFile = getInputFile();
		
		if (inputFile == null) {
			return null;
		}
		
		return doLoadDataModel(inputFile);
	}
	
	protected abstract DM doLoadDataModel(IFile file);
	

	
	@Override
	public void dispose() {
		super.dispose();
		unregisterResourceTracker();
	}
	
	protected void handleInputFileChanged() {
		boolean reloadInput = true;
		
		if (isDirty()) {
			IWorkbenchPartSite site = getSite();
			Shell shell = site.getShell();
			
			String title = "Question";
			String message = "The editor's resource has been deleted but there are unsaved changes. Do you want to save the changes before closing the editor?";

			MessageDialog yesNoCancelDialog = JFaceUtil.createYesNoCancelDialog(shell, title, message);
			int result = yesNoCancelDialog.open();
			
			switch (result) {
				case 0:
					//Yes
					reloadInput = true;
					break;
				case 1:
					//No
					reloadInput = false;
					break;
				case 2:
					//Cancel
					reloadInput = false;
					break;
			}
		}
		
		if (reloadInput) {
			//Reload editor input
			IEditorInput editorInput = getEditorInput();
			setInput(editorInput);
			
			CommandStack commandStack = getCommandStack();
			commandStack.flush();
		}
	}
	
	protected void handleEditorInputFileMovedOrRenamed(IFile newEditorInputFile) {
		FileEditorInput newInput = new FileEditorInput(newEditorInputFile);
		setInput(newInput);
	}
	
	protected void handleEditorInputFileDeleted() {
		IWorkbenchPartSite site = getSite();
		Shell shell = site.getShell();
		
		boolean closeEditor = true;
		boolean saveOnClose = false;
		
		if (isDirty()) {
			String title = "Question";
			String message = "The editor's resource has been deleted but there are unsaved changes. Do you want to save the changes before closing the editor?";

			MessageDialog yesNoCancelDialog = JFaceUtil.createYesNoCancelDialog(shell, title, message);
			int result = yesNoCancelDialog.open();
			
			switch (result) {
				case 0:
					//Yes
					closeEditor = true;
					saveOnClose = true;
					break;
				case 1:
					//No
					closeEditor = true;
					saveOnClose = false;
					break;
				case 2:
					//Cancel
					closeEditor = false;
					saveOnClose = false;
					break;
			}
		}
		
		if (closeEditor) {
			IWorkbenchPage page = site.getPage();
			page.closeEditor(this, saveOnClose);
		}
	}
	
//	@Override
//	public boolean isSaveAsAllowed() {
//		return true;
//	}
//	
//	@Override
//	public void doSaveAs() {
//		IContainer container = EcoreResolverUtil.resolveContainerFromResource(resource);
//		
//		//TODO: Remove hardcoding
//		IFile saveAsFile = container.getFile(new Path("Dummy.defeaturemodel"));
//		
//		Resource saveAsResource = EcoreIOUtil.copyResourceTo(resource, saveAsFile);
//		
//		resource = saveAsResource;
//		//TODO: Set model
//		//TODO: update 
//		
//		performSave();
//	}
}
