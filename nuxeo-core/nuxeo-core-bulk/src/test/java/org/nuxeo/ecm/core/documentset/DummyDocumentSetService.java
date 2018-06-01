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

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import org.nuxeo.ecm.core.documentset.DocumentSet.State;

/**
 * DummyDocumentSetService.
 *
 * @since 10.2
 */
public class DummyDocumentSetService implements DocumentSetService {

    protected Collection<String> known = new LinkedList<>();

    @Override
    public String createDocumentSet(String nxql) {
        String id = UUID.randomUUID().toString();
        known.add(id);
        return id;
    }

    @Override
    public State getDocumentSetState(String documentSetId) {
        if (known.contains(documentSetId)) {
            return State.BUILDING;
        }
        return null;
    }

}
