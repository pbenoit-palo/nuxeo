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
 *
 */

package org.nuxeo.ecm.annotation;

import org.nuxeo.ecm.core.api.CoreSession;

import java.util.List;

/**
 * Annotation API to perform CRUD operations on annotations.
 *
 * @since 10.1
 */
public interface AnnotationService {

    /**
     * Creates an annotation on a document.
     *
     * @param session the core session
     * @param annotation the annotation to create
     * @return the created annotation
     */
    Annotation createAnnotation(CoreSession session, Annotation annotation);

    /**
     * Gets an annotation.
     *
     * @param session the core session
     * @param annotationId the annotation id
     * @return the annotation, or {@code null} if the annotation with the given id was not found
     */
    Annotation getAnnotation(CoreSession session, String annotationId);

    /**
     * Gets all annotations for a document.
     *
     * @param session the core session
     * @param documentId the annotated document id
     * @param xpath the annotated blob xpath in the document
     * @return the list of annotations, or an empty list if no annotation is found
     */
    List<Annotation> getAnnotations(CoreSession session, String documentId, String xpath);

    /**
     * Updates an annotation for a document.
     *
     * @param session the core session
     * @param annotation the annotation containing the modifications
     */
    void updateAnnotation(CoreSession session, Annotation annotation);

    /**
     * Deletes an annotation for a document.
     *
     * @param session the core session
     * @param annotationId the annotation id
     * @throws IllegalArgumentException if no annotation was found with the given id
     */
    void deleteAnnotation(CoreSession session, String annotationId)
            throws IllegalArgumentException;

}
