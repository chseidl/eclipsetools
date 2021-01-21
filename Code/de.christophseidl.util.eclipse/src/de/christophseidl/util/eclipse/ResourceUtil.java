package de.christophseidl.util.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;

public class ResourceUtil {
	public static File loadFileFromBundle(String bundleName, String relativePath) {
		Bundle bundle = Platform.getBundle(bundleName);
		return loadFileFromBundle(bundle, relativePath);
	}
	
	public static File loadFileFromBundle(Bundle bundle, String relativePath) {
		if (bundle == null) {
			return null;
		}
		
		URL fileURL = bundle.getEntry(relativePath);
		
		try {
			URL locatedURL = FileLocator.resolve(fileURL);
			//TODO: Have to handle spaces here etc.
		    return new File(locatedURL.toURI());
		} catch (URISyntaxException e1) {
		    e1.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		
		return null;
	}
	
	
	public static File resourceToFile(IResource resource) {
		IPath path = resource.getLocation();
		
		return new File(path.toString());
	}
	
	@Deprecated
	public static IResource fileToResource(File file) {
		return fileToFile(file);
	}
	
	public static IFile fileToFile(File file) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IPath workspaceLocation = workspaceRoot.getLocation();
		IPath filePath = new Path(file.toString());
		
		boolean isWorkspaceResource = workspaceLocation.isPrefixOf(filePath);
		boolean isInExternalProject = false;

		if (isWorkspaceResource) {
			IPath relativeFilePath = filePath.makeRelativeTo(workspaceLocation);
			return workspaceRoot.getFile(relativeFilePath);
		}
		
		IProject projectOfFile = null;
		
		for (IProject project : workspaceRoot.getProjects()) {
			IPath projectLocation = project.getLocation();
			
			if (projectLocation.isPrefixOf(filePath)) {
				projectOfFile = project;
				isInExternalProject = true;
				break;
			}
		}
		
		if (!isInExternalProject) {
			return null;
		}
		
		IPath relativeFilePath = filePath.makeRelativeTo(projectOfFile.getLocation());
		return projectOfFile.getFile(relativeFilePath);		
	}
	
	public static void refreshResource(IResource resource, int depth) {
		try {
			resource.refreshLocal(depth, null);
		} catch(CoreException e) {
			
		}
	}
	
	public static void refreshProject(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		refreshProject(project);
	}
	
	public static void refreshProject(IProject project) {
		refreshResource(project, IResource.DEPTH_INFINITE);
	}
	
	public static void refreshResource(IResource resource) {
		//For some reason, DEPTH_ZERO still leaves this unrefreshed.
//		refreshResource(resource, IResource.DEPTH_ZERO);
		refreshResource(resource, IResource.DEPTH_ONE);
	}
	
	public static void refreshResourceRecursively(IResource resource) {
		refreshResource(resource, IResource.DEPTH_INFINITE);
	}
	
	public static IPath createAbsolutePath(IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath workspaceDirectory = workspace.getRoot().getLocation();
		
		return workspaceDirectory.append(path);
	}
	
	public static IPath createRelativePath(IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		//"Location" is the absolute path to the workspace.
		IPath workspaceLocation = workspaceRoot.getLocation();
		
		boolean isWorkspaceResource = workspaceLocation.isPrefixOf(path);

		if (!isWorkspaceResource) {
			return null;
		}
		
		return path.makeRelativeTo(workspaceLocation);
	}
	
	public static IFile createRelativeFile(IFile file) {
		IPath filePath = file.getFullPath();
		IPath relativeFilePath = createRelativePath(filePath);
		
		if (relativeFilePath == null) {
			return null;
		}
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		return workspaceRoot.getFile(relativeFilePath);
	}
	
