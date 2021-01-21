package de.christophseidl.util.gef.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;

import de.christophseidl.util.eclipse.ResourceUtil;

public abstract class ResourceTracker implements IResourceChangeListener, IResourceDeltaVisitor {
	private GraphicalEditor editor;
	
	public ResourceTracker(GraphicalEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		
		try {
			if (delta != null) {
				delta.accept(this);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) {
		if (delta == null) {
			return true;
		}
		
		IEditorInput editorInput = editor.getEditorInput();
		IFile editorInputFile = org.eclipse.ui.ide.ResourceUtil.getFile(editorInput);
		IResource deltaResource = delta.getResource();
		
		if (!deltaResource.equals(editorInputFile)) {
			return true;
		}

		int deltaKind = delta.getKind();
		
		IWorkbenchPartSite site = editor.getSite();
		Shell shell = site.getShell();
		Display display = shell.getDisplay();
		
		if (deltaKind == IResourceDelta.REMOVED) {
			int deltaFlags = delta.getFlags();
			
			if ((deltaFlags & IResourceDelta.MOVED_TO) == 0) {
				// File was deleted
				display.asyncExec(new Runnable() {
					public void run() {
						editorInputFileDeleted();
					}
				});
			} else {
				// File was moved or renamed
				IPath movedToPath = delta.getMovedToPath();
				final IFile newEditorInputFile = ResourceUtil.getLocalFile(movedToPath);
				
				display.asyncExec(new Runnable() {
					public void run() {
						editorInputFileMovedOrRenamed(newEditorInputFile);
					}
				});
			}
			
			return false;
		}
		
		if (deltaKind == IResourceDelta.CHANGED) {
			// The file has changed and could have been overwritten somehow.
			display.asyncExec(new Runnable() {
				public void run() {
					editorInputFileChanged();
				}
			});
			
			return false;
		}
		
		return true;
	}
	
	protected abstract void editorInputFileChanged();
	protected abstract void editorInputFileMovedOrRenamed(IFile newEditorInputFile);
	protected abstract void editorInputFileDeleted();

	protected GraphicalEditor getEditor() {
		return editor;
	}

	protected void setEditor(GraphicalEditor editor) {
		this.editor = editor;
	}
}
