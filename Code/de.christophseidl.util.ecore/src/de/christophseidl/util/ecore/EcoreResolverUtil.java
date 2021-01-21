package de.christophseidl.util.ecore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import de.christophseidl.util.eclipse.ResourceUtil;
import de.christophseidl.util.java.CollectionsUtil;

public class EcoreResolverUtil {
	/**
	 * Assembles a set containing the original metamodel and all metamodel that its elements reference directly/indirectly through types of attributes, references, operations' return types, operations' parameters or super classes.
	 * 
	 * @param originalMetamodel
	 * @return A set of all directly/indirectly referenced metamodels.
	 */
	public static Set<EPackage> resolveAllRecursivelyReferencedMetamodels(EPackage originalMetamodel) {
		Set<EPackage> allRecursivelyReferencedMetamodels = new LinkedHashSet<EPackage>();
		doResolveAllRecursivelyReferencedMetamodels(originalMetamodel, allRecursivelyReferencedMetamodels);
		return allRecursivelyReferencedMetamodels;
	}

	private static void doResolveAllRecursivelyReferencedMetamodels(EPackage currentMetamodel, Set<EPackage> processedMetamodels) {
		//Mark this metamodel as processed so that it is not processed again (avoid infinite loops).
		processedMetamodels.add(currentMetamodel);
		
		Set<EPackage> newMetamodels = new HashSet<EPackage>();
		
		//Determine all referenced metamodels and, if they are new, process and add them to the set of referencable metamodels.
		Iterator<EObject> iterator = currentMetamodel.eAllContents();
		
		//NOTE: There's two options for referencing another metamodel:
		//a) The type of a typed element, i.e., an attribute, operation or its parameters
		//b) Super classes of meta classes
		while(iterator.hasNext()) {
			EObject eObject = iterator.next();
			
			if (eObject instanceof ETypedElement) {
				ETypedElement eTypedElement = (ETypedElement) eObject;
				EClassifier eType = eTypedElement.getEType();
				
				//Type may be null, e.g. for EOperations that do not have a return type
				if (eType != null) {
					EClass eTypeEClass = eType.eClass();
					
					addMetamodelOfMetamodelElementIfNotAlreadyPresent(eTypeEClass, processedMetamodels, newMetamodels);
				}
			}
			
			if (eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				
				doResolveAllReferencedModelsFromEClassSuperTypes(eClass, processedMetamodels, newMetamodels);
			}
		}
		
		//Recursively process the newly discovered metamodels.
		Iterator<EPackage> iterator2 = newMetamodels.iterator();
		
		if (iterator2.hasNext()) {
			EPackage newMetamodel = iterator2.next();
			//NOTE: Processing the metamodel also adds it to the set of processed, i.e., referencable metamodels.
			doResolveAllRecursivelyReferencedMetamodels(newMetamodel, processedMetamodels);
		}
	}
	
	private static void doResolveAllReferencedModelsFromEClassSuperTypes(EClass eClass, Set<EPackage> processedMetamodels, Set<EPackage> newMetamodels) {
		List<EClass> eClassSuperTypes = eClass.getEAllSuperTypes();
		
		for (EClass eClassSuperType : eClassSuperTypes) {
			addMetamodelOfMetamodelElementIfNotAlreadyPresent(eClassSuperType, processedMetamodels, newMetamodels);
		}
	}
	
	private static void addMetamodelOfMetamodelElementIfNotAlreadyPresent(EObject metamodelElement, Set<EPackage> processedMetamodels, Set<EPackage> newMetamodels) {
		EObject rootContainer = EcoreUtil.getRootContainer(metamodelElement);
		
		if (rootContainer instanceof EPackage) {
			EPackage metamodel = (EPackage) rootContainer;
			
			if (metamodel != null) {
				if (!processedMetamodels.contains(metamodel)) {
					newMetamodels.add(metamodel);
				}
			}
		}
	}
	
	
	
	public static Resource resolveAccompanyingResource(Resource originalResource, String extension) {
		URI originalURI = originalResource.getURI();
		URI accompanyingURI = originalURI.trimFileExtension().appendFileExtension(extension);
		
		ResourceSet resourceSet = originalResource.getResourceSet();
		
		Resource accompanyingResource = resourceSet.getResource(accompanyingURI, true);
		
//		if (accompanyingResource == null) {
//			accompanyingResource = resourceSet.createResource(accompanyingURI);
//		}
		
		return accompanyingResource;
	}
	
