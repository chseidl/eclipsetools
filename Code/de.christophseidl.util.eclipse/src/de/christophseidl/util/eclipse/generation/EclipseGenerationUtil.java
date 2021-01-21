package de.christophseidl.util.eclipse.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import de.christophseidl.util.eclipse.ResourceUtil;
import de.christophseidl.util.eclipse.resourcehandlers.ManifestHandler;

//This class is utterly incomplete, but the stuff that is there seems to work.
public class EclipseGenerationUtil {

	public static IProject createProject(String projectName, IProgressMonitor progressMonitor) {
		try {
			if (projectName == null || projectName.isEmpty()) {
				return null;
			}
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject project = root.getProject(projectName);
			
			if (!project.exists()) {
				project.create(progressMonitor);
			}
			
			if (!project.isOpen()) {
				project.open(progressMonitor);
			}
			
			return project;
		} catch(CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IJavaProject createJavaProject(String projectName, IProgressMonitor progressMonitor) {
		IProject project = createProject(projectName, progressMonitor);
		IJavaProject javaProject = JavaCore.create(project);
		
		ensureJavaProjectSetup(javaProject);
		
		return javaProject;
	}

	public static IJavaProject createPluginProject(String pluginId, IProgressMonitor progressMonitor) {
		IJavaProject javaProject = createJavaProject(pluginId, progressMonitor);
		
		ensurePluginProjectSetup(pluginId, javaProject);
		
		return javaProject;
	}

	public static IJavaProject createEcorePluginProject(String pluginId, IProgressMonitor progressMonitor) {
		IJavaProject javaProject = createPluginProject(pluginId, progressMonitor);

		ensureEcorePluginProjectSetup(javaProject);
		
		return javaProject;
	}

	public static void ensureJavaProjectSetup(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
			
		//Add the Java project nature if not already done so.
		ensureProjectNature(JavaCore.NATURE_ID, project);
		
		//By default, the project root seems to be on the class path. Remove this.
		ensureProjectRootNotOnClassPath(javaProject);
		
		//Add java libraries to class path
		ensureJavaLibrariesOnClasspath(javaProject);
		
		//Add "src" folder
		ensureSourceFolder("src", javaProject);
		
		//Create bin folder
		ensureOutputFolder("bin", javaProject);
	}

	private static void ensureProjectRootNotOnClassPath(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		IPath projectPath = project.getFullPath();
		
		if (isOnClasspath(projectPath, javaProject)) {
			removeFromClasspath(projectPath, javaProject);
		}
	}
	
	private static boolean isOnClasspath(IPath path, IJavaProject javaProject) {
		try {
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			
			for (IClasspathEntry oldEntry : oldEntries) {
				IPath oldPath = oldEntry.getPath();
				
				if (oldPath.equals(path)) {
					return true;
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return false;
	}
	
	private static void addToClasspath(IClasspathEntry entry, IJavaProject javaProject) {
		try {
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = entry;
			javaProject.setRawClasspath(newEntries, null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void removeFromClasspath(IPath path, IJavaProject javaProject) {
		try {
			IClasspathEntry[] oldClasspathEntries = javaProject.getRawClasspath();
			List<IClasspathEntry> newClassPathEntriesList = new ArrayList<IClasspathEntry>(Arrays.asList(oldClasspathEntries));
			
			Iterator<IClasspathEntry> iterator = newClassPathEntriesList.iterator();
			
			while (iterator.hasNext()) {
				IClasspathEntry classpathEntry = iterator.next();
				IPath classpathEntryPath = classpathEntry.getPath();
				
				if (classpathEntryPath.equals(path)) {
					iterator.remove();
				}
			}
	
			IClasspathEntry[] newClasspathEntries = newClassPathEntriesList.toArray(new IClasspathEntry[0]);
			javaProject.setRawClasspath(newClasspathEntries, null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void ensureJavaLibrariesOnClasspath(IJavaProject javaProject) {
		try {
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] libraryLocations = JavaRuntime.getLibraryLocations(vmInstall);
			
			for (LibraryLocation libraryLocation : libraryLocations) {
				IPath libraryPath = libraryLocation.getSystemLibraryPath();
				
				if (!isOnClasspath(libraryPath, javaProject)) {
					IClasspathEntry entry = JavaCore.newLibraryEntry(libraryPath, null, null);
					addToClasspath(entry, javaProject);
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void ensurePluginProjectSetup(String pluginId, IJavaProject javaProject) {
		//TODO: Add plugin builders, plugin project nature?
		
		ensureManifest(pluginId, javaProject);
//		ensureBuildProperties(javaProject);
	}

	public static void ensureEcorePluginProjectSetup(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		
		ensureFolder("model", project);
		ensureSourceFolder("src-gen", javaProject);		
	}

	public static void ensureProjectNature(String projectNatureId, IProject project) {
		try {
			if (!project.hasNature(projectNatureId)) {
				IProjectDescription description = project.getDescription();
				String[] oldNatureIds = description.getNatureIds();
				String[] newNatureIds = new String[oldNatureIds.length + 1];
				System.arraycopy(oldNatureIds, 0, newNatureIds, 0, oldNatureIds.length);
				newNatureIds[oldNatureIds.length] = projectNatureId;
				description.setNatureIds(newNatureIds);
				project.setDescription(description, null);
			}
		} catch(CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IFolder ensureFolder(String folderName, IProject project) {
		try {
			IFolder folder = project.getFolder(folderName);
			
			if (!folder.exists()) {
				folder.create(false, true, null);
			}
			
			return folder;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IFolder ensureOutputFolder(String outputFolderName, IJavaProject javaProject) {
		try {
			IProject project = javaProject.getProject();
			
			IFolder outputFolder = ensureFolder(outputFolderName, project);
			IPath outputFolderPath = outputFolder.getFullPath();
			javaProject.setOutputLocation(outputFolderPath, null);
			
			return outputFolder;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IFolder ensureSourceFolder(String sourceFolderName, IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		IFolder sourceFolder = ensureFolder(sourceFolderName, project);
		
		try {
//			IPackageFragmentRoot srcFolderRoot = javaProject.getPackageFragmentRoot(sourceFolder.getName());
			IPath sourceFolderPath = sourceFolder.getFullPath();
			
			if (!isOnClasspath(sourceFolderPath, javaProject)) {
				//Source folder is not yet on the class path. Add it.
				IClasspathEntry classPathEntry = JavaCore.newSourceEntry(sourceFolderPath);
				addToClasspath(classPathEntry, javaProject);
				
				//TODO: If this is a plugin project, add the source folder to the build.properties.
				
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return sourceFolder;
	}

	public static IFolder ensureMetaInfFolder(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		return ensureFolder("META-INF", project);
	}

	public static IFile ensureManifest(String pluginId, IJavaProject javaProject) {
		IFolder folder = EclipseGenerationUtil.ensureMetaInfFolder(javaProject);
		
		IFile manifestFile = folder.getFile("MANIFEST.MF");

//		IPath relativeManifestFilePath = relativeManifestFile.getFullPath();
//		IProject project = javaProject.getProject();
//		IFile manifestFile = project.getFile(relativeManifestFilePath);
		
		if (!manifestFile.exists()) {
			ManifestHandler manifest = new ManifestHandler();
			manifest.setPluginId(pluginId);
			manifest.saveAs(manifestFile);
		}
		
		return manifestFile;
	}

	public static IFile ensurePluginXml(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		IFile pluginXmlFile = project.getFile("plugin.xml");
		
		if (!pluginXmlFile.exists()) {
			//TODO: Set eclipse version programmatically and create extra method for manual choice
			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
							 "<?eclipse version=\"3.4\"?>\n"+
							 "<plugin>\n\n" +
							 "</plugin>";
			ResourceUtil.writeToFile(content, pluginXmlFile);
		}
		
		return pluginXmlFile;
	}
	
//	public static void ensureBuildProperties(IJavaProject javaProject) {
//		// TODO Auto-generated method stub
//		
//		//Check the current source folders and add them to the build.properties
//		
//	}

//	public static void addPluginDependency(String pluginDependencyName, IJavaProject pluginProject) {
////		ensurePluginProjectSetup(pluginProject);
//	}

	public static IFile createClassFileFromQualifiedClassName(String qualifiedClassName, IFolder sourceFolder) {
		//Replace dots of qualified class name with slashes to create relative file path.
		String relativeClassName = qualifiedClassName.replaceAll("\\.", "/") + ".java";
		IPath relativeClassNamePath = new Path(relativeClassName);
		
		return sourceFolder.getFile(relativeClassNamePath);
	}
}
