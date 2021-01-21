package de.christophseidl.util.ecore;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.christophseidl.util.eclipse.ResourceUtil;
import de.christophseidl.util.java.CollectionsUtil;

//Using eclipse style uris when saving automatically refreshes resources etc.
//http://www.eclipsezone.com/eclipse/forums/t113801.html

public class EcoreIOUtil {
	//General
	public static ResourceSet getDefaultResourceSet(EObject model) {
		Resource resource = model.eResource();
		return getDefaultResourceSet(resource);
	}
	
	public static ResourceSet getDefaultResourceSet(Resource resource) {
		if (resource != null) {
			return resource.getResourceSet();
		}
	
		return getDefaultResourceSet();
	}
	
	public static ResourceSet getDefaultResourceSet() {
		return new ResourceSetImpl();
	}
	
	public static IFile getFile(EObject element) {
		if (element == null) {
			return null;
		}
		
		Resource resource = element.eResource();
		return getFile(resource);
	}
	
	public static IFile getFile(Resource resource) {
		if (resource == null) {
			return null;
		}
		
		URI resourceURI = resource.getURI();
		return getFile(resourceURI);
	}
	
	public static IFile getFile(URI resourceURI) {
		if (resourceURI == null) {
			return null;
		}
		
		if (resourceURI.isFile()) {
			//TODO: Untested
			String fileString = resourceURI.toFileString();
			File file = new File(fileString);
			return ResourceUtil.fileToFile(file);
		}
		
		if (resourceURI.isPlatformResource()) {
			String platformString = resourceURI.toPlatformString(true);
			return ResourceUtil.makeFileRelativeToWorkspace(platformString);
		}
		
		return null;
	}
	
	
	//Loading
	public static <T extends EObject> T loadModel(Resource resource) {
		if (resource == null) {
			return null;
		}
		
		List<T> models = doLoadModels(Collections.singletonList(resource));
		
		if (!models.isEmpty()) {
			return models.get(0);
		}
		
		return null;
	}
	
	public static <T extends EObject> T loadModel(URI uri) {
		return loadModel(uri, new ResourceSetImpl());
	}
	
	public static <T extends EObject> T loadModel(URI uri, ResourceSet resourceSet) {
		if (uri == null) {
			return null;
		}
		
		List<T> models = doLoadModels(Collections.singletonList(uri), resourceSet, null, false);
		
		if (!models.isEmpty()) {
			return models.get(0);
		}
		
		return null;
	}
	
	public static <T extends EObject> T loadModel(IFile file) {
		String extension = file.getFileExtension();
		return loadModel(file, extension, new XMIResourceFactoryImpl(), false);
	}
	
	public static <T extends EObject> T loadModel(IFile file, ResourceSet resourceSet) {
		return loadModel(file, resourceSet, null, false);
	}
	
	public static <T extends EObject> T loadModel(IFile file, String extension) {
		return loadModel(file, extension, null, false);
	}
	
	public static <T extends EObject> T loadModel(IFile file, String extension, Object resourceFactory) {
		return loadModel(file, extension, resourceFactory, false);
	}
	
	public static <T extends EObject> T loadModel(IFile file, String extension, Object resourceFactory, boolean overrideRegisteredFactory) {
		return loadModel(file, new ResourceSetImpl(), extension, resourceFactory, overrideRegisteredFactory);
	}
	
	public static <T extends EObject> T loadModel(IFile file, ResourceSet resourceSet, String extension, Object resourceFactory, boolean overrideRegisteredFactory) {
		List<ExtensionToFactoryMap> extensionsToFactoryMap = new ArrayList<ExtensionToFactoryMap>();
		
		if (resourceFactory != null) {
			extensionsToFactoryMap.add(new ExtensionToFactoryMap(extension, resourceFactory));
		}
		
		return loadModel(file, resourceSet, extensionsToFactoryMap, overrideRegisteredFactory);
	}
	
	//Careful: Default for override has changed from false to true.
//	public static <T extends EObject> T loadModel(IFile file, List<ExtensionToFactoryMap> extensionsToFactoryMap) {
//		return loadModel(file, extensionsToFactoryMap, true);
//	}
	
	public static <T extends EObject> T loadModel(IFile file, ResourceSet resourceSet, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		List<IFile> files = new ArrayList<IFile>();
		files.add(file);
		
		List<T> models = loadModels(files, resourceSet, extensionsToFactoryMap, overrideRegisteredFactories);
		
		if (models.isEmpty()) {
			return null;
		}
		
		return models.get(0);
	}
	
	public static <T extends EObject> List<T> loadModels(List<IFile> files) {
		return loadModels(files, null, false);
	}
	