	public static IProject resolveProjectFromEObject(EObject eObject) {
		if (eObject == null) {
			return null;
		}
		
		Resource resource = eObject.eResource();
		
		return resolveProjectFromResource(resource);
	}
	
	public static IProject resolveProjectFromResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		
		URI uri = resource.getURI();
		
		return resolveProjectFromResourceURI(uri);
	}
	
	public static IProject resolveProjectFromEPackage(EPackage ePackage) {
		String namespace = ePackage.getNsURI();
		URI namespaceURI = URI.createURI(namespace);
		return resolveProjectFromNamespaceURI(namespaceURI);
	}
	
	public static IProject resolveProjectFromNamespaceURI(URI namespaceURI) {
		@SuppressWarnings("deprecation")
		final Map<String, URI> packageNamespaceToGenModelLocationMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap();
		String namespace = namespaceURI.toString();
		
		if (packageNamespaceToGenModelLocationMap.containsKey(namespace)) {
			//This is a file URI!
			URI genModelURI = packageNamespaceToGenModelLocationMap.get(namespace);
			
			if (genModelURI != null && (genModelURI.isFile() || genModelURI.isPlatform())) {
				return resolveProjectFromResourceURI(genModelURI);
			}
		}
		
		return null;
	}
	
	public static EPackage resolveEPackageFromRegistry(String namespace) {
		if (namespace == null) {
			return null;
		}
		
		return EPackage.Registry.INSTANCE.getEPackage(namespace);
	}
	
	//Works but not needed right now
//	public static EPackage resolveEPackageFromRegistry(URI namespaceURI) {
//		if (namespaceURI == null) {
//			return null;
//		}
//		
//		String namespace = namespaceURI.toString();
//		return resolveEPackageFromRegistry(namespace);
//	}

	public static GenModel resolveGenModelFromModelElement(EObject modelElement) {
		Resource resource = modelElement.eResource();
		ResourceSet resourceSet = resource.getResourceSet();
		URI namespaceURI = resource.getURI();
		return doResolveGenModelFromNamespaceURI(namespaceURI, resourceSet);
	}
	
	public static GenModel resolveGenModelFromNamespace(String namespace) {
		URI namespaceURI = URI.createURI(namespace);
		return EcoreResolverUtil.resolveGenModelFromNamespaceURI(namespaceURI);
	}
	
	public static GenModel resolveGenModelFromNamespaceURI(URI namespaceURI) {
		return doResolveGenModelFromNamespaceURI(namespaceURI, null);
	}
	
	protected static GenModel doResolveGenModelFromNamespaceURI(URI namespaceURI, ResourceSet resourceSet) {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
		}
		
		@SuppressWarnings("deprecation")
		final Map<String, URI> packageNamespaceToGenModelLocationMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap();
		String namespace = namespaceURI.toString();
		
		if (packageNamespaceToGenModelLocationMap.containsKey(namespace)) {
			//This is a file URI!
			URI genModelURI = packageNamespaceToGenModelLocationMap.get(namespace);
			GenModel genModel = EcoreIOUtil.loadModel(genModelURI, resourceSet);
			return genModel;
		}
		
		return null;
	}
	
	
