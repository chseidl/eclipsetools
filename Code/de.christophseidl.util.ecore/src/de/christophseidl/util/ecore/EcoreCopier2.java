package de.christophseidl.util.ecore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreCopier2 extends EcoreUtil.Copier {
	private static final long serialVersionUID = 1L;

	private ResourceSet resourceSet = createResourceSet();

	protected ResourceSet createResourceSet() {
		return new ResourceSetImpl();
	}

	@SuppressWarnings("unchecked")
	public <T extends EObject> T copyWithResource(T eObject) {
		Resource originalResource = eObject.eResource();
		copyResource(originalResource);

		//Find copied element and return it.
		return (T) get(eObject);
	}
	
	public Resource copyResource(Resource resource) {
		Resource copiedResource = doCopyResource(resource, resourceSet);

		copyReferences();

		return copiedResource;
	}
	
	public List<Resource> copyAllResources(Collection<Resource> originalResources) {
		List<Resource> copiedResources = new LinkedList<Resource>();
		
		for (Resource originalResource : originalResources) {
			Resource copiedResource = doCopyResource(originalResource, resourceSet);
			copiedResources.add(copiedResource);
		}
		
		copyReferences();
		
		return copiedResources;
	}
	
	public <T extends EObject> List<T> copyAllWithResources(Collection<T> originalElements) {
		List<T> copiedElements = new LinkedList<T>();
		
		for (T originalElement : originalElements) {
			T copiedElement = copyWithResource(originalElement);
			copiedElements.add(copiedElement);
			
		}
		
		copyReferences();
		
		return copiedElements;
	}
	
	
	private Resource doCopyResource(Resource resource, ResourceSet copyResourceSet) {
		if (resource == null) {
			return null;
		}
		
		URI uri = resource.getURI();
		Resource copiedResource = copyResourceSet.createResource(uri);
		
		List<EObject> originalContents = resource.getContents();
		Collection<EObject> copiedContents = copyAll(originalContents);
		
		List<EObject> contents = copiedResource.getContents();
		contents.addAll(copiedContents);
		
		return copiedResource;
	}
	


	
	@Override
	public void clear() {
		//Create a new resource set on fresh usage of the copier.
		resourceSet = createResourceSet();
		super.clear();
	}
}
