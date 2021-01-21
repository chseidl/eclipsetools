package de.christophseidl.util.gef.editor;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.ui.IEditorInput;

/**
 * Base class for an editor that uses a view model that is based on a data model, e.g., as a subset of the elements.
 * It is also possible to use the entire data model as view model.
 * 
 * @author Christoph Seidl
 *
 * @param <DM> The type of the data model.
 * @param <VM> The type of the view model.
 */
public abstract class ViewModelBasedGraphicalEditor<DM, VM> extends BaseGraphicalEditor {
	private DM dataModel;
	private VM viewModel;
	
	protected void updateViewModel() {
		viewModel = calculateViewModelFromDataModel(dataModel);
		
		doUpdateViewModel();
		
		GraphicalViewer viewer = getGraphicalViewer();
		
		if (viewer != null) {
			viewer.setContents(viewModel);
		}
	}
	
	protected void doUpdateViewModel() {
	}
	
	protected abstract VM calculateViewModelFromDataModel(DM dataModel);

	@Override
	protected void initializeGraphicalViewer() {
		updateViewModel();
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		DM dataModel = loadDataModel();
		setDataModel(dataModel);
	}
	
	protected abstract DM loadDataModel();
	
	protected void setDataModel(DM dataModel) {
		this.dataModel = dataModel;
		
		updateViewModel();
	}
	
	protected DM getDataModel() {
		return dataModel;
	}
	
	protected VM getViewModel() {
		return viewModel;
	}
}