//	//NOTE: The following alternative implementation works but uses the Eclipse extension point registry instead.
//	public static GenModel findGenModel(URI metaModelURI) {
//		//TODO: Later on might also search file system etc.
//		return findGenModelInRegistry(metaModelURI);
//	}
//	
//	public static GenModel findGenModelInRegistry(URI metaModelURI) {
//		IConfigurationElement metaModelConfigurationElement = findMetaModelConfigurationElement(metaModelURI);
//		return findGenModelInRegistry(metaModelConfigurationElement);
//	}
//	
//	public static GenModel findGenModelInRegistry(IConfigurationElement metaModelConfigurationElement) {
//		if (metaModelConfigurationElement == null) {
//			return null;
//		}
//					
//		IContributor contributor = metaModelConfigurationElement.getContributor();
//		String pluginName = contributor.getName();
//		
//		String rawRelativeGenModelPath = metaModelConfigurationElement.getAttribute("genModel");
//		
//		return EcoreIOUtil.loadModelFromPlugin(pluginName, rawRelativeGenModelPath);
//	}
//	
//	
//	private static IConfigurationElement findMetaModelConfigurationElement(URI metaModelURI) {
//		if (metaModelURI == null) {
//			return null;
//		}
//		
//		String rawMetaModelURI = metaModelURI.toString();
//		
//		//Process the registered extensions to find one for the respective meta model URI.
//		List<IExtension> extensions = ExtensionUtil.getExtensions("org.eclipse.emf.ecore.generated_package");
//		
//		for (IExtension extension : extensions) {
//			IConfigurationElement configurationElement = ExtensionUtil.getConfigurationElement("package", extension);
//			String rawCompareMetaModelURI = configurationElement.getAttribute("uri");
//			
//			if (rawMetaModelURI.equals(rawCompareMetaModelURI)) {
//				return configurationElement;
//			}
//		}
//		
//		return null;
//	}
	
	public static GenClass resolveGenClass(EClass eClass, GenModel genModel) {
		Iterator<EObject> iterator = genModel.eAllContents();
		
		while (iterator.hasNext()) {
			EObject eObject = iterator.next();
			
			if (eObject instanceof GenClass) {
				GenClass genClass = (GenClass) eObject;
				EClass genClassEClass = genClass.getEcoreClass();
				
				if (eClass == genClassEClass) {
					return genClass;
				}
			}
		}
		
		return null;
	}
	
	public static GenFeature resolveGenFeature(EStructuralFeature eStructuralFeature, GenModel genModel) {
		Iterator<EObject> iterator = genModel.eAllContents();
		
		while (iterator.hasNext()) {
			EObject eObject = iterator.next();
			
			if (eObject instanceof GenFeature) {
				GenFeature genFeature = (GenFeature) eObject;
				EStructuralFeature genFeatureEClass = genFeature.getEcoreFeature();
				
				if (eStructuralFeature == genFeatureEClass) {
					return genFeature;
				}
			}
		}
		
		return null;
	}
	
	
	
	public static String resolveBasePackageFromRegisteredGenModel(EPackage ePackage) {
		String namespace = ePackage.getNsURI();
		GenModel genModel = EcoreResolverUtil.resolveGenModelFromNamespace(namespace);
		
		if (genModel == null) {
			return null;
		}
		
		List<GenPackage> genPackages = genModel.getGenPackages();
		
		if (genPackages.isEmpty()) {
			return null;
		}
		
		GenPackage genPackage = genPackages.get(0);
		return genPackage.getBasePackage();
	}
	
	/**
	 * @deprecated use {@link ResourceUtil.makeFileRelativeToWorkspace()} instead. 
	 */
	@Deprecated
	public static IFile makeFileRelativeToWorkspace(IFile file) {
		return ResourceUtil.makeFileRelativeToWorkspace(file);
	}
	
//	public static IProject resolveProjectFromResourceURI(URI resourceURI) {
//		IFile file = resolveAbsoluteFileFromResourceURI(resourceURI);
//		file = makeFileRelativeToWorkspace(file);
//		
//		IProject project = file.getProject();
//		
//		return project;
//	}
//	
//	//TODO: This is not working properly
//	public static IFile resolveAbsoluteFileFromResourceURI(URI resourceURI) {
//		try {
//			URL resourceURL = new URL(resourceURI.toString());
//
//			//Make the URL absolute. 
//			return ResourceUtil.createAbsoluteFile(resourceURL);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
	
	public static IProject resolveProjectFromResourceURI(URI resourceURI) {
		IFile file = resolveRelativeFileFromResourceURI(resourceURI);
//		file = ResourceUtil.makeFileRelativeToWorkspace(file);

		IProject project = file.getProject();

		return project;
	}
	
	public static IFile resolveRelativeFileFromEObject(EObject eObject) {
		Resource resource = eObject.eResource();
		
		if (resource == null) {
			return null;
		}
		
		return resolveRelativeFileFromResource(resource);
	}
	
	public static IFile resolveRelativeFileFromResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		
		URI uri = resource.getURI();
		return resolveRelativeFileFromResourceURI(uri);
	}
	
	public static IFile resolveRelativeFileFromResourceURI(URI resourceURI) {
		String uriString = null;
		
		if (resourceURI.isFile()) {
			uriString = resourceURI.toFileString();
		} else if (resourceURI.isPlatform()) {
			uriString = resourceURI.toPlatformString(true);
		}
		
		if (uriString == null) {
			return null;
		}
		
		IPath path = new Path(uriString);
		IFile file = ResourceUtil.makePathRelativeToWorkspace(path);
		
		return file;
	}
	
	public static IContainer resolveContainerFromEObject(EObject eObject) {
		if (eObject == null) {
			return null;
		}
		
		Resource resource = eObject.eResource();
		return resolveContainerFromResource(resource);
	}
	
	public static IContainer resolveContainerFromResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		
		URI uri = resource.getURI();
		IFile file = EcoreResolverUtil.resolveRelativeFileFromResourceURI(uri);
		
		if (file == null) {
			return null;
		}
		
		return file.getParent();
	}
	
	public static List<Resource> resolveResourcesFromModels(List<EObject> models) {
		List<Resource> resources = new ArrayList<Resource>();
		
		for (EObject model : models) {
			Resource resource = model.eResource();
			
			if (resource == null) {
				System.err.println("Resource of \"" + model + "\" is null");
				continue;
			}
			
			if (!resources.contains(resource)) {
				resources.add(resource);
			}
		}
		
		return resources;
	}
	
	
	
	//NOTE: This code seems functional but has never been completed or tested
	public static List<EPackage> resolveReferencedMetaModels(EPackage metaModel) {
		List<EObject> referencedModels = resolveReferencedModels(metaModel);
		List<EPackage> referencedMetaModels = CollectionsUtil.filter(referencedModels, EPackage.class);
		
		return referencedMetaModels;
	}
	
