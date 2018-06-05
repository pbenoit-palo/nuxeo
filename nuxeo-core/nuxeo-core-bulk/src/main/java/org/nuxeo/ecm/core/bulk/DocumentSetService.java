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
 *       Kevin Leturc <kleturc@nuxeo.com>
 */

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

package org.nuxeo.ecm.core.bulk;

import java.util.UUID;

import org.nuxeo.ecm.core.bulk.BulkStatus;

/**
 * API to manage document sets.
 * 
 * @since 10.2
 */
public interface DocumentSetService {

    String DOCUMENTSET_LOG_CONFIG_PROP = "nuxeo.stream.documentset.log.config";

    String DEFAULT_DOCUMENTSET_LOG_CONFIG = "documentset";

    String DOCUMENTSET_STORE_ID = "documentset";

    /**
     * Creates a document set from the given NXQL query.
     *
     * @param repository the repository
     * @param nxql the query
     * @return the newly created document set
     */
    BulkStatus createDocumentSet(String repository, String nxql);

    /**
     * Gets document set from its id.
     * 
     * @param documentSetId the document set id
     * @return the documentSet
     */
    BulkStatus getDocumentSet(UUID documentSetId);
}