	public static <T extends EObject> List<T> loadModels(List<IFile> files, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		return loadModels(files, new ResourceSetImpl(), extensionsToFactoryMap, overrideRegisteredFactories);
	}
	
	public static <T extends EObject> List<T> loadModels(List<IFile> files, ResourceSet resourceSet, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		//NOTE NOTE NOTE
		//Need to load from ABSOLUTE PATH - otherwise resolving of proxies in other resources will fail!
		List<URI> uris = new ArrayList<URI>();

		//Create resources for all files and load them.
	    for (IFile file : files) {
	    	//TODO Ensure that the file has an absolute path! Otherwise, proxies WILL fail!!!
	    	URI uri = createURIFromFile(file);
	    	uris.add(uri);
		}
		
	    return doLoadModels(uris, resourceSet, extensionsToFactoryMap, overrideRegisteredFactories);
	}
	
	private static <T extends EObject> List<T> doLoadModels(List<URI> uris, ResourceSet resourceSet, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		//Implement extensions to factory map if requested.
		if (extensionsToFactoryMap != null) {
			Map<String, Object> factoryMap = Registry.INSTANCE.getExtensionToFactoryMap();
			
			for (ExtensionToFactoryMap extensionToFactoryMap : extensionsToFactoryMap) {
				String extension = extensionToFactoryMap.getExtension();
				Object resourceFactory = extensionToFactoryMap.getResourceFactory();
				
				boolean factoryRegistered = factoryMap.containsKey(extension);
				
				//Only add provided factory if no other factory is registered or overriding is explicitly requested.
				if (!factoryRegistered || (factoryRegistered && overrideRegisteredFactories)) {
					factoryMap.put(extension, resourceFactory);
				}
			}
		}
		
		List<Resource> resources = new ArrayList<Resource>();
		
	    for (URI uri : uris) {
		    Resource resource = resourceSet.getResource(uri, true);
//		    Resource resource = resourceSet.getResource(uri, false);
//		    
//		    try {
//				resource.load(null);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		    resources.add(resource);
		}
		
	    return doLoadModels(resources);
	}
	
	private static <T extends EObject> List<T> doLoadModels(List<Resource> resources) {
		List<T> models = new ArrayList<T>();

		//Create resources for all files and load them.
	    for (Resource resource : resources) {
		    try {
		    	resource.load(Collections.EMPTY_MAP);
		    	
		    	List<EObject> contents = resource.getContents();
		    	
		    	if (contents == null || contents.isEmpty()) {
		    		//Throw just to be caught again immediately and keep on going with the next model.
		    		throw new UnsupportedOperationException();
		    	}
		    	
		    	for (EObject model : contents) {
//		    	EObject model = contents.get(0);
		    		T typedModel = getTypedModel(model);
		    		
		    		if (typedModel != null) {
		    			models.add(typedModel);
		    		}
		    	}
		    } catch(Exception e) {
		    	e.printStackTrace();
		    	System.err.println("Failed to load model(s) from \"" + resource.getURI() + "\".");
		    }
		}
		
	    return models;
	}
	
	public static <T extends EObject> T loadAccompanyingModel(EObject elementInOriginalResource, String extension, String... additionalExtensions) {
		Resource originalResource = elementInOriginalResource.eResource();
		return loadAccompanyingModel(originalResource, extension, additionalExtensions);
	}
	
	
	public static <T extends EObject> T loadAccompanyingModel(Resource originalResource, String extension, String... additionalExtensions) {
		//Create a collection of the varargs arguments.
		//This is not nice but makes it easier to call the method while avoiding improper use with no extension.
		List<String> extensions = new ArrayList<String>();
		extensions.add(extension);
		
		if (additionalExtensions != null) {
			CollectionsUtil.addAll(additionalExtensions, extensions);
		}
		
		for (String currentExtension : extensions) {
			Resource resource = EcoreResolverUtil.resolveAccompanyingResource(originalResource, currentExtension);
		
			try {
				T model = EcoreIOUtil.loadModel(resource);
				
				if (model != null) {
					return model;
				}
			} catch(Exception e) {
			}
		}
		
		return null;
	}
	
	public static <T extends EObject> T loadModelFromProjectRoot(String fileExtension, EObject elementFromProject) {
		Resource resource = elementFromProject.eResource();
		return loadModelFromProjectRoot(fileExtension, resource);
	}

	public static <T extends EObject> T loadModelFromProjectRoot(String fileExtension, Resource resource) {
		IProject project = EcoreResolverUtil.resolveProjectFromResource(resource);
		ResourceSet resourceSet = getDefaultResourceSet(resource);
		return loadModelFromProjectRoot(fileExtension, project, resourceSet);
	}
	
