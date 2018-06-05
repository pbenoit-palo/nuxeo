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
 *       Kevin Leturc <kleturc@nuxeo.com>
 */
package org.nuxeo.ecm.core.bulk;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.nuxeo.ecm.core.bulk.BulkStatus.State.SCHEDULED;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;
import org.nuxeo.runtime.stream.StreamService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic implementation of {@link BulkService}.
 *
 * @since 10.2
 */
public class BulkServiceImpl implements BulkService {

    private static final Log log = LogFactory.getLog(BulkServiceImpl.class);

    protected static final String SET_STREAM_NAME = "documentSet";

    protected final static String REPOSITORY = ":repository";

    protected final static String QUERY = ":query";

    protected final static String CREATION_DATE = ":creationDate";

    protected final static String STATE = ":state";

    protected final static String SCROLLED_DOCUMENT_COUNT = ":scrolledDocumentCount";

    protected BulkServiceDescriptor descriptor;

    protected KeyValueStore kvStore;

    protected LogManager logManager;

    public BulkServiceImpl(BulkServiceDescriptor descriptor) {
        this.descriptor = descriptor;
        kvStore = Framework.getService(KeyValueService.class).getKeyValueStore(descriptor.kvStore);
        logManager = Framework.getService(StreamService.class).getLogManager(descriptor.logManager);
    }

    @Override
    public BulkStatus runOperation(BulkCommand command) {
        if (log.isDebugEnabled()) {
            log.debug("Run operation with command=" + command);
        }
        // fill command object
        if (isEmpty(command.getRepository())) {
            String repository = Framework.getService(RepositoryManager.class).getDefaultRepositoryName();
            command.withRepository(repository);
        }
        // create the operation id and status
        UUID bulkOperationId = UUID.randomUUID();

        BulkStatus status = new BulkStatus();
        status.setUUID(bulkOperationId);
        status.setState(SCHEDULED);
        status.setCreationDate(ZonedDateTime.now());
        status.setCommand(command);

        // store the bulk command and status in the key/value store
        kvStore.put(bulkOperationId + STATE, status.getState().toString());
        kvStore.put(bulkOperationId + CREATION_DATE, status.getCreationDate().toString());
        kvStore.put(bulkOperationId + REPOSITORY, command.getRepository());
        kvStore.put(bulkOperationId + QUERY, command.getQuery());

        try {
            // send it to nuxeo-stream
            String key = bulkOperationId.toString();
            byte[] value = new ObjectMapper().writeValueAsBytes(command);
            logManager.getAppender(SET_STREAM_NAME).append(key, new Record(key, value));
        } catch (JsonProcessingException e) {
            throw new NuxeoException("Unable to serialize the bulk command=" + command, e);
        }
        return status;
    }

    @Override
    public BulkStatus getStatus(UUID bulkOperationId) {
        return null;
    }

    @Override
    public String getKeyValueStore() {
        return descriptor.kvStore;
    }

    @Override
    public List<String> getOperations() {
        // TODO change that when doing bulk computation part
        return singletonList("count");
    }
}
