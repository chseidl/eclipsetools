package de.christophseidl.util.ecore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreReflectionUtil {
	public static EClass findContainerForEClass(EClass eClass) {
		EReference eReference = findContainingReferenceForEClass(eClass);
		
		if (eReference == null) {
			return null;
		}
		
		return eReference.getEContainingClass();
	}
	
	public static EReference findContainingReferenceForEClass(EClass eClass) {
		//There is no perfect match if the EClass has multiple incoming
		//containment references. Make a guess in this case and simply take the first one.
		List<EReference> containingReferences = findContainingReferencesForEClass(eClass);
		
		if (containingReferences.isEmpty()) {
			return null;
		}
		
		return containingReferences.get(0);
	}
	
	public static List<EReference> findContainingReferencesForEClass(EClass eClass) {
		//On meta level, we need all EClasses that have a reference that points
		//to this EClass (or one of its super classes) and has type containment.
		EObject rootContainer = EcoreUtil.getRootContainer(eClass);
		Iterator<EObject> iterator = rootContainer.eAllContents();
		List<EReference> containingReferences = new ArrayList<EReference>();
		
		while(iterator.hasNext()) {
			EObject content = iterator.next();
			
			//Skip the inspected EClass itself.
			//Pointer equality intended.
			if (content == eClass) {
				continue;
			}
			
			if (content instanceof EClass) {
				EClass otherEClass = (EClass) content;
				
				List<EReference> eReferences = otherEClass.getEReferences();
				
				for (EReference eReference : eReferences) {
					EClass eReferenceType = eReference.getEReferenceType();
					
					if (eReference.isContainment() && isAssignableFrom(eReferenceType, eClass)) {
						containingReferences.add(eReference);
					}
				}
			}
		}
		
		return containingReferences;
	}
	
	public static List<EReference> findContainmentReferencesFromEClassToEClass(EClass sourceEClass, EClass targetEClass) {
		List<EReference> containingReferences = new ArrayList<EReference>();
		List<EReference> outgoingContainmentReferences = sourceEClass.getEAllContainments();
		
		for (EReference outgoingContainmentReference : outgoingContainmentReferences) {
			EClassifier currentTargetEClassifier = outgoingContainmentReference.getEType();
			
			if (currentTargetEClassifier instanceof EClass) {
				EClass currentTargetEClass = (EClass) currentTargetEClassifier;
				
				if (isAssignableFrom(currentTargetEClass, targetEClass)) {
					containingReferences.add(outgoingContainmentReference);
				}
			}
		}
		
		return containingReferences;
	}
	
	public static boolean isAssignableFrom(EClass potentialSuperClass, EClass potentialSubClass) {
		if (EcoreUtil.equals(potentialSuperClass, potentialSubClass)) {
			return true;
		}
		
		List<EClass> allSuperTypes = potentialSubClass.getEAllSuperTypes();
		
		for (EClass superType : allSuperTypes) {
			if (EcoreUtil.equals(superType, potentialSuperClass)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static EStructuralFeature findStructuralFeature(EObject eObject, int structuralFeatureId) {
		if (structuralFeatureId != Notification.NO_FEATURE_ID) {
			EClass eClass = eObject.eClass();
			return eClass.getEStructuralFeature(structuralFeatureId);
		}
		
		return null;
	}
	
	public static EStructuralFeature findEquivalentStructuralFeature(EStructuralFeature sourceEStructuralFeature, EClass targetEClass) {
		String sourceName = sourceEStructuralFeature.getName();
		EClassifier sourceEType = sourceEStructuralFeature.getEType();
		int sourceLowerBound = sourceEStructuralFeature.getLowerBound();
		int sourceUpperBound = sourceEStructuralFeature.getUpperBound();
		
		List<EStructuralFeature> allTargetEStructuralFeatures = targetEClass.getEAllStructuralFeatures();
		
		for (EStructuralFeature targetEStructuralFeature : allTargetEStructuralFeatures) {
			String targetName = targetEStructuralFeature.getName();
			EClassifier targetEType = targetEStructuralFeature.getEType();
			int targetLowerBound = targetEStructuralFeature.getLowerBound();
			int targetUpperBound = targetEStructuralFeature.getUpperBound();
			
			if (sourceName.equals(targetName) && sourceEType == targetEType && sourceLowerBound == targetLowerBound && sourceUpperBound == targetUpperBound) {
				return targetEStructuralFeature;
			}
		}
		
		return null;
	}
}
