package de.christophseidl.util.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.christophseidl.util.java.CollectionsUtil;

public class ExtensionUtil {
	public static List<IExtension> getExtensions(String pointId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(pointId);
		
		if (point == null) {
			System.err.println("Extension point \"" + pointId + "\" not found.");
			return null;
		}
		
		IExtension[] extensions = point.getExtensions();
		return CollectionsUtil.toList(extensions);
	}
	
	public static IConfigurationElement getConfigurationElement(String name, IExtension extension) {
		List<IConfigurationElement> matchingConfigurationElements = getConfigurationElements(name, extension);
		
		if (!matchingConfigurationElements.isEmpty()) {
			return matchingConfigurationElements.get(0);
		}
		
		return null;
	}
	
	public static List<IConfigurationElement> getConfigurationElements(String name, IExtension extension) {
		IConfigurationElement[] configurationElements = extension.getConfigurationElements();
		List<IConfigurationElement> matchingConfigurationElements = new ArrayList<IConfigurationElement>();
		
		for (IConfigurationElement configurationElement : configurationElements) {
			String configurationElementName = configurationElement.getName();
			
			if (configurationElementName.equals(name)) {
				matchingConfigurationElements.add(configurationElement);
			}
		}
		
		return matchingConfigurationElements;
	}
}
