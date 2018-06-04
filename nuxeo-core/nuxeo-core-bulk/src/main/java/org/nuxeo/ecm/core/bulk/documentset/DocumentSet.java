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

package org.nuxeo.ecm.core.bulk.documentset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Document set object.
 * 
 * @since 10.2
 */
public class DocumentSet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Possible states of the document set.
     */
    public enum State {
        SCHEDULED, BUILDING, COMPLETED
    }

    protected UUID uuid;

    protected String query;

    protected State state;

    protected String repository;

    protected ZonedDateTime creationDate;

    protected Long lowerOffset;

    protected Long upperOffset;

    /**
     * Gets document set id.
     * 
     * @return the id
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets document set id.
     *
     * @param uuid the id
     */
    protected void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets document set NXQL query.
     *
     * @return the NXQL query
     */
    public String getQuery() {
        return query;
    }


    /**
     * Sets document set NXQL query.
     *
     * @param query the NXQL query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets document set state. Possible values are SCHEDULED, BUILDING or COMPLETED.
     * 
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Sets document set state.
     * 
     * @param state the state
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Gets document set repository.
     * 
     * @return the repository
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Sets document set repository.
     * 
     * @param repository the repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Gets document set creation date.
     *
     * @return the creation date
     */
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Gets document set creation date as a {@link Calendar}.
     * 
     * @return the creation date
     */
    public Calendar getCreationDateCal() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.from(creationDate.toInstant()));
        return cal;
    }

    /**
     * Sets document set creation date.
     *
     * @param creationDate the creation date
     */
    protected void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets document set lower offset in the stream.
     * 
     * @return the lower offset
     */
    public Long getLowerOffset() {
        return lowerOffset;
    }

    /**
     * Sets document set lower offset in the stream.
     * 
     * @param lowerOffset the lower offset
     */
    public void setLowerOffset(long lowerOffset) {
        this.lowerOffset = lowerOffset;
    }

    /**
     * Gets document set upper offset in the stream.
     *
     * @return the upper offset
     */
    public Long getUpperOffset() {
        return upperOffset;
    }

    /**
     * Sets document set upper offset in the stream.
     *
     * @param upperOffset the upper offset
     */
    public void setUpperOffset(long upperOffset) {
        this.upperOffset = upperOffset;
    }

}
