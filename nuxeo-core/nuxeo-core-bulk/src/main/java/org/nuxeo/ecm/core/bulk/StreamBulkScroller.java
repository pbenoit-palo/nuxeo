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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.SCROLLED_DOCUMENT_COUNT;
import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.SET_STREAM_NAME;
import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.STATE;
import static org.nuxeo.ecm.core.bulk.BulkStatus.State.BUILDING;
import static org.nuxeo.ecm.core.bulk.BulkStatus.State.COMPLETED;
import static org.nuxeo.ecm.core.bulk.BulkStatus.State.SCHEDULED;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.ScrollResult;
import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;
import org.nuxeo.runtime.stream.StreamProcessorTopology;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Computation that consumes a {@link BulkCommand} and produce document ids. This scroller takes a query to execute on
 * DB (by scrolling) and then produce document id to the appropriate stream.
 *
 * @since 10.2
 */
public class StreamBulkScroller implements StreamProcessorTopology {

    private static final Log log = LogFactory.getLog(StreamBulkScroller.class);

    public static final String COMPUTATION_NAME = "bulkDocumentScroller";

    public static final String SCROLL_BATCH_SIZE_OPT = "scrollBatchSize";

    public static final String SCROLL_KEEP_ALIVE_SECONDS_OPT = "scrollKeepAlive";

    public static final int DEFAULT_SCROLL_BATCH_SIZE = 100;

    public static final int DEFAULT_SCROLL_KEEPALIVE_SECONDS = 60;

    @Override
    public Topology getTopology(Map<String, String> options) {
        // retrieve options
        int scrollBatchSize = getOptionAsInteger(options, SCROLL_BATCH_SIZE_OPT, DEFAULT_SCROLL_BATCH_SIZE);
        int scrollKeepAliveSeconds = getOptionAsInteger(options, SCROLL_KEEP_ALIVE_SECONDS_OPT,
                DEFAULT_SCROLL_KEEPALIVE_SECONDS);
        // retrieve bulk operations to deduce output streams
        BulkAdminService service = Framework.getService(BulkAdminService.class);
        String kvStore = service.getKeyValueStore();
        List<String> operations = service.getOperations();
        List<String> mapping = new ArrayList<>();
        mapping.add("i1:" + SET_STREAM_NAME);
        int i = 1;
        for (String operation : operations) {
            mapping.add(String.format("o%s:%s", i, operation));
            i++;
        }
        return Topology.builder()
                       .addComputation( //
                               () -> new BulkDocumentScrollerComputation(COMPUTATION_NAME, operations.size(), kvStore,
                                       scrollBatchSize, scrollKeepAliveSeconds), //
                               mapping)
                       .build();
    }

    public static class BulkDocumentScrollerComputation extends AbstractComputation {

        protected final String kvStoreName;

        protected final int scrollBatchSize;

        protected final int scrollKeepAliveSeconds;

        /** Lazy initialized. */
        protected KeyValueStore kvStore;

        public BulkDocumentScrollerComputation(String name, int nbOutputStreams, String kvStoreName,
                int scrollBatchSize, int scrollKeepAliveSeconds) {
            super(name, 1, nbOutputStreams);
            this.kvStoreName = kvStoreName;
            this.scrollBatchSize = scrollBatchSize;
            this.scrollKeepAliveSeconds = scrollKeepAliveSeconds;
        }

        @Override
        public void processRecord(ComputationContext context, String inputStreamName, Record record) {
            TransactionHelper.runInTransaction(() -> processRecord(context, record));
        }

        protected void processRecord(ComputationContext context, Record record) {
            KeyValueStore kvStore = getKeyValueStore();
            try {
                BulkCommand command = getBulkCommandJson(record.data);
                if (!kvStore.compareAndSet(record.key + STATE, SCHEDULED.toString(), BUILDING.toString())) {
                    log.error("Discard record: " + record + " because it's already building");
                    return;
                }
                try (CloseableCoreSession session = CoreInstance.openCoreSession(command.getRepository(),
                        command.getUsername())) {
                    // scroll documents
                    ScrollResult<String> scroll = session.scroll(command.getQuery(), scrollBatchSize,
                            scrollKeepAliveSeconds);
                    long documentCount = 0;
                    while (scroll.hasResults()) {
                        List<String> docIds = scroll.getResults();
                        // send these ids as keys to the appropriate stream
                        // key will be bulkOperationId/docId
                        // value/data is a BulkCommand serialized as JSON
                        docIds.forEach(docId -> context.produceRecord(command.getOperation(), record.key + '/' + docId,
                                record.data));
                        documentCount += docIds.size();
                        context.askForCheckpoint();
                        // next batch
                        scroll = session.scroll(scroll.getScrollId());
                        TransactionHelper.commitOrRollbackTransaction();
                        TransactionHelper.startTransaction();
                    }
                    kvStore.put(record.key + STATE, COMPLETED.toString());
                    kvStore.put(record.key + SCROLLED_DOCUMENT_COUNT, documentCount);
                }
            } catch (NuxeoException e) {
                log.error("Discard invalid record: " + record, e);
            }
        }

        protected BulkCommand getBulkCommandJson(byte[] data) {
            String json = "";
            try {
                json = new String(data, UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, BulkCommand.class);
            } catch (UnsupportedEncodingException e) {
                throw new NuxeoException("Discard bulk command, invalid byte array", e);
            } catch (IOException e) {
                throw new NuxeoException("Invalid json bulkCommand=" + json, e);
            }
        }

        protected KeyValueStore getKeyValueStore() {
            if (kvStore == null) {
                kvStore = Framework.getService(KeyValueService.class).getKeyValueStore(kvStoreName);
            }
            return kvStore;
        }
    }

    // TODO copied from StreamAuditWriter - where can we put that ?
    protected int getOptionAsInteger(Map<String, String> options, String option, int defaultValue) {
        String value = options.get(option);
        return value == null ? defaultValue : Integer.valueOf(value);
    }
}
