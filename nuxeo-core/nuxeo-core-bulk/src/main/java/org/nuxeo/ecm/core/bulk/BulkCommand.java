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

import java.io.Serializable;

import com.google.common.base.MoreObjects;

/**
 * A command to execute by {@link BulkService}.
 *
 * @since 10.2
 */
public class BulkCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String repository;

    /** The NXQL query to execute. */
    private String query;

    /** The Bulk operation to execute. */
    private String operation;

    public BulkCommand() {
    }

    public BulkCommand withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public BulkCommand withQuery(String query) {
        this.query = query;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public BulkCommand withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("repository", repository).add("query", query).toString()
                ;
    }
}
