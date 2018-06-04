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
package org.nuxeo.ecm.core.bulk.documentset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.nuxeo.ecm.core.bulk.documentset.DocumentSet.State.BUILDING;
import static org.nuxeo.ecm.core.bulk.documentset.DocumentSet.State.COMPLETED;
import static org.nuxeo.ecm.core.bulk.documentset.DocumentSet.State.SCHEDULED;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.ScrollResult;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;
import org.nuxeo.runtime.stream.StreamService;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Basic implementation of {@link DocumentSetService}.
 *
 * @since 10.2
 */
public class DocumentSetServiceImpl implements DocumentSetService {

    protected final static String REPOSITORY = ":repository";

    protected final static String QUERY = ":query";

    protected final static String CREATION_DATE = ":creationDate";

    protected final static String STATE = ":state";

    protected final static String LOWER_OFFSET = ":lowerOffset";

    protected final static String UPPER_OFFSET = ":upperOffset";

    protected DocumentSetServiceDescriptor descriptor;

    protected KeyValueStore kvStore;

    protected WorkManager workManager;

    public DocumentSetServiceImpl(DocumentSetServiceDescriptor descriptor) {
        this.descriptor = descriptor;
        kvStore = Framework.getService(KeyValueService.class).getKeyValueStore(descriptor.kvStore);
        workManager = Framework.getService(WorkManager.class);
    }

    @Override
    public DocumentSet createDocumentSet(String repository, String nxql) {


        UUID documentSetId = UUID.randomUUID();

        DocumentSet set = new DocumentSet();
        set.setUUID(documentSetId);
        set.setState(SCHEDULED);
        set.setCreationDate(ZonedDateTime.now());
        set.setRepository(repository);
        set.setQuery(nxql);

        // Store the documentSet metadata in the key/value store
        kvStore.put(documentSetId + STATE, set.getState().toString());
        kvStore.put(documentSetId + CREATION_DATE, set.getCreationDate().toString());
        kvStore.put(documentSetId + REPOSITORY, repository);
        kvStore.put(documentSetId + QUERY, nxql);

        Work work = new DocumentSetCreationWork(descriptor, set);
        Framework.getService(WorkManager.class).schedule(work);

        return set;
    }

    @Override
    public DocumentSet getDocumentSet(UUID documentSetId) {
        return null;
    }

    public static class DocumentSetCreationWork extends AbstractWork {

        protected static final int BATCH_SIZE = 100;

        protected static final int SCROLL_KEEPALIVE_SECONDS = 60;

        protected static final String STREAM_NAME = "documentSet";

        protected DocumentSetServiceDescriptor descriptor;

        protected DocumentSet set;

        protected transient LogManager logManager;

        protected transient KeyValueStore kvStore;

        public DocumentSetCreationWork(DocumentSetServiceDescriptor descriptor, DocumentSet set) {
            this.repositoryName = set.getRepository();
            this.descriptor = descriptor;
            this.set = set;
        }

        @Override
        public String getTitle() {
            return "DocumentSet creation work";
        }

        @Override
        public void work() {

            UUID documentSetId = set.getUUID();
            // Check and update documentSet state in the key/value store
            if (!getKeyValueStore().compareAndSet(documentSetId + STATE, SCHEDULED.toString(), BUILDING.toString())) {
                throw new NuxeoException(String.format("The documentSet %s is already building.", documentSetId));
            }

            openSystemSession();
            setProgress(Progress.PROGRESS_INDETERMINATE);
            setStatus("Creating documentSet");

            String nxql = set.getQuery();
            ScrollResult<String> scroll = session.scroll(nxql, BATCH_SIZE, SCROLL_KEEPALIVE_SECONDS);
            long documentCount = 0;
            while (scroll.hasResults()) {
                List<String> docIds = scroll.getResults();
                // send these ids to the stream
                if (!docIds.isEmpty()) {
                    LogAppender<Record> appender = getLogManager().getAppender(STREAM_NAME);
                    docIds.forEach(docId -> writeDocId(appender, set, docId));
                }
                documentCount += docIds.size();
                setProgress(new Progress(documentCount, -1));
                // next batch
                scroll = session.scroll(scroll.getScrollId());
                TransactionHelper.commitOrRollbackTransaction();
                TransactionHelper.startTransaction();
            }
            setProgress(new Progress(documentCount, documentCount));
            getKeyValueStore().put(documentSetId + UPPER_OFFSET, set.getUpperOffset());
            getKeyValueStore().put(documentSetId + STATE, COMPLETED.toString());
            setStatus("Done");

        }

        protected void writeDocId(LogAppender<Record> appender, DocumentSet set, String documentId) {
            String documentSetId = set.getUUID().toString();
            LogOffset offset = appender.append(documentSetId, new Record(documentId, documentId.getBytes(UTF_8)));
            if (set.getLowerOffset() == null) {
                set.setLowerOffset(offset.offset());
                getKeyValueStore().put(documentSetId + LOWER_OFFSET, offset.offset());
            }
            set.setUpperOffset(offset.offset());
        }

        protected LogManager getLogManager() {
            if (logManager == null) {
                logManager = Framework.getService(StreamService.class).getLogManager(descriptor.logManager);
            }
            return logManager;
        }

        protected KeyValueStore getKeyValueStore() {
            if (kvStore == null) {
                kvStore = Framework.getService(KeyValueService.class).getKeyValueStore(descriptor.kvStore);
            }
            return kvStore;
        }
    }
}
