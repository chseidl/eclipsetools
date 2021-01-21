package de.christophseidl.util.eclipse.resourcehandlers.pluginxml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtensionPointElement extends AbstractXmlSerializable {
	private static final String elementName = "extension-point";
	
	private String id;
	private String name;
	private String schema;
	
	//Can extension points have content?
	
	public ExtensionPointElement(Element extensionPointElement) {
		//E.g., "<extension-point id="eu.vicci.ecosystem.sft.resource.sft_text.default_load_options" name="Default Load Options" schema="schema/default_load_options.exsd">"
		id = extensionPointElement.getAttribute("id");
		name = extensionPointElement.getAttribute("name");
		schema = extensionPointElement.getAttribute("schema");
	}
	
	public ExtensionPointElement(String id, String name, String schema) {
		this.id = id;
		this.name = name;
		this.schema = schema;
	}

	public static boolean isMatchingElement(Element element) {
		return element.getNodeName().equals(elementName);
	}
	
	@Override
	public Element createElement(Document xmlDocument) {
		Element element = xmlDocument.createElement(elementName);
		
		addAttribute("id", id, element, xmlDocument);
		addAttribute("name", name, element, xmlDocument);
		addAttribute("schema", schema, element, xmlDocument);
		
		return element;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
	
}
