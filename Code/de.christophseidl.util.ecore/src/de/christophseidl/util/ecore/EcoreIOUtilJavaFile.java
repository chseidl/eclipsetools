package de.christophseidl.util.ecore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.christophseidl.util.eclipse.ResourceUtil;

public class EcoreIOUtilJavaFile {
	public static <T extends EObject> T loadModel(File file) {
		String filename = file.getName();
		int index = filename.lastIndexOf(".");
		String extension = "";
		
		if (index != -1) {
			extension = filename.substring(index + 1);
		}
		
		return loadModel(file, extension, new XMIResourceFactoryImpl(), false);
	}
	
	public static <T extends EObject> T loadModel(File file, ResourceSet resourceSet) {
		return loadModel(file, resourceSet, null, false);
	}
	
	public static <T extends EObject> T loadModel(File file, String extension, Object resourceFactory, boolean overrideRegisteredFactories) {
		List<ExtensionToFactoryMap> extensionsToFactoryMap = new ArrayList<ExtensionToFactoryMap>();
		
		extensionsToFactoryMap.add(new ExtensionToFactoryMap(extension, resourceFactory));
		
		return loadModel(file, new ResourceSetImpl(), extensionsToFactoryMap, overrideRegisteredFactories);
	}
	
	public static <T extends EObject> T loadModel(File file, ResourceSet resourceSet, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		List<File> files = new ArrayList<File>();
		files.add(file);
		
		List<T> models = loadModels(files, resourceSet, extensionsToFactoryMap, overrideRegisteredFactories);
		
		if (models.isEmpty()) {
			return null;
		}
		
		return models.get(0);
	}
	
	public static <T extends EObject> List<T> loadModels(List<File> files) {
		return loadModels(files, new ResourceSetImpl(), null, false);
	}
	
	public static <T extends EObject> List<T> loadModels(List<File> files, ResourceSet resourceSet, List<ExtensionToFactoryMap> extensionsToFactoryMap, boolean overrideRegisteredFactories) {
		//NOTE NOTE NOTE
		//Need to load from ABSOLUTE PATH - otherwise resolving of proxies in other resources will fail!
		List<T> models = new ArrayList<T>();
		
	//		org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createPlatformResourceURI(path, true);
			
	//		ePackage.eINSTANCE.eClass();
			
		if (extensionsToFactoryMap != null) {
			Map<String, Object> factoryMap = Registry.INSTANCE.getExtensionToFactoryMap();
			
			for (ExtensionToFactoryMap extensionToFactoryMap : extensionsToFactoryMap) {
				String extension = extensionToFactoryMap.getExtension();
				Object resourceFactory = extensionToFactoryMap.getResourceFactory();
				
				boolean factoryRegistered = factoryMap.containsKey(extension);
				
				//Only add XMIResourceFactory if no other factory is registered or overriding is explicitly requested.
				if (!factoryRegistered || (factoryRegistered && overrideRegisteredFactories)) {
					factoryMap.put(extension, resourceFactory);
				}
			}
		}
	  		
	    for (File file : files) {
	    	file = ResourceUtil.createAbsoluteFile(file);
		    Resource resource = resourceSet.getResource(URI.createFileURI(file.toString()), true);
		    
		    try {
		    	resource.load(Collections.EMPTY_MAP);
		    	
		    	List<EObject> contents = resource.getContents();
		    	
		    	if (contents == null || contents.isEmpty()) {
		    		return null;
		    	}
		    	
	//	    	EcoreUtil.resolveAll(resourceSet);
		    	
		    	EObject model = contents.get(0);
		    	T typedModel = EcoreIOUtil.getTypedModel(model);
		    	
		    	if (typedModel != null) {
		    		models.add(typedModel);
		    	}
		    } catch(IOException e) {
		    	e.printStackTrace();
		    	System.err.println("Failed to load model from \"" + file + "\".");
		    }
		}
		
	    return models;
	}
	
	
	
	public static boolean saveModel(EObject model) {
	    Resource resource = model.eResource();
	    return doSaveModel(model, resource);
	}
	
	public static boolean saveModelAs(EObject model, File file) {
		return saveModelAs(model, file, new ResourceSetImpl());
	}
	
	public static boolean saveModelAs(EObject model, File file, ResourceSet resourceSet) {
//		ePackage.eINSTANCE.eClass();
		String extension = "";
		
		String filename = file.getName();
		int index = filename.lastIndexOf(".");
		
		if (index != -1 && index < filename.length() - 1) {
			extension = filename.substring(index + 1);
		}
		
	    Map<String, Object> extensionToFactoryMap = Registry.INSTANCE.getExtensionToFactoryMap();
	    
	    //Add a default resource factory for this extension if none was registered.
	    if (!extensionToFactoryMap.containsKey(extension)) {
	    	extensionToFactoryMap.put(extension, new XMIResourceFactoryImpl());
	    }
	    
	    Resource resource = resourceSet.createResource(URI.createFileURI(file.toString()));
	    resource.getContents().add(model);

		return doSaveModel(model, resource);
	}
	
	private static boolean doSaveModel(EObject model, Resource resource) {
		try {
			//TODO: faulty for save as!?
			resource.save(Collections.EMPTY_MAP);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.err.println("Failed to save model.");
		return false;
	}
	
	public static URI createURIFromFile(File file) {
		if (file == null) {
			return null;
		}
		
		String absolutePathString = file.getAbsolutePath();
		
		return URI.createFileURI(absolutePathString);
	}
	
	public static boolean saveResourceAs(Resource resource, File file) {
		URI uri = createURIFromFile(file);
		ResourceSet resourceSet = resource.getResourceSet();
		Resource newResource = resourceSet.createResource(uri);
		
		List<EObject> newContents = newResource.getContents();
		//To avoid concurrent modification
		List<EObject> copiedOldContents = new ArrayList<EObject>(resource.getContents());
		
		for (EObject model : copiedOldContents) {
			newContents.add(model);
		}
		
		try {
			newResource.save(Collections.EMPTY_MAP);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.err.println("Failed to save resource.");
		return false;
	}
	
	
	public static File getResourceFileForDifferentFolder(Resource resource, File moveTargetFolder) {
		if (resource == null) {
			return null;
		}
		
		URI resourceURI = resource.getURI();
		
		if (resourceURI.isFile() || resourceURI.isPlatformResource() || resourceURI.isPlatformPlugin()) {
			String filename = resourceURI.lastSegment();

			return new File(moveTargetFolder, filename);
		}
			
		return null;
	}
}
