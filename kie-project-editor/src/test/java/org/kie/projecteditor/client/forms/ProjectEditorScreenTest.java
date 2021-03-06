/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.projecteditor.client.forms;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.junit.Before;
import org.junit.Test;
import org.kie.projecteditor.client.widgets.ListFormComboPanelView;
import org.kie.projecteditor.client.widgets.NamePopup;
import org.kie.projecteditor.client.widgets.PopupSetNameCommand;
import org.kie.projecteditor.shared.model.KBaseModel;
import org.kie.projecteditor.shared.model.KProjectModel;
import org.kie.projecteditor.shared.service.ProjectEditorService;
import org.mockito.ArgumentCaptor;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.workbench.widgets.menu.MenuBar;
import org.uberfire.client.workbench.widgets.menu.MenuItem;
import org.uberfire.client.workbench.widgets.menu.impl.DefaultMenuItemCommand;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ProjectEditorScreenTest {

    private Path path;
    private ListFormComboPanelView view;
    private MockProjectEditorServiceCaller projectEditorServiceCaller;
    private ProjectEditorScreen screen;
    private ListFormComboPanelView.Presenter presenter;
    private NamePopup nameNamePopup;
    private KBaseForm form;

    @Before
    public void setUp() throws Exception {
        path = mock(Path.class);
        view = mock(ListFormComboPanelView.class);
        projectEditorServiceCaller = new MockProjectEditorServiceCaller();

        nameNamePopup = mock(NamePopup.class);
        form = mock(KBaseForm.class);
        screen = new ProjectEditorScreen(projectEditorServiceCaller, form, nameNamePopup, view);
        presenter = screen;
    }

    @Test
    public void testShowEmptyModel() throws Exception {
        projectEditorServiceCaller.setUpModelForLoading(new KProjectModel());

        verify(view, never()).addItem(anyString());
    }

    @Test
    public void testShowModelWithSessions() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        kProjectModel.add(createKBaseConfiguration("First"));
        kProjectModel.add(createKBaseConfiguration("Second"));
        kProjectModel.add(createKBaseConfiguration("Third"));
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        verify(view).addItem("First");
        verify(view).addItem("Second");
        verify(view).addItem("Third");
        verify(view, times(3)).addItem(anyString());
    }

    @Test
    public void testSelectKBase() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        KBaseModel theOne = createKBaseConfiguration("TheOne");
        kProjectModel.add(theOne);
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        presenter.onSelect("TheOne");

        verify(form).setModel(theOne);
    }

    @Test
    public void testAddKBase() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        presenter.onAdd();

        ArgumentCaptor<PopupSetNameCommand> addKBaseCommandArgumentCaptor = ArgumentCaptor.forClass(PopupSetNameCommand.class);
        verify(nameNamePopup).show(addKBaseCommandArgumentCaptor.capture());
        addKBaseCommandArgumentCaptor.getValue().setName("TheOne");

        verify(nameNamePopup).setOldName(""); // Old name should be "" since there is no old name.
        assertNotNull(kProjectModel.get("TheOne"));
        verify(view).addItem("TheOne");
        verify(view).setSelected("TheOne");
        verify(form).setModel(kProjectModel.get("TheOne"));
    }

    @Test
    public void testRemoveKBase() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        kProjectModel.add(createKBaseConfiguration("RemoveMe"));
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        presenter.onSelect("RemoveMe");

        presenter.onRemove();

        assertNull(kProjectModel.get("RemoveMe"));
        verify(view).remove("RemoveMe");
    }

    @Test
    public void testRename() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        kProjectModel.add(createKBaseConfiguration("RenameMe"));
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        presenter.onSelect("RenameMe");

        presenter.onRename();

        ArgumentCaptor<PopupSetNameCommand> addKBaseCommandArgumentCaptor = ArgumentCaptor.forClass(PopupSetNameCommand.class);
        verify(nameNamePopup).show(addKBaseCommandArgumentCaptor.capture());
        addKBaseCommandArgumentCaptor.getValue().setName("NewName");

        verify(nameNamePopup).setOldName("RenameMe");
        assertNull(kProjectModel.get("RenameMe"));
        assertNotNull(kProjectModel.get("NewName"));
    }

    @Test
    public void testRemoveKBaseNoItemSelected() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        kProjectModel.add(createKBaseConfiguration("CantRemoveMe"));
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        presenter.onRemove();

        verify(view).showPleaseSelectAnItem();

        assertNotNull(kProjectModel.get("CantRemoveMe"));
        verify(view, never()).remove("CantRemoveMe");
    }

    @Test
    public void testDoubleClickRemoveSecondTimeWithoutATarget() throws Exception {

        KProjectModel kProjectModel = new KProjectModel();
        kProjectModel.add(createKBaseConfiguration("RemoveMe"));
        kProjectModel.add(createKBaseConfiguration("CantRemoveMe"));
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        // Select one and remove.
        presenter.onSelect("RemoveMe");
        presenter.onRemove();

        // Click again, nothing is selected.
        presenter.onRemove();

        verify(view).showPleaseSelectAnItem();

        assertNotNull(kProjectModel.get("CantRemoveMe"));
        verify(view, never()).remove("CantRemoveMe");
    }

    @Test
    public void testSave() throws Exception {
        KProjectModel kProjectModel = new KProjectModel();
        projectEditorServiceCaller.setUpModelForLoading(kProjectModel);
        screen.init(path);

        MenuBar menuBar = screen.buildMenuBar();
        clickFirst(menuBar);

        assertEquals(kProjectModel, projectEditorServiceCaller.getSavedModel());
    }

    private void clickFirst(MenuBar menuBar) {
        for (MenuItem menuItem : menuBar.getItems()) {
            if (menuItem instanceof DefaultMenuItemCommand) {
                DefaultMenuItemCommand defaultMenuItemCommand = (DefaultMenuItemCommand) menuItem;
                defaultMenuItemCommand.getCommand().execute();
                break;
            }
        }
    }

    private KBaseModel createKBaseConfiguration(String name) {
        KBaseModel knowledgeBaseConfiguration = new KBaseModel();
        knowledgeBaseConfiguration.setName(name);
        return knowledgeBaseConfiguration;
    }

    class MockProjectEditorServiceCaller
            implements Caller<ProjectEditorService> {

        private final ProjectEditorService service;

        private KProjectModel savedModel;
        private KProjectModel modelForLoading;

        private RemoteCallback callback;

        MockProjectEditorServiceCaller() {

            service = new ProjectEditorService() {

                @Override
                public void save(KProjectModel model) {
                    callback.callback(null);
                    savedModel = model;
                }

                @Override
                public KProjectModel load(Path path) {
                    callback.callback(modelForLoading);
                    return modelForLoading;
                }
            };
        }

        public KProjectModel getSavedModel() {
            return savedModel;
        }

        @Override
        public ProjectEditorService call(RemoteCallback<?> callback) {
            this.callback = callback;
            return service;
        }

        @Override
        public ProjectEditorService call(RemoteCallback<?> callback, ErrorCallback errorCallback) {
            this.callback = callback;
            return service;
        }

        public void setUpModelForLoading(KProjectModel upModelForLoading) {
            this.modelForLoading = upModelForLoading;
        }
    }
}

