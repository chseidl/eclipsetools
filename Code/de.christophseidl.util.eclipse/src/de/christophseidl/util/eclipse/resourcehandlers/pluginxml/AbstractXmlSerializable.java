package de.christophseidl.util.eclipse.resourcehandlers.pluginxml;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractXmlSerializable {
	public abstract Element createElement(Document xmlDocument);
	
	protected static Attr addAttribute(String attributeName, Object value, Element parentElement, Document xmlDocument) {
		if (value == null) {
			return null;
		}
		
		Attr attribute = xmlDocument.createAttribute(attributeName);
		attribute.setValue(value.toString());
		parentElement.setAttributeNode(attribute);
		
		return attribute;
	}
}
