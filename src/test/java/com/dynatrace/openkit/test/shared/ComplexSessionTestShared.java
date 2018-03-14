/**
 * Copyright 2018 Dynatrace LLC
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
 */

package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ComplexSessionTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);

        RootAction actionOne = session.enterAction("ActionOne");

        actionOne.reportValue("IntegerValue", 45);
        actionOne.reportValue("DoubleValue", 9.2);
        actionOne.reportValue("String", "This is a string");

        actionOne.reportError("errorName", 22, "meaningful reason");
        actionOne.reportError("FATAL ERROR", 42, "valid reason");

        // create a child Action
        Action actionTwo = actionOne.enterAction("ActionTwo");
        actionTwo.reportEvent("EventOne");
        actionTwo.leaveAction();

        // simulate the tagged web request - we dont actually need to send it
        URL url;
        WebRequestTracer timing;
        try {
            url = new URL("http://mydomain/app/search.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            timing = actionOne.traceWebRequest(conn);            // tags the request
            timing.start();                            // starts the timing
            // no request is performed - but that's OK
            timing.stop();                            // stop the timing and generate the beacon signal
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        actionOne.leaveAction();

        session.end();

        openKit.shutdown();
    }

}
