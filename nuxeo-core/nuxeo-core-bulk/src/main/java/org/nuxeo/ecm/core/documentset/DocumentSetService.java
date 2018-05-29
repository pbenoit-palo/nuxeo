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
 *     Funsho David
 */

package org.nuxeo.ecm.core.documentset;

import org.nuxeo.ecm.core.documentset.DocumentSet.State;

/**
 * API to manage document sets.
 * 
 * @since 10.2
 */
public interface DocumentSetService {

    /**
     * Creates a document set from the given NXQL query.
     * 
     * @param nxql the query
     * @return the newly created document set id
     */
    String createDocumentSet(String nxql);

    /**
     * Gets document set state from its id. Possible values are SCHEDULED, BUILDING or COMPLETED.
     * 
     * @param documentSetId the document set id
     * @return the state
     */
    State getDocumentSetState(String documentSetId);
}
