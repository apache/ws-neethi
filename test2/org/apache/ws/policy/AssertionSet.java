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

import java.util.List;
import java.util.Vector;

import org.apache.ws.policy.*;

/**
 * <p>
 * Class to represent a collection of PermValues.
 * 
 * @version 1.0.0
 */
public class AssertionSet {
  private Vector _values = new Vector();

  public void clear() {
    _values.clear();
  }

  public boolean isComplete() {
    boolean outcome = true;

    for (int i = 0; i < _values.size(); i++) {
      AssertionPerm perm = (AssertionPerm) _values.elementAt(i);
      if (perm.isChecked() == false) {
        outcome = false;
        break;
      }
    }
    return outcome;
  }

  public void add(Assertion perm[]) {
    AssertionPerm permValue = new AssertionPerm(perm);
    _values.add(permValue);
  }

  public boolean contains(List alternative) {
    boolean outcome = false;

    for (int i = 0; i < _values.size(); i++) {
      AssertionPerm permValue = (AssertionPerm) _values.elementAt(i);
      // We allow duplicates, so this may well be a repeat
      if (permValue.matches(alternative)) {
        outcome = true;
        permValue.setChecked(true);
        break;
      }
    }
    return outcome;
  }

  public boolean contains(Assertion[] array) {
    boolean outcome = false;

    for (int i = 0; i < _values.size(); i++) {
      AssertionPerm permValue = (AssertionPerm) _values.elementAt(i);
      if (permValue.matches(array)) {
        outcome = true;
        permValue.setChecked(true);
        break;
      }
    }
    return outcome;
  }

  // Special case where in fact you just want to match a single element.
  public boolean contains(Assertion value) {
    Assertion array[] = { value };
    return contains(array);
  }

}
