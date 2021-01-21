package de.christophseidl.util.eclipse.resourcehandlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import de.christophseidl.util.eclipse.ResourceUtil;

public abstract class AbstractResourceHandler {
	protected static final String endl = "\n";
	
	private IFile file;
	
	public AbstractResourceHandler() {
		file = null;
		
		initialize();
	}
	
	public AbstractResourceHandler(IFile file) {
		this();
		
		loadFromFile(file);
	}
	
	public AbstractResourceHandler(IProject project) {
		this();
		
		IFile file = project.getFile(getFileDefaultLocationRelativeToProject());
		loadFromFile(file);
	}
	
	public AbstractResourceHandler(IJavaProject javaProject) {
		this(javaProject.getProject());
	}
	
	protected abstract String getFileDefaultLocationRelativeToProject();
	
	private void loadFromFile(IFile file) {
		this.file = file;
		
		if (file != null && file.exists()) {
			try {
				doLoadFromFile(file);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected abstract void initialize();
	
	protected abstract void doLoadFromFile(IFile file) throws Exception;
	
	public abstract String serialize();
	
	public boolean save() {
		return doSave(file);
	}
	
	public boolean saveAs(IFile newFile) {
		if (doSave(newFile)) {
			file = newFile;
			return true;
		}
		
		return false;
	}
	
	protected boolean doSave(IFile file) {
		if (file == null) {
			System.err.println("File is null");
			return false;
		}
		
		try {
			String content = serialize();
			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
	
			if (file.exists()) {
				file.setContents(inputStream, IFile.FORCE, null);
			} else {
				ResourceUtil.ensureFolderStructure(file);
				file.create(inputStream, true, null);
			}
			
			return true;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}
}
