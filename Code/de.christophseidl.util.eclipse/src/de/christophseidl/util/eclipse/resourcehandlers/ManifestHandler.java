package de.christophseidl.util.eclipse.resourcehandlers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

import de.christophseidl.util.java.StringUtil;

//TODO: Resolve values from localization?!?!
public class ManifestHandler extends AbstractResourceHandler {
	private static final String manifestValueSeparator = "," + endl + " ";
	
	private String pluginId;
	private String bundleName;
	private String bundleVendor;
	private String bundleVersion;
	private String activatorClass;

	private boolean singleton;
	private boolean lazyActivation;
	
	private String requiredExecutionEnvironment;
	
	private Collection<String> requiredBundles;
	private Collection<String> exportedPackages;
	private Collection<String> importedPackages;
	private Collection<String> bundleClasspathEntries;
	
	public ManifestHandler() {
		super();
	}

	public ManifestHandler(IFile file) {
		super(file);
	}

	public ManifestHandler(IJavaProject javaProject) {
		super(javaProject);
	}

	public ManifestHandler(IProject project) {
		super(project);
	}
	
	public ManifestHandler(Manifest manifest) {
		loadFromManifest(manifest);
	}
	
	@Override
	protected String getFileDefaultLocationRelativeToProject() {
		return "META-INF/MANIFEST.MF";
	}
	
	@Override
	protected void initialize() {
		pluginId = "";
		bundleName = "";
		bundleVendor = "";
		bundleVersion = "1.0.0";
		activatorClass = null;
		
		singleton = false;
		lazyActivation = false;
		
		requiredExecutionEnvironment = "JavaSE-1.7";
		
		requiredBundles = new LinkedHashSet<String>();
		exportedPackages = new LinkedHashSet<String>();
		importedPackages = new LinkedHashSet<String>();
		bundleClasspathEntries = new LinkedHashSet<String>();
	}
	
	@Override
	protected void doLoadFromFile(IFile manifestFile) throws Exception {
		IPath manifestFilePath = manifestFile.getLocation();
		
		InputStream manifestInputStream = new FileInputStream(manifestFilePath.toString());
		Manifest manifest = new Manifest(manifestInputStream);
		loadFromManifest(manifest);
	}
	
	private void loadFromManifest(Manifest m) {
		Attributes mainAttributes = m.getMainAttributes();
		String rawBundleSymbolicName = mainAttributes.getValue("Bundle-SymbolicName");
		int index = rawBundleSymbolicName.indexOf(";");
		int pluginIdLastIndex = (index == -1 ? rawBundleSymbolicName.length() : index);
		
		pluginId = rawBundleSymbolicName.substring(0, pluginIdLastIndex);
		bundleName = mainAttributes.getValue("Bundle-Name");
		bundleVendor = mainAttributes.getValue("Bundle-Vendor");
		bundleVersion = mainAttributes.getValue("Bundle-Version");
		activatorClass = mainAttributes.getValue("Bundle-Activator");
		
		singleton = rawBundleSymbolicName.matches("(.*);\\s*singleton\\s*:=\\s*true");
		lazyActivation = (mainAttributes.getValue("Bundle-ActivationPolicy") != null);
		
		requiredExecutionEnvironment = mainAttributes.getValue("Bundle-RequiredExecutionEnvironment");
		
		final String splitPattern = "\\s*,\\s*\n?";
		
		StringUtil.explode(mainAttributes.getValue("Require-Bundle"), splitPattern, requiredBundles);
		StringUtil.explode(mainAttributes.getValue("Export-Package"), splitPattern, exportedPackages);
		StringUtil.explode(mainAttributes.getValue("Import-Package"), splitPattern, importedPackages);
		StringUtil.explode(mainAttributes.getValue("Bundle-ClassPath"), splitPattern, bundleClasspathEntries);
	}
	
