package de.christophseidl.util.gef.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IValidationListener;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.emf.validation.service.ValidationEvent;
import org.eclipse.gef.commands.CommandStack;

import de.christophseidl.util.ecore.EcoreIOUtil;

public abstract class EcoreGraphicalEditor<T extends EObject> extends IFileBasedGraphicalEditor<T, T> {
	private Map<EObject, List<IConstraintStatus>> problemStatus;

	public EcoreGraphicalEditor() {
		problemStatus = new HashMap<EObject, List<IConstraintStatus>>();
	}
	
	@Override
	protected void registerListeners() {
		super.registerListeners();
		
		ModelValidationService modelValidationService = ModelValidationService.getInstance();
		
		modelValidationService.addValidationListener(new IValidationListener() {
			@Override
			public void validationOccurred(ValidationEvent event) {
				T model = getDataModel();
				Collection<?> targets = event.getValidationTargets();
				
				if (targets.contains(model)) {
					List<IConstraintStatus> validationResults = event.getValidationResults();
					updateProblemStatus(validationResults);
					refreshVisuals();
				}
			}
		});
	}
	
	@Override
	protected void doSaveDataModel(T model, IFile file) throws IOException {
		if (model == null) {
			return;
		}
		
		Resource originalResource = model.eResource();

		
		//TODO: Respect file for resource!
		Resource resource = originalResource;
		
		if (resource == null) {
			return;
		}
		
		try {
			resource.save(null);
			
			CommandStack commandStack = getCommandStack();
			commandStack.markSaveLocation();
		} catch (IOException e) {
			resource = null;
			throw e;
		}
	}
	

	@Override
	protected T doLoadDataModel(IFile file) {
		return EcoreIOUtil.loadModel(file);	
	}
	
	protected void updateProblemStatus(List<IConstraintStatus> validationResults) {
		problemStatus.clear();
		
		for (IConstraintStatus validationResult : validationResults) {
			Set<EObject> resultLocus = validationResult.getResultLocus();

			for (EObject eObject : resultLocus) {
				if (!problemStatus.containsKey(eObject)) {
					problemStatus.put(eObject, new ArrayList<IConstraintStatus>());
				}
				
				List<IConstraintStatus> constraintStatusList = problemStatus.get(eObject);
				constraintStatusList.add(validationResult);
			}
		}
	}
	
	public boolean hasError(EObject eObject) {
		return hasStatus(eObject, IStatus.ERROR);
	}
	
	public boolean hasWarning(EObject eObject) {
		return hasStatus(eObject, IStatus.WARNING);
	}
	
	private boolean hasStatus(EObject eObject, int searchedSeverity) {
		List<IConstraintStatus> constraintStatusList = getConstraintStatusList(eObject);
		
		if (constraintStatusList == null) {
			return false;
		}
		
		for (IConstraintStatus constraintStatus : constraintStatusList) {
			int severity = constraintStatus.getSeverity();
			
			if (severity == searchedSeverity) {
				return true;
			}
		}
		
		return false;
	}

	private List<IConstraintStatus> getConstraintStatusList(EObject eObject) {
		if (problemStatus.containsKey(eObject)) {
			return problemStatus.get(eObject);
		}
		
		return null;
	}
	
	@Override
	protected T calculateViewModelFromDataModel(T dataModel) {
		//Data model and view model are assumed to be equivalent.
		//TODO: Revise this at some point
		return dataModel;
	}
}
