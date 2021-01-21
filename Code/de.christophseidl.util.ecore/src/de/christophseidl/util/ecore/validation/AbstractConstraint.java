package de.christophseidl.util.ecore.validation;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.EMFEventType;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.model.ConstraintSeverity;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.model.IModelConstraint;
import org.eclipse.emf.validation.service.AbstractConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintDescriptor;

public abstract class AbstractConstraint<T extends EObject> extends AbstractModelConstraint implements IModelConstraint {
	protected IStatus createSuccessStatus() {
		return Status.OK_STATUS;
	}
	
	protected IStatus createErrorStatus(String message, EObject element) {
		return new ConstraintStatus(this, element, message, Collections.singleton(element));
	}
	
	protected IStatus createWarningStatus(String message, EObject element) {
		int code = -1; //Has no semantics.
		return new ConstraintStatus(this, element, IStatus.WARNING, code, message, Collections.singleton(element));
	}
	
	@Override
	public IConstraintDescriptor getDescriptor() {
		return new AbstractConstraintDescriptor() {
			@Override
			public boolean targetsTypeOf(EObject eObject) {
				return true;
			}
			
			@Override
			public boolean targetsEvent(Notification notification) {
				return false;
			}
			
			@Override
			public void removeCategory(Category category) {
			}
			
			@Override
			public boolean isLive() {
				return false;
			}
			
			@Override
			public boolean isBatch() {
				return true;
			}
			
			@Override
			public int getStatusCode() {
				return 0;
			}
			
			@Override
			public ConstraintSeverity getSeverity() {
				return AbstractConstraint.this.getSeverity();
			}
			
			@Override
			public String getPluginId() {
				return AbstractConstraint.class.getPackage().getName();
			}
			
			@Override
			public String getName() {
				return "name";
			}
			
			@Override
			public String getMessagePattern() {
				return "message";
			}
			
			@Override
			public String getId() {
				return null;
			}
			
			@Override
			public EvaluationMode<?> getEvaluationMode() {
				return EvaluationMode.BATCH;
			}
			
			@Override
			public String getDescription() {
				return "description";
			}
			
			@Override
			public Set<Category> getCategories() {
				return null;
			}
			
			@Override
			public String getBody() {
				return null;
			}
			
			@Override
			public void addCategory(Category category) {
			}
		};
	}

	protected ConstraintSeverity getSeverity() {
		return ConstraintSeverity.ERROR;
	}
	
	//As EMFText recreates the model with each parser pass, only batch validation will work.
	@SuppressWarnings("unchecked")
	@Override
	public IStatus validate(IValidationContext context) {
		EObject target = context.getTarget();
		EMFEventType eventType = context.getEventType();
		
		//Batch validation
		if (eventType == EMFEventType.NULL) {
			try {
				T typedTarget = (T) target;
				return doValidate(typedTarget);
			} catch(ClassCastException e) {
				return createErrorStatus(getClass().getSimpleName() + " misconfigured: target is not of the specified type (Is: " + target.getClass().getSimpleName() + ").", target);
			}
		}
		
		return createSuccessStatus();
	}
	
	protected abstract IStatus doValidate(T model);
}
