/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     anechaev
 */
package org.nuxeo.runtime.codec;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.lib.stream.codec.Codec;

@SuppressWarnings("CanBeFinal")
@XObject("codec")
public class CodecDescriptor {
    @XNode("@name")
    protected String name;

    @XNode("@class")
    protected Class<Codec> klass;

    @XNodeMap(value = "option", key = "@name", type = HashMap.class, componentType = String.class)
    public Map<String, String> options = new HashMap<>();

    public String getName() {
        return name;
    }

    public Class<Codec> getKlass() {
        return klass;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public String getOption(String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return "CodecDescriptor{" + "klass=" + klass + ", options=" + options + '}';
    }

    public Codec getInstance() {
        try {
            Codec ret = getKlass().getDeclaredConstructor().newInstance();
            return ret;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Invalid class: " + getClass(), e);
        }
    }
}
