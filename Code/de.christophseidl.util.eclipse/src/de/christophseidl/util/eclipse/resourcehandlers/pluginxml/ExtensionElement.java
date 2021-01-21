package de.christophseidl.util.eclipse.resourcehandlers.pluginxml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtensionElement extends AbstractXmlSerializable {
	private static final String elementName = "extension";
	
	private String point;
	private String id;
	private String name;
	
	//TODO: Find better way to represent content
	private NodeList content;
	
	public ExtensionElement(Element extensionElement) {
		//E.g., "<extension point="org.eclipse.core.resources.builders" id="eu.vicci.ecosystem.sft.resource.sft_text.builder" name="sft_text Builder">"
		point = extensionElement.getAttribute("point");
		id = extensionElement.getAttribute("id");
		name = extensionElement.getAttribute("name");
		
		content = extensionElement.getChildNodes();
	}
	
	public ExtensionElement(String point, String id, String name, NodeList content) {
		this.point = point;
		this.id = id;
		this.name = name;
		this.content = content;
	}
	
	public static boolean isMatchingElement(Element element) {
		return element.getNodeName().equals(elementName);
	}
	
	@Override
	public Element createElement(Document xmlDocument) {
		Element element = xmlDocument.createElement(elementName);

		addAttribute("point", point, element, xmlDocument);
		addAttribute("id", id, element, xmlDocument);
		addAttribute("name", name, element, xmlDocument);

		appendContent(element, xmlDocument);
		
		return element;
	}
	
	protected void appendContent(Element element, Document xmlDocument) {
		if (content != null) {
			for (int i = 0; i < content.getLength(); i++) {
				Node node = content.item(i);
				element.appendChild(node);
			}
		}
	}
	
	public String getPoint() {
		return point;
	}
	
	public void setPoint(String point) {
		this.point = point;
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
	
	public NodeList getContent() {
		return content;
	}
	
	public void setContent(NodeList content) {
		this.content = content;
	}
}
