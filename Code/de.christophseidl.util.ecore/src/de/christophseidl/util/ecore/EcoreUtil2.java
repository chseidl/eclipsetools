package de.christophseidl.util.ecore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;

public class EcoreUtil2 {
	public static<T extends ENamedElement> void sortByName(List<T> namedElements) {
		Collections.sort(namedElements, new Comparator<T>() {
			@Override
			public int compare(T namedElement1, T namedElement2) {
				if (namedElement1 == namedElement2) {
					return 0;
				}
				
				if (namedElement1 == null) {
					return 1;
				}
				
				String name1 = namedElement1.getName();
				String name2 = namedElement2.getName();
				
				return name1.compareTo(name2);
			}
		});
	}
	
	public static String printModel(EObject model) {
		return doPrintModel(model, 0, ""); 
	}
	
	private static String doPrintModel(EObject model, int level, String output) {
		output += indent(level) + model + "\n";
		
		List<EObject> contents = model.eContents();
		
		for (EObject child : contents) {
			output = doPrintModel(child, level + 1, output);
		}
		
		return output;
	}
	
	private static String indent(int level) {
		String output = "";
		
		for (int i = 0; i < level; i++) {
			output += "  ";
		}
		
		return output;
	}
	
	/**
	 * Copy all values of structural features with the same name, type and multiplicity.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EObject> T cloneToEObjectOfEClass(EObject sourceEObject, EClass targetEObjectEClass) {
		EObject targetEObject = EcoreFactory.eINSTANCE.create(targetEObjectEClass);
		
		EClass sourceEClass = sourceEObject.eClass();
		EClass targetEClass = targetEObject.eClass();
		
		List<EStructuralFeature> allSourceEStructuralFeatures = sourceEClass.getEAllStructuralFeatures();
		
		for (EStructuralFeature sourceEStructuralFeature : allSourceEStructuralFeatures) {
			if (sourceEObject.eIsSet(sourceEStructuralFeature) && !sourceEStructuralFeature.isDerived()) {
				
				EStructuralFeature targetEStructuralFeature = EcoreReflectionUtil.findEquivalentStructuralFeature(sourceEStructuralFeature, targetEClass);
				
				if (targetEStructuralFeature != null && !targetEStructuralFeature.isDerived() && targetEStructuralFeature.isChangeable()) {
					//TODO: What if lists?
					Object value = sourceEObject.eGet(sourceEStructuralFeature);
					targetEObject.eSet(targetEStructuralFeature, value);
				}
			}
		}
		
		return (T) targetEObject;
	}
}
