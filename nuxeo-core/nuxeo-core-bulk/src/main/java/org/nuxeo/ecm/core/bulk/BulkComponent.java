/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     pierre
 */
package org.nuxeo.ecm.core.bulk;

import java.util.LinkedList;
import java.util.Queue;

import org.nuxeo.ecm.core.documentset.DocumentSetService;
import org.nuxeo.ecm.core.documentset.DocumentSetServiceDescriptor;
import org.nuxeo.runtime.RuntimeServiceException;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * The bulk component.
 *
 * @since 10.2
 */
public class BulkComponent extends DefaultComponent {

    public static final String SET_XP = "set";

    protected Queue<DocumentSetServiceDescriptor> documentSetServiceResgistry = new LinkedList<>();

    protected DocumentSetService documentSetService;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(documentSetService.getClass())) {
            return (T) documentSetService;
        }
        return null;
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (SET_XP.equals(extensionPoint)) {
            documentSetServiceResgistry.add((DocumentSetServiceDescriptor) contribution);
        } else {
            throw new RuntimeServiceException("Unknown extension point: " + extensionPoint);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(ComponentContext context) {
        try {
            DocumentSetServiceDescriptor last = documentSetServiceResgistry.peek();
            Class<? extends DocumentSetService> clazz = (Class<? extends DocumentSetService>) Class.forName(last.clazz);
            documentSetService = clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeServiceException(e);
        }
    }

    @Override
    public void stop(ComponentContext context) throws InterruptedException {
        documentSetService = null;
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (SET_XP.equals(extensionPoint)) {
            documentSetServiceResgistry.remove(contribution);
        } else {
            throw new RuntimeServiceException("Unknown extension point: " + extensionPoint);
        }
    }
}
