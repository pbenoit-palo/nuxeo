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
package org.nuxeo.ecm.core.documentset;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;

/**
 * @since 10.2
 */
public class DocumentSetRepositoryInit extends DefaultRepositoryInit {

    @Override
    public void populate(CoreSession session) {
        super.populate(session);
        for (int i = 0; i < 10; i++) {
            createDocument(session, "document " + 1);
        }
    }

    private void createDocument(CoreSession session, String name) {
        session.createDocument(session.createDocumentModel("/default-domain/workspaces/test/", name, "ComplexDoc"));
    }

}
