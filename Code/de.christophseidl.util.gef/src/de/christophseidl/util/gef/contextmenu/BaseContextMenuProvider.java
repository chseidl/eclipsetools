package de.christophseidl.util.gef.contextmenu;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

import de.christophseidl.util.gef.action.BaseAction;

public abstract class BaseContextMenuProvider extends ContextMenuProvider {
    private ActionRegistry actionRegistry;
    
    public BaseContextMenuProvider(EditPartViewer viewer, ActionRegistry actionRegistry) {
        super(viewer);
        this.actionRegistry = actionRegistry;
    }
 
    @Override
    public void buildContextMenu(IMenuManager menuManager) {
        GEFActionConstants.addStandardActionGroups(menuManager);
        
        registerContextMenuEntries(menuManager, actionRegistry);
        
        registerListeners(menuManager);
        updateMenuManagerEnabledState(menuManager);
    }
    
    protected abstract void registerContextMenuEntries(IMenuManager menuManager, ActionRegistry actionRegistry);
    
    private void registerListeners(IMenuManager menuManager) {
        menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menuManager) {
				updateMenuManagerEnabledState(menuManager);
			}
		});
    }
    
    private void updateMenuManagerEnabledState(IMenuManager menuManager) {
		IContributionItem[] items = menuManager.getItems();
		
		if (items != null) {
			for (IContributionItem item : items) {
				if (item instanceof ActionContributionItem) {
					ActionContributionItem actionContributionItem = (ActionContributionItem) item;
					IAction rawAction = actionContributionItem.getAction();
					
					if (rawAction instanceof BaseAction) {
						BaseAction action = (BaseAction) rawAction;
						action.updateEnabledState();
					}
				}
			}
		}
    }
}
