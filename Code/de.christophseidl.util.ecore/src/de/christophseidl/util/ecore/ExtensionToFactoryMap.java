package de.christophseidl.util.ecore;

public class ExtensionToFactoryMap {
	private String extension;
	private Object resourceFactory;
	
	public ExtensionToFactoryMap(String extension, Object resourceFactory) {
		this.extension = extension;
		this.resourceFactory = resourceFactory;
	}

	public String getExtension() {
		return extension;
	}

	public Object getResourceFactory() {
		return resourceFactory;
	}
}
