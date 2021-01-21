package de.christophseidl.util.eclipse.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.christophseidl.util.eclipse.ResourceUtil;

public class SelectionUtil {
	public static List<IResource> getSelectedResources(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		return extractResourcesFromSelection(selection);
	}
	
	public static List<IResource> getSelectedResources() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		
		ISelection selection = selectionService.getSelection();
		
		return extractResourcesFromSelection(selection);
	}
	
	private static List<IResource> extractResourcesFromSelection(ISelection selection) {
		List<IResource> selectedResources = new ArrayList<IResource>();
		
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

			List<?> selectedObjects = structuredSelection.toList();

			for (Object selectedObject : selectedObjects) {
				if (selectedObject instanceof IResource) {
					IResource resource = (IResource) selectedObject;
					selectedResources.add(resource);
				} else if (selectedObject instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) selectedObject;
					Object adapter = adaptable.getAdapter(IResource.class);

					if (adapter != null) {
						IResource resource = (IResource) adapter;
						selectedResources.add(resource);
					}
				}
			}
		}
		
		return selectedResources;
	}
	
	public static IFile getFirstIFileFromSelectionWithExtension(String requestedFileExtension, ISelection selection) {
		List<IResource> resources = extractResourcesFromSelection(selection);
		
		for (IResource resource : resources) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				
				String fileExtension = file.getFileExtension();
				if (fileExtension.equalsIgnoreCase(requestedFileExtension)) {
					return file;
				}
			}
		}
		
		return null;
	}
	
	public static IFile getFirstSelectedIFile() {
		List<IResource> resources = getSelectedResources();
		
		if (resources.isEmpty()) {
			return null;
		}
		
		IResource resource = resources.get(0);
		
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		
		return null;
	}
	
	public static File getFirstSelectedFile() {
		IFile selectedIFile = getFirstSelectedIFile();
		return ResourceUtil.resourceToFile(selectedIFile);
	}
	
	public static IFile getFirstSelectedIFileWithExtension(String searchedFileExtension) {
		List<IFile> selectedFilesWithExtension = getSelectedIFilesWithExtension(searchedFileExtension); 
		
		if (selectedFilesWithExtension.isEmpty()) {
			return null;
		}
		
		return selectedFilesWithExtension.get(0);
	}
	
	public static File getFirstSelectedFileWithExtension(String searchedFileExtension) {
		List<File> selectedFilesWithExtension = getSelectedFilesWithExtension(searchedFileExtension); 
		
		if (selectedFilesWithExtension.isEmpty()) {
			return null;
		}
		
		return selectedFilesWithExtension.get(0);
	}
	
	public static List<IFile> getSelectedIFilesWithExtension(String searchedFileExtension) {
		return getSelectedIFilesWithExtensions(new String[] {searchedFileExtension});
	}
	
	public static List<File> getSelectedFilesWithExtension(String searchedFileExtension) {
		return getSelectedFilesWithExtensions(new String[] {searchedFileExtension});
	}
	
	public static List<IFile> getSelectedIFilesWithExtensions(String[] searchedFileExtensions) {
		List<IFile> selectedFilesWithExtension = new ArrayList<IFile>();
		
		if (searchedFileExtensions == null) {
			return selectedFilesWithExtension;
		}
		
		List<IResource> resources = getSelectedResources();
		
		for (IResource resource : resources) {
			String fileExtension = resource.getFileExtension();
			
			if (containsExtension(searchedFileExtensions, fileExtension)) {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					selectedFilesWithExtension.add(file);
				}
			}
		}
		
		return selectedFilesWithExtension;
	}
	
	public static List<File> getSelectedFilesWithExtensions(String[] searchedFileExtensions) {
		List<IFile> selectedIFilesWithExtension = getSelectedIFilesWithExtensions(searchedFileExtensions);
		List<File> selectedFilesWithExtension = new ArrayList<File>();
		
		for (IFile selectedIFile : selectedIFilesWithExtension) {
			File selectedFile = ResourceUtil.resourceToFile(selectedIFile);
			selectedFilesWithExtension.add(selectedFile);
		}
		
		return selectedFilesWithExtension;
	}
	
	private static boolean containsExtension(String[] fileExtensions, String searchedFileExtension) {
		for (String fileExtension : fileExtensions) {
			if (fileExtension == null) {
				if (searchedFileExtension == null) {
					return true;
				}
			}
			else if (fileExtension.equalsIgnoreCase(searchedFileExtension)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static IFile getFirstActiveIFileWithExtension(String extension) {
		IFile selectedFile = SelectionUtil.getFirstSelectedIFileWithExtension(extension);
		
		if (selectedFile != null) {
			return selectedFile;
		}
		
		IFile editorFile = getActiveEditorIFile();
		
		if (editorFile != null && editorFile.getFileExtension().equals(extension)) {
			return editorFile;
		}
		
		return null;
	}
	
	public static IEditorPart getActiveEditorPart() {
		IWorkbench workBench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workBench.getActiveWorkbenchWindow();
		
		if (window == null) {
			return null;
		}
		
		IWorkbenchPage page = window.getActivePage();
		
		if (page == null) {
			return null;
		}
		
		return page.getActiveEditor();
	}
	
	public static IEditorInput getActiveEditorInput() {
		IEditorPart editor = getActiveEditorPart();
		
		if (editor == null) {
			return null;
		}
		
		return editor.getEditorInput();
	}
	
	public static IPath getActiveEditorInputPath() {
		IEditorInput editorInput = getActiveEditorInput();
		
		if (editorInput instanceof IPathEditorInput) {
			IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
			return pathEditorInput.getPath();
		}
		
		return null;
	}
	
	public static IFile getActiveEditorIFile() {
		IPath path = getActiveEditorInputPath();
		
		if (path == null) {
			return null;
		}
		
		return ResourceUtil.makePathRelativeToWorkspace(path);
	}
}
