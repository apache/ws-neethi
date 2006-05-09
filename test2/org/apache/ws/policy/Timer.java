/*
 * Copyright 2004,2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.policy;


public class Timer {
  private long _start = 0;
  private long _stop = 0;
  private double _elapse = 0.000;
  private String _eventClass = "";
  private String _eventContext = "";

  public Timer(String eventClass) {
    _eventClass = eventClass;
  }

  public void start(String currentContext) {
    _eventContext = currentContext;
    _start = System.currentTimeMillis();
  }

  public String stop() {
    _stop = System.currentTimeMillis();
    _elapse = (_stop - _start) / 1000.000;
    String stopString = _eventClass + "(" + _eventContext + ")" + ":[" + _elapse + "]";
    System.out.println(stopString);
    return stopString;
  }
}
