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
package org.universAAL.middleware.brokers.control;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class WaitForResponse<T> {

    private int timeout;
    private Object[] responses;
    private long endAt;
    private int idx;

    public WaitForResponse(int maxresponses, int timeout) {
        this.endAt = System.currentTimeMillis() + timeout;
        this.timeout = timeout;
        this.responses = new Object[maxresponses < 0 ? 1 : maxresponses];
        this.idx = 0;
    }

    public T getFirstReponse() {
        synchronized (responses) {
            while (true) {
                if (idx > 0) {
                    // We have got an answer
                    return (T) responses[0];
                }
                long nextStop = endAt - System.currentTimeMillis();
                if (nextStop <= 0) {
                    // Timeout fired
                    return (T) responses[0];
                }
                try {
                    responses.wait(nextStop);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public List<T> getReponses() {
        final ArrayList<T> list = new ArrayList<T>();
        final int n;
        synchronized (responses) {
            while (true) {
                if (responses.length == idx) {
                    // We have got all the requested answers
                    break;
                }
                long nextStop = endAt - System.currentTimeMillis();
                if (nextStop <= 0) {
                    // Timeout fired
                    break;
                }
                try {
                    wait(nextStop);
                } catch (InterruptedException e) {
                }
            }
            n = idx;
        }
        for (int i = 0; i < n; i++) {
            list.add((T) responses[i]);
        }
        return list;
    }

    public void addResponse(T msg) {
        synchronized (responses) {
            if (idx >= responses.length) {
                // TODO Log me
                return;
            }
            responses[idx] = msg;
            idx++;
            responses.notify();
        }
    }

}
