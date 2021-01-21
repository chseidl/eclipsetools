package de.christophseidl.util.eclipse.resourcehandlers.pluginxml;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.christophseidl.util.eclipse.ResourceUtil;
import de.christophseidl.util.eclipse.resourcehandlers.AbstractResourceHandler;

public class PluginXmlHandler extends AbstractResourceHandler {
	private List<ExtensionElement> extensions;
	private List<ExtensionPointElement> extensionPoints;
	
	public PluginXmlHandler() {
		super();
	}

	public PluginXmlHandler(IFile file) {
		super(file);
	}

	public PluginXmlHandler(IJavaProject javaProject) {
		super(javaProject);
	}

	public PluginXmlHandler(IProject project) {
		super(project);
	}
	
	public PluginXmlHandler(Document xmlDocument) {
		loadFromXmlDocument(xmlDocument);
	}
	
	@Override
	protected String getFileDefaultLocationRelativeToProject() {
		return "plugin.xml";
	}
	
	@Override
	protected void initialize() {
		extensions = new ArrayList<ExtensionElement>();
		extensionPoints = new ArrayList<ExtensionPointElement>();
	}
	
	@Override
	protected void doLoadFromFile(IFile pluginXmlFile) throws Exception {
		File rawPluginXmlFile = ResourceUtil.resourceToFile(pluginXmlFile);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(rawPluginXmlFile);
		
		loadFromXmlDocument(document);
	}
	
	private void loadFromXmlDocument(Document document) {
		document.getDocumentElement().normalize();
		
		Element pluginElement = document.getDocumentElement();
		
		if (pluginElement != null && pluginElement.getNodeName().equals("plugin")) {
			
			NodeList extensionsOrExtensionPoints = pluginElement.getChildNodes();
			
			for (int i = 0; i < extensionsOrExtensionPoints.getLength(); i++) {
				Node extensionOrExtensionPointNode = extensionsOrExtensionPoints.item(i);
				
				if (extensionOrExtensionPointNode instanceof Element) {
					Element extensionOrExtensionPointElement = (Element) extensionOrExtensionPointNode;
					
					//extension-point
					if (ExtensionPointElement.isMatchingElement(extensionOrExtensionPointElement)) {
						ExtensionPointElement extensionPoint = new ExtensionPointElement(extensionOrExtensionPointElement);
						extensionPoints.add(extensionPoint);
						continue;
					}
					
					//extension
					if (ExtensionElement.isMatchingElement(extensionOrExtensionPointElement)) {
						ExtensionElement extension = new ExtensionElement(extensionOrExtensionPointElement);
						extensions.add(extension);
						continue;
					}
				}
				
//				System.err.println("Found unexpected node \"" + extensionOrExtensionPointNode.getNodeName() + "\" in plugin.xml.");
			}
		}
	}
	
	
	@Override
	public String serialize() {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
	 
			//Build dom tree
			Document document = documentBuilder.newDocument();
			//TODO: Write the eclipse declaration (currently workaround afterwards, see below)
			Element pluginElement = document.createElement("plugin");
			document.appendChild(pluginElement);
			
			for (ExtensionPointElement extensionPoint : extensionPoints) {
				Element element = extensionPoint.createElement(document);
				pluginElement.appendChild(element);
			}
			
			for (ExtensionElement extension : extensions) {
				Element element = extension.createElement(document);
				pluginElement.appendChild(element);
			}

			
			//Transform dom tree to string
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			
			//Pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			//This doesn't seem to work - no harm done but fix some time for esthetics.
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			
			transformer.transform(source, result);
			
			String content = writer.toString();
			
			//This is hacky!
			//Replace first newline with the eclipse declaration and the found newline.
			content = content.replaceFirst("(\\r\\n|\\r|\\n)", "$1<?eclipse version=\"3.4\"?>$1");
			
			return content;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void addExtension(ExtensionElement extension) {
		extensions.add(extension);
	}
	
	public void removeExtension(ExtensionElement extension) {
		extensions.remove(extension);
	}
	
	public void removeExtensions(String searchedPoint) {
		List<ExtensionElement> matchingExtensions = getExtensions(searchedPoint);
		extensions.removeAll(matchingExtensions);
	}
	
	public List<ExtensionElement> getExtensions() {
		return extensions;
	}
	
	public void addExtensionPoint(ExtensionPointElement extensionPoint) {
		extensionPoints.add(extensionPoint);
	}
	
	public void removeExtensionPoint(ExtensionPointElement extensionPoint) {
		extensionPoints.remove(extensionPoint);
	}
	
	public List<ExtensionPointElement> getExtensionPoints() {
		return extensionPoints;
	}
	
	public boolean hasExtension(String searchedPoint) {
		if (searchedPoint == null) {
			return false;
		}
		
		for (ExtensionElement extension : extensions) {
			String point = extension.getPoint();
			
			if (searchedPoint.equals(point)) {
				return true;
			}
		}
		
		return false;
	}
	
	public ExtensionElement getExtension(String searchedPoint) {
		List<ExtensionElement> matchingExtensions = getExtensions(searchedPoint);
		
		if (matchingExtensions.isEmpty()) {
			return null;
		}
		
		return matchingExtensions.get(0);
	}
	
	public List<ExtensionElement> getExtensions(String searchedPoint) {
		List<ExtensionElement> matchingExtensions = new ArrayList<ExtensionElement>();
		
		if (searchedPoint == null) {
			return matchingExtensions;
		}
		
		for (ExtensionElement extension : extensions) {
			String point = extension.getPoint();
			
			if (searchedPoint.equals(point)) {
				matchingExtensions.add(extension);
			}
		}
		
		return matchingExtensions;
	}
}