//	public static List<EObject> resolveAllReferencedModels(EObject model) {
//		
//	}
	

	//TODO: Are there other ways of referencing external models than types of typed elements and super types of classes? 
	public static List<Resource> resolveReferencedResources(Resource originalResource) {
		List<Resource> referencedResources = new ArrayList<Resource>();

		List<EObject> models = originalResource.getContents();
		
		for (EObject model : models) {
			Iterator<EObject> iterator = model.eAllContents();
			
			while (iterator.hasNext()) {
				EObject eObject = iterator.next();
				
				if (eObject instanceof ETypedElement) {
					ETypedElement eTypedElement = (ETypedElement) eObject;
					
					EClassifier eType = eTypedElement.getEType();
					Resource otherResource = eType.eResource();
					
					addReferencedResourceIfNotAlreadyPresent(originalResource, otherResource, referencedResources);
				}
				
				if (eObject instanceof EClass) {
					EClass eClass = (EClass) eObject;
					
					List<EClass> superTypes = eClass.getEAllSuperTypes();
					
					for (EClass superType : superTypes) {
						Resource otherResource = superType.eResource();
						
						addReferencedResourceIfNotAlreadyPresent(originalResource, otherResource, referencedResources);
					}
				}
			}
		}
		
		return referencedResources;
	}
	
	private static void addReferencedResourceIfNotAlreadyPresent(Resource originalResource, Resource otherResource, List<Resource> referencedResources) {
		if (otherResource != originalResource) {
			if (!referencedResources.contains(otherResource)) {
				referencedResources.add(otherResource);
			}
		}
	}
	
//	public static void resolveReferencedResources(EObject model) {
//		
//	}
	
	public static List<EObject> resolveReferencedModels(EObject originalModel) {
		List<EObject> referencedModels = new ArrayList<EObject>();
		
		Resource originalResource = originalModel.eResource();
		List<Resource> referencedResources = resolveReferencedResources(originalResource);
		
		for (Resource referencedResource : referencedResources) {
			List<EObject> models = referencedResource.getContents();
			
			for (EObject model : models) {
				referencedModels.add(model);
			}
		}
		
		return referencedModels;
	}
	
	
	public static IPath resolveRelativePathBetweenModels(EObject originalModel, EObject referenceModel) {
		Resource originalResource = originalModel.eResource();
		Resource referenceResource = referenceModel.eResource();
		return resolveRelativePathBetweenResources(originalResource, referenceResource);
	}
	
	public static IPath resolveRelativePathBetweenResources(Resource originalResource, Resource referenceResource) {
		IFile originalFile = EcoreResolverUtil.resolveRelativeFileFromResource(originalResource);
		IFile referenceFile = EcoreResolverUtil.resolveRelativeFileFromResource(referenceResource);
		
		return ResourceUtil.makeResourcePathRelativeToFile(originalFile, referenceFile);
	}
}
