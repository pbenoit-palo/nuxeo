/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bdelbosc
 */
package org.nuxeo.lib.stream.log;

import java.io.Externalizable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.nuxeo.lib.stream.codec.Codec;

/**
 * Manage Log and give access to Appenders and Tailers. Closing the LogManager will also close all its appenders and
 * tailers.
 *
 * @since 9.3
 */
public interface LogManager extends AutoCloseable {

    /**
     * Returns {@code true} if a Log with this {@code name} exists.
     */
    boolean exists(String name);

    /**
     * Creates a new Log with {@code size} partitions if the Log does not exists. Returns true it the Log has been
     * created.
     */
    boolean createIfNotExists(String name, int size);

    /**
     * Try to delete a Log. Returns true if successfully deleted, might not be possible depending on the implementation.
     */
    boolean delete(String name);

    /**
     * Returns the number of partition of a Log.
     *
     * @since 10.2
     */
    int size(String name);

    /**
     * Get an appender for the Log named {@code name}, use {@code codec} to encode records. An appender is thread safe.
     *
     * @since 10.2
     */
    <M extends Externalizable> LogAppender<M> getAppender(String name, Codec<M> codec);

    /**
     * Get an appender for the Log named {@code name}. Encode message using Java Externalizable API. An appender is
     * thread safe.
     */
    default <M extends Externalizable> LogAppender<M> getAppender(String name) {
        return getAppender(name, null);
    }

    /**
     * Create a tailer for a consumer {@code group} and assign multiple {@code partitions}. Note that {@code partitions}
     * can be from different Logs. Legacy codec used to decode records. A tailer is NOT thread safe.
     *
     * @since 10.2
     */
    <M extends Externalizable> LogTailer<M> createTailer(String group, Collection<LogPartition> partitions,
            Codec<M> codec);

    /**
     * Create a tailer for a consumer {@code group} and assign multiple {@code partitions}. Note that {@code partitions}
     * can be from different Logs. Legacy codec used to decode records. A tailer is NOT thread safe.
     */
    default <M extends Externalizable> LogTailer<M> createTailer(String group, Collection<LogPartition> partitions) {
        return createTailer(group, partitions, null);
    }

    /**
     * Create a tailer for a consumer {@code group} and assign a single {@code partition}. Legacy codec used to decode
     * records. A tailer is NOT thread safe.
     */
    default <M extends Externalizable> LogTailer<M> createTailer(String group, LogPartition partition) {
        return createTailer(group, partition, null);
    }

    /**
     * Create a tailer for a consumer {@code group} and assign all {@code partitions} of the Log. Legacy codec used to
     * decode records. A tailer is NOT thread safe.
     */
    default <M extends Externalizable> LogTailer<M> createTailer(String group, String name) {
        return createTailer(group, name, null);
    }

    /**
     * Create a tailer for a consumer {@code group} and assign a single {@code partition}. Use an explicit codec to
     * decode records. A tailer is NOT thread safe.
     *
     * @since 10.2
     */
    default <M extends Externalizable> LogTailer<M> createTailer(String group, LogPartition partition, Codec<M> codec) {
        return createTailer(group, Collections.singletonList(partition), codec);
    }

    /**
     * Create a tailer for a consumer {@code group} and assign all {@code partitions} of the Log. Use an explicit codec
     * to decode records. A tailer is NOT thread safe.
     *
     * @since 10.2
     */
    default <M extends Externalizable> LogTailer<M> createTailer(String group, String name, Codec<M> codec) {
        return createTailer(group,
                IntStream.range(0, size(name)).boxed().map(partition -> new LogPartition(name, partition)).collect(
                        Collectors.toList()),
                codec);
    }

    /**
     * Returns {@code true} if the Log {@link #subscribe} method is supported.
     */
    boolean supportSubscribe();

    /**
     * Create a tailer for a consumer {@code group} and subscribe to multiple Logs. The partitions assignment is done
     * dynamically depending on the number of subscribers. The partitions can change during tailers life, this is called
     * a rebalancing. A listener can be used to be notified on assignment changes. Use an explicit codec to decode
     * records.
     * <p/>
     * A tailer is NOT thread safe.
     * <p/>
     * You should not mix {@link #createTailer} and {@code subscribe} usage using the same {@code group}.
     */
    <M extends Externalizable> LogTailer<M> subscribe(String group, Collection<String> names,
            RebalanceListener listener, Codec<M> codec);

    default <M extends Externalizable> LogTailer<M> subscribe(String group, Collection<String> names,
            RebalanceListener listener) {
        return subscribe(group, names, listener, null);
    }

    /**
     * Returns the lag between consumer {@code group} and the producers for each partition. The result list is ordered,
     * for instance index 0 is lag for partition 0.
     */
    List<LogLag> getLagPerPartition(String name, String group);

    /**
     * Returns the lag between consumer {@code group} and producers for a Log.
     */
    default LogLag getLag(String name, String group) {
        return LogLag.of(getLagPerPartition(name, group));
    }

    /**
     * Returns the lag with latency. Timestamps used to compute the latencies are extracted from the records. This
     * requires to read one record per partition so it costs more than {@link #getLagPerPartition(String, String)}.
     * <br/>
     * Two functions need to be provided to extract the timestamp and a key from a record.
     *
     * @since 10.2
     */
    <M extends Externalizable> List<Latency> getLatencyPerPartition(String name, String group, Codec<M> codec,
            Function<M, Long> timestampExtractor, Function<M, String> keyExtractor);

    /**
     * Returns the lag with latency. Timestamps used to compute the latencies are extracted from the records. This
     * requires to read one record per partition so it costs more than {@link #getLagPerPartition(String, String)}.
     * <br/>
     * Two functions need to be provided to extract the timestamp and a key from a record.
     *
     * @since 10.1
     */
    default <M extends Externalizable> List<Latency> getLatencyPerPartition(String name, String group,
            Function<M, Long> timestampExtractor, Function<M, String> keyExtractor) {
        return getLatencyPerPartition(name, group, null, timestampExtractor, keyExtractor);
    }

    /**
     * Returns the latency between consumer {@code group} and producers for a Log.
     *
     * @since 10.1
     */
    default <M extends Externalizable> Latency getLatency(String name, String group,
            Function<M, Long> timestampExtractor, Function<M, String> keyExtractor) {
        return getLatency(name, group, null, timestampExtractor, keyExtractor);
    }

    /**
     * Returns the latency between consumer {@code group} and producers for a Log.
     *
     * @since 10.2
     */
    default <M extends Externalizable> Latency getLatency(String name, String group, Codec<M> codec,
            Function<M, Long> timestampExtractor, Function<M, String> keyExtractor) {
        return Latency.of(getLatencyPerPartition(name, group, codec, timestampExtractor, keyExtractor));
    }

    /**
     * Returns all the Log names.
     */
    List<String> listAll();

    /**
     * List the consumer groups for a Log.<br/>
     * Note that for Kafka it returns only consumers that use the subscribe API.
     */
    List<String> listConsumerGroups(String name);

    @Override
    void close();
}