	public static <T extends EObject> T loadModelFromProjectRoot(String fileExtension, IProject project, ResourceSet resourceSet) {
		try {
			List<IFile> modelFiles = new ArrayList<IFile>();
			
			//List all files at project root ending in the file extension
			for (IResource resource : project.members()) {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExtension2 = file.getFileExtension();
					
					if (fileExtension2.equals(fileExtension)) {
						modelFiles.add(file);
					}
				}
			}
			
			//If there is more or less than one file, throw an error.
			if (modelFiles.isEmpty()) {
				return null;
			}
			
			if (modelFiles.size() > 1) {
				return null;
			}
			
			//Otherwise, load the file as model.
			IFile modelFile = modelFiles.get(0);
			return EcoreIOUtil.loadModel(modelFile, resourceSet);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//NOTE: Here on request by MN
	//TODO: Refactor this (shouldn't it be possible to get the EPackage from the extension?)
	public static <T extends EObject> T loadModelFromString(String modelInTextForm, EPackage ePackage, String fileExtension) throws IOException {
		// Create a ResourceSet
		ResourceSet resourceSet = getDefaultResourceSet();

		return getTypedModel(loadModelFromString(modelInTextForm, ePackage, fileExtension, resourceSet));
	}
	
	public static <T extends EObject> T loadModelFromString(String modelInTextForm, EPackage ePackage, String fileExtension, ResourceSet resourceSet) throws IOException {
		if (fileExtension != null && !fileExtension.startsWith(".")) {
			fileExtension = "." + fileExtension;
		}

		// Create a ResourceSet
//		ResourceSet resourceSet = getDefaultResourceSet();

		// register XMIRegistryResourceFactoryIml
		resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		// register your epackage to the resource set so it has a reference to
		// your ecore
		// you can get an instance to your epackage by calling
		// YourEPackageClass.getInstace();
		resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		
		Resource resource = resourceSet.createResource(URI.createURI("*" + fileExtension));

		resource.load(new URIConverter.ReadableInputStream(modelInTextForm), null);
		
		List<EObject> contents = resource.getContents();
		EObject model = contents.get(0);
		return getTypedModel(model);
	}
	
	
	
	//Saving as Model
	public static boolean saveModel(EObject model) {
	    Resource resource = model.eResource();
	    return doSaveModel(model, resource);
	}
	
	public static boolean saveModelAs(EObject model, IFile file) {
		return saveModelAs(model, file, getDefaultResourceSet(model));
	}
	
	public static boolean saveModelAs(EObject model, String fileExtension) {
		Resource resource = model.eResource();
		URI oldURI = resource.getURI();
		URI newURI = oldURI.trimFileExtension().appendFileExtension(fileExtension);
		
		return saveModelAs(model, newURI, getDefaultResourceSet(model));
	}
	
	public static boolean saveModelAs(EObject model, IFile file, ResourceSet resourceSet) {
		URI uri = createURIFromFile(file);
		return saveModelAs(model, uri, resourceSet);
	}
	
	public static boolean saveModelAs(EObject model, URI uri, ResourceSet resourceSet) {
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(model);
		
		return doSaveModel(model, resource);
	}
	
	private static boolean doSaveModel(EObject model, Resource resource) {
		try {
			resource.save(Collections.EMPTY_MAP);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.err.println("Failed to save model.");
		return false;
	}
	
	
	//Saving as Resource
	
	public static void saveResourceAs(Resource resource, IFile newFile) {
		Resource newResource = copyResourceTo(resource, newFile);
		
		try {
			newResource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Resource copyResourceTo(Resource resource, IFile newFile) {
		URI uri = createURIFromFile(newFile);
		ResourceSet resourceSet = resource.getResourceSet();
		Resource newResource = resourceSet.createResource(uri);
		
		List<EObject> newContents = newResource.getContents();
		//To avoid concurrent modification
		List<EObject> copiedOldContents = new ArrayList<EObject>(resource.getContents());
		
		for (EObject model : copiedOldContents) {
			newContents.add(model);
		}
		
		return newResource;
	}
	
	public static void saveResourcesAs(List<Resource> resources, IFolder newFolder) {
		ResourceUtil.ensureFolderStructure(newFolder);
		
		//Write all resources to disk
		for (Resource resource : resources) {
			IFile relocatedFile = EcoreIOUtil.getResourceFileForDifferentFolder(resource, newFolder);
			EcoreIOUtil.saveResourceAs(resource, relocatedFile);
		}

		//Refresh new folder (in case it is in the workspace)
		ResourceUtil.refreshResourceRecursively(newFolder);
	}
	
	
	//General and internal utilities
	
	public static URI createURIFromFile(IFile file) {
		//TODO: Not sure if this is the definite answer
		//TODO TODO TODO Ensure that the file has an absolute path! Otherwise, proxies WILL fail!!!
		//Update 2014_11_09: Not so sure about this anymore. File has to be within workspace though.
		
		if (file == null) {
			return null;
		}
		
		IPath fullPath = file.getFullPath();
		String absolutePathString = fullPath.toOSString();
		
		return URI.createPlatformResourceURI(absolutePathString, true);
	}
	
//	public static Resource createResourceFromFile(IFile file, ResourceSet resourceSet) {
//	}
	
	public static Resource createResource(IFile file, ResourceSet resourceSet, boolean allowOverride) {
		URI uri = createURIFromFile(file);
		
		boolean resourceExists = resourceSet.getResource(uri, false) != null;
		
		if (resourceExists && !allowOverride) {
			throw new InvalidParameterException("Resource for file \"" + file.getFullPath() + "\" already exists.");
		}
		
		return resourceSet.createResource(uri);
	}
	
	public static Resource getResource(IFile file, ResourceSet resourceSet, boolean loadOnDemand) {
		URI uri = createURIFromFile(file);
		
		return resourceSet.getResource(uri, loadOnDemand);
	}
	
	//Sometimes needed as fall back when type cannot be derived automatically (e.g., when used as parameter to method calls).
//	@Deprecated
	public static <T extends EObject> T getTypedModel(EObject model, Class<T> modelType) {
		return getTypedModel(model);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EObject> T getTypedModel(EObject model) {
		try {
			//Try to cast to requested type...
			return (T) model;
		} catch(ClassCastException e) {
			//... and return null if it fails.
			//Most likely the cleanest solution with generics.
			return null;
		}
	}
	
	public static IFile getModelFileForDifferentFolder(EObject model, IFolder moveTargetFolder) {
		Resource resource = model.eResource();
		return getResourceFileForDifferentFolder(resource, moveTargetFolder);
	}
	
	public static IFile getResourceFileForDifferentFolder(Resource resource, IFolder moveTargetFolder) {
		if (resource == null) {
			return null;
		}
		
		URI resourceURI = resource.getURI();
		
		if (resourceURI.isFile() || resourceURI.isPlatformResource() || resourceURI.isPlatformPlugin()) {
			String filename = resourceURI.lastSegment();

			return moveTargetFolder.getFile(filename);
		}
			
		return null;
	}
	
	
	//See if those are needed - already compatible.
	
//	public static <T extends EObject> T loadModelFromPlatformResource(String projectName, String relativePathToModel, Class<T> modelType) {
//		return loadModelFromPlatformResource(projectName, relativePathToModel, modelType, new ResourceSetImpl());
//	}
//	
//	public static <T extends EObject> T loadModelFromPlatformResource(String projectName, String relativePathToModel, Class<T> modelType, ResourceSet resourceSet) {
//		URI uri = URI.createURI("platform:/resource/" + projectName + "/" + relativePathToModel);
//		Resource resource = resourceSet.getResource(uri, true);
//		return getTypedModel(resource.getContents().get(0), modelType);
//	}
	
	public static <T extends EObject> T loadModelFromPlugin(String pluginId, String relativePathToModel) {
		return loadModelFromPlugin(pluginId, relativePathToModel, new ResourceSetImpl());
	}
	
	public static <T extends EObject> T loadModelFromPlugin(String pluginId, String relativePathToModel, ResourceSet resourceSet) {
		URI uri = URI.createURI("platform:/plugin/" + pluginId + "/" + relativePathToModel);
		Resource resource = resourceSet.getResource(uri, true);
		return getTypedModel(resource.getContents().get(0));
	}
	
	@Deprecated
	public static <T extends EObject> T loadModelFromPlugin(String pluginId, String relativePathToModel, Class<T> modelType) {
		return loadModelFromPlugin(pluginId, relativePathToModel, modelType, new ResourceSetImpl());
	}
	
	@Deprecated
	public static <T extends EObject> T loadModelFromPlugin(String pluginId, String relativePathToModel, Class<T> modelType, ResourceSet resourceSet) {
		URI uri = URI.createURI("platform:/plugin/" + pluginId + "/" + relativePathToModel);
		Resource resource = resourceSet.getResource(uri, true);
		return getTypedModel(resource.getContents().get(0), modelType);
	}
}
