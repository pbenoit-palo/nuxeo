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
package org.nuxeo.runtime.stream.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

import java.util.EnumSet;

/**
 * @since 9.3
 */
@RunWith(FeaturesRunner.class)
@Features(RuntimeFeature.class)
@Deploy("org.nuxeo.runtime.stream")
@LocalDeploy("org.nuxeo.runtime.stream:test-codec-contrib.xml")
public class TestCodecService {

    @Test
    public void testService() throws Exception {
        CodecService service = Framework.getService(CodecService.class);
        assertNotNull(service);
    }

    @Test
    public void testCodec() throws Exception {
        CodecService service = Framework.getService(CodecService.class);
        Record record = Record.of("key", "value".getBytes("UTF-8"));
        record.setFlags(EnumSet.of(Record.Flag.COMMIT, Record.Flag.POISON_PILL));

        Codec<Record> codec = service.getCodec("default", Record.class);
        checkCodec(record, codec);
        codec = service.getCodec("avro", Record.class);
        checkCodec(record, codec);
        codec = service.getCodec("avroBinary", Record.class);
        checkCodec(record, codec);
        codec = service.getCodec("avroJson", Record.class);
        checkCodec(record, codec);

        codec = service.getCodec("default", Record.class);
        checkCodec(record, codec);
    }

    private void checkCodec(Record src, Codec<Record> codec) {
        assertNotNull(codec);
        byte[] data = codec.encode(src);
        System.out.println("len : " + data.length);
        Record dest = codec.decode(data);
        assertEquals(src, dest);
    }
}
