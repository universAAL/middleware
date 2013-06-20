/*

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.brokers.message.control;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.junit.Test;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class ControlMessageTest {

    @Test
    public void testControlMessageAttributes() throws Exception {
        ControlMessage original = new ControlMessage(null,
                Arrays.asList(new String[] { "ciao", "mio" }));
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageAttributesEscaping() throws Exception {
        {
            ControlMessage original = new ControlMessage(null,
                    Arrays.asList(new String[] { "{ciao", "mio" }));
            String txt = original.toString();
            ControlMessage parsed = ControlMessage.unmarshall(txt);
            assertEquals(txt, parsed.toString());
        }
        {
            ControlMessage original = new ControlMessage(
                    null,
                    Arrays.asList(new String[] { "\"ci/a\n\r\u9922o", "\\mio}" }));
            String txt = original.toString();
            ControlMessage parsed = ControlMessage.unmarshall(txt);
            assertEquals(txt, parsed.toString());
        }

    }

    @Test
    public void testControlMessageGetPeers() throws Exception {
        HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
        filter.put("OS", "android");
        filter.put("system", null);
        filter.put("sysversion", 3);
        ControlMessage original = new ControlMessage(null, filter);
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageGetPeersEscaping() throws Exception {
        HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
        filter.put("OS", "{}\\}\\{");
        filter.put("system", "\"ci/a\n\r\u9922o}");
        ControlMessage original = new ControlMessage(null, filter);
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageAttibuteValues() throws Exception {
        HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
        filter.put("OS", "{}\\}\\{");
        filter.put("system", "\"ci/a\n\r\u9922o}");
        ControlMessage original = new ControlMessage(null, UUID.randomUUID()
                .toString(), filter);
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageMatchingPeerResponse() throws Exception {
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("OS", "{}\\}\\{");
        map.put("system", "\"ci/a\n\r\u9922o}");
        ControlMessage original = new ControlMessage(null, UUID.randomUUID()
                .toString(), map, true);
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageMatchingPeerResponseFalse() throws Exception {
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("OS", "{}\\}\\{");
        map.put("system", "\"ci/a\n\r\u9922o}");
        ControlMessage original = new ControlMessage(null, UUID.randomUUID()
                .toString(), map, false);
        String txt = original.toString();
        ControlMessage parsed = ControlMessage.unmarshall(txt);
        assertEquals(txt, parsed.toString());
    }
}
