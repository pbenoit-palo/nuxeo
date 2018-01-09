/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.lib.stream.tests.codec;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import org.junit.Test;
import org.nuxeo.lib.stream.codec.AvroBinaryCodec;
import org.nuxeo.lib.stream.codec.AvroJsonCodec;
import org.nuxeo.lib.stream.codec.AvroMessageCodec;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.codec.SerializableCodec;
import org.nuxeo.lib.stream.computation.Record;

/**
 * @since 10.1
 */
public class TestCodec {

    @Test
    public void testExternalizable() throws Exception {
        Record src = getRecord();
        Codec<Record> codec = new SerializableCodec<>();
        testCodec(src, codec);
    }

    @Test
    public void testMessageAvro() throws Exception {
        Record src = getRecord();
        Codec<Record> codec = new AvroMessageCodec<>(Record.class);
        testCodec(src, codec);
    }

    @Test
    public void testRawMessageAvro() throws Exception {
        Record src = getRecord();
        Codec<Record> codec = new AvroBinaryCodec<>(Record.class);
        testCodec(src, codec);
    }

    @Test
    public void testJsonAvro() throws Exception {
        Record src = getRecord();
        Codec<Record> codec = new AvroJsonCodec<>(Record.class);
        testCodec(src, codec);
    }

    protected void testCodec(Record src, Codec<Record> codec) {
        byte[] data = codec.encode(src);
        Record dest = codec.decode(data);
        assertEquals(src, dest);
        byte[] data2 = codec.encode(dest);
        Record dest2 = codec.decode(data2);
        assertEquals(src, dest2);
        assertEquals(String.format("%s\n%s", overview(data), overview(data2)), data.length, data2.length);
        System.out.println(String.format("Codec: %s, size: %d", codec.getClass().getSimpleName(), data.length));
    }

    protected Record getRecord() throws UnsupportedEncodingException {
        Record src = Record.of("key", "value".getBytes("UTF-8"));
        src.setFlags(EnumSet.of(Record.Flag.COMMIT, Record.Flag.TRACE));
        return src;
    }

    protected String overview(byte[] data) {
        String overview;
        try {
            overview = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            overview = "unsupported encoding";
        }
        return overview;
    }

}