	@Override
	public String serialize() {
		String content = "";
		
		content += "Manifest-Version: 1.0" + endl;
		content += "Bundle-ManifestVersion: 2" + endl;
		content += "Bundle-Name: " + bundleName + endl;
		content += "Bundle-SymbolicName: " + pluginId + (singleton ? ";singleton:=true" : "") + endl;
		content += "Bundle-Version: " + bundleVersion + endl;
		
		if (bundleVendor != null && !bundleVendor.isEmpty()) {
			content += "Bundle-Vendor: " + bundleVendor + endl;
		}
		
		if (!requiredBundles.isEmpty()) {
			content += "Require-Bundle: " + StringUtil.implode(requiredBundles, manifestValueSeparator) + endl;
		}
		
		if (lazyActivation) { 
			content += "Bundle-ActivationPolicy: lazy" + endl;
		}
		
		if (requiredExecutionEnvironment != null) {
			content += "Bundle-RequiredExecutionEnvironment: " + requiredExecutionEnvironment + endl;
		}
		
		if (!exportedPackages.isEmpty()) {
			content += "Export-Package: " + StringUtil.implode(exportedPackages, manifestValueSeparator) + endl;
		}
		
		if (activatorClass != null) {
			content += "Bundle-Activator: " + activatorClass + endl;
		}

		if (!importedPackages.isEmpty()) {
			content += "Import-Package: " + StringUtil.implode(importedPackages, manifestValueSeparator) + endl;
		}
		
		if (!bundleClasspathEntries.isEmpty()) {
			content += "Bundle-ClassPath: " + "." + manifestValueSeparator + StringUtil.implode(bundleClasspathEntries, manifestValueSeparator) + endl;
		} 
		
		return content;
	}
	
	public boolean requiresBundle(String bundleId) {
		for (String requiredBundle : requiredBundles) {
			if (requiredBundle.matches(bundleId + "($|;).*")) {
//			if (requiredBundle.equals(bundleId)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean exportsPackage(String qualifiedPackageName) {
		for (String exportedPackage : exportedPackages) {
			if (exportedPackage.equals(qualifiedPackageName)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean importsPackage(String qualifiedPackageName) {
		for (String importedPackage : importedPackages) {
			if (importedPackage.equals(qualifiedPackageName)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public void setActivatorClass(String activatorClass) {
		this.activatorClass = activatorClass;
	}

	public String getBundleName() {
		return bundleName;
	}

	public String getActivatorClass() {
		return activatorClass;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	//Alias
	public void setBundleSymbolicName(String bundleSymbolicName) {
		setPluginId(bundleSymbolicName);
	}
	
	public String getPluginId() {
		return pluginId;
	}

	//Alias
	public String getBundleSymbolicName() {
		return getPluginId();
	}
	
	public void addRequiredBundle(String requiredBundle) {
		if (requiredBundle == null || requiredBundle.isEmpty()) {
			return;
		}
		
		requiredBundles.add(requiredBundle);
	}
	
	public void addExportedPackage(String exportedPackage) {
		if (exportedPackage == null || exportedPackage.isEmpty()) {
			return;
		}
		
		exportedPackages.add(exportedPackage);
	}
	
	public void addImportedPackage(String importedPackage) {
		if (importedPackage == null || importedPackage.isEmpty()) {
			return;
		}
		
		importedPackages.add(importedPackage);
	}
	
	public void addBundleClasspathEntry(String bundleClasspathEntry) {
		bundleClasspathEntries.add(bundleClasspathEntry);
	}

	public Collection<String> getRequiredBundles() {
		return requiredBundles;
	}

	public Collection<String> getExportedPackages() {
		return exportedPackages;
	}

	public Collection<String> getImportedPackages() {
		return importedPackages;
	}

	public Collection<String> getBundleClasspathEntries() {
		return bundleClasspathEntries;
	}

	public String getRequiredExecutionEnvironment() {
		return requiredExecutionEnvironment;
	}

	public void setRequiredExecutionEnvironment(String requiredExecutionEnvironment) {
		this.requiredExecutionEnvironment = requiredExecutionEnvironment;
	}

	public boolean getLazyActivation() {
		return lazyActivation;
	}

	public void setLazyActivation(boolean lazyActivation) {
		this.lazyActivation = lazyActivation;
	}

	public String getBundleVendor() {
		return bundleVendor;
	}

	public void setBundleVendor(String bundleVendor) {
		this.bundleVendor = bundleVendor;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String bundleVersion) {
		this.bundleVersion = bundleVersion;
	}

	public boolean getSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

}
