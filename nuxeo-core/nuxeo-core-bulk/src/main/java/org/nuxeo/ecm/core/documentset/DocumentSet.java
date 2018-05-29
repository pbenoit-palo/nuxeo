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

import java.io.Serializable;
import java.util.Calendar;

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
    enum State {
        SCHEDULED, BUILDING, COMPLETED
    }

    protected String id;

    protected State state;

    protected String repository;

    protected Calendar creationDate;

    protected long lowerOffset;

    protected long upperOffset;

    /**
     * Gets document set id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets document set id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
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
    public Calendar getCreationDate() {
        return creationDate;
    }

    /**
     * Sets document set creation date.
     * 
     * @param creationDate the creation date
     */
    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets document set lower offset in the stream.
     * 
     * @return the lower offset
     */
    public long getLowerOffset() {
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
    public long getUpperOffset() {
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