	public static IFile createAbsoluteFile(IFile file) {
		try {
			IPath absoluteFilePath = file.getLocation();
			URL resourceURL = new URL("file://" + absoluteFilePath.toString());
			
			return createAbsoluteFile(resourceURL);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static IFile createAbsoluteFile(URL resourceURL) {
		try {
			resourceURL = FileLocator.toFileURL(resourceURL);
			resourceURL = FileLocator.resolve(resourceURL);
			IPath filePath = new Path(resourceURL.toString());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			
			return workspace.getRoot().getFile(filePath);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static File createAbsoluteFile(File file) {
		return file.getAbsoluteFile();
	}
	
	public static void writeToFile(String content, IFile file) {
		try {
			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
			
			if (file.exists()) {
				file.delete(true, null);
			}
			
			ensureFolderStructure(file);
			file.create(inputStream, true, null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void ensureFolderStructure(IResource resource) {
		//Recursively create a (possible not yet existing) file structure
		IContainer container = resource.getParent();
		
		if (container instanceof IResource)  {
			ensureFolderStructure((IResource) container);
		}
		
		if (resource instanceof IFolder) {
			IFolder folder = (IFolder) resource;
			
			if (!folder.exists()) {
				try {
					folder.create(true, false, null);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
		    }
		}
	}
	
	
	public static IFile makeFileRelativeToWorkspace(IFile file) {
		IPath filePath = file.getFullPath();
		
		return makePathRelativeToWorkspace(filePath);
	}
	
	public static IFile makeFileRelativeToWorkspace(String rawFilePath) {
		IPath filePath = new Path(rawFilePath);
		return makePathRelativeToWorkspace(filePath);
	}
	
	public static IFile makePathRelativeToWorkspace(IPath filePath) {
		IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
//		IPath workspacePath = workspaceRoot.getFullPath();
		IPath workspacePath = workspaceRoot.getLocation();
		
		IPath relativeFilePath = filePath.makeRelativeTo(workspacePath);
		
		return workspaceRoot.getFile(relativeFilePath);
	}
	
	//February 2017
	@Deprecated
	public static IPath makeFilePathRelativeToFile(IFile originalFile, IFile referenceFile) {
		return makeResourcePathRelativeToFile(originalFile, referenceFile);
	}
	
	public static IPath makeResourcePathRelativeToFile(IResource originalResource, IFile referenceFile) {
		IContainer referenceContainer = referenceFile.getParent();
		return makeResourcePathRelativeToContainer(originalResource, referenceContainer);
	}
	
	//February 2017
	@Deprecated
	public static IPath makeFilePathRelativeToContainer(IFile originalFile, IContainer referenceContainer) {
		return makeResourcePathRelativeToContainer(originalFile, referenceContainer);
	}
	
	public static IPath makeResourcePathRelativeToContainer(IResource originalResource, IContainer referenceContainer) {
		IPath referenceContainerPath = referenceContainer.getLocation();
		IPath originalFilePath = originalResource.getLocation();

		return originalFilePath.makeRelativeTo(referenceContainerPath);
	}
	
	
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		IWorkspace workspace = getWorkspace();
		return workspace.getRoot();
	}
	
	public static String getBaseFilename(IFile file) {
		String filename = file.getName();
		String extension = file.getFileExtension();
		
		int lastIndex = filename.length() - (extension.length() + 1);
		return filename.substring(0, lastIndex);
	}
	
	public static IFile deriveFile(IFile file, String newExtension) {
		String baseFilename = getBaseFilename(file);
		String newFilename = baseFilename + "." + newExtension;

		return getFileInSameContainer(file, newFilename);
	}
	
	public static IFile deriveFileInOtherContainer(IFile file, IContainer newContainer) {
		String filename = file.getName();
		IPath path = new Path(filename);
		return newContainer.getFile(path);
	}
	
	public static IFile getFileInSameContainer(IFile file, String newFilename) {
		IContainer parent = file.getParent();
		IPath newFilePath = new Path(newFilename);
		
		return parent.getFile(newFilePath);
	}
	
	public static IFile getLocalFile(String workspaceRelativePathString) {
		IPath workspaceRelativePath = new Path(workspaceRelativePathString);
		return getLocalFile(workspaceRelativePath);
	}

	public static IFile getLocalFile(IPath workspaceRelativePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		
		return workspaceRoot.getFile(workspaceRelativePath);
	}
	
	public static IContainer getLocalContainer(String containerName) {
		return ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(new Path(containerName));
	}
	
	public static IFile findFirstFileWithExtension(IContainer container, String searchedFileExtension) {
		try {
			IResource[] members = container.members();
			
			if (members != null) {
				for (IResource member : members) {
					if (member instanceof IFile) {
						IFile file = (IFile) member;
						String fileExtension = file.getFileExtension();
						
						if (fileExtension != null) {
							if (fileExtension.equalsIgnoreCase(searchedFileExtension)) {
								return file;
							}
						}
					}
				}
			}
		} catch(CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void deleteResourcesInFolder(IFolder folder) throws CoreException {
		IResource[] resources = folder.members();
		
		for (IResource resource : resources) {
			resource.delete(true, null);
		}
	}
}
