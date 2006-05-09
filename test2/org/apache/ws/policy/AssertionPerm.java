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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.ws.policy.*;

/**
 * <p>Class to represent an Assertion permutation.
 * @version 1.0.0 
 */
public class AssertionPerm {
  private Assertion _perm[] = null;
  private boolean _matched[] = null;
  private boolean _allChecked = false;

  public AssertionPerm(Assertion perm[]) {
    _perm = perm;
    int length = perm.length;
    _matched = new boolean[perm.length];
    for (int i = 0; i < _perm.length; i++) {
      _matched[i] = false;
    }
  }

  boolean isChecked() {
    return _allChecked;
  }

  public void setChecked(boolean checked) {
    _allChecked = checked;
  }

  public boolean matches(List alternative) {
    // Special case an empty Alternative
    if (_perm.length == 0) {
      if (alternative.size() == 0) {
        return true;
      }
    }
    boolean outcome = false;
    // Initialise the checked flag for each assertion 
    for (int i = 0; i < _perm.length; i++) {
      _matched[i] = false;
    }
    int numberOfAltAssertions = 0;
    for (int j = 0; j < alternative.size(); j++) {
      PrimitiveAssertion ass = (PrimitiveAssertion) alternative.get(j);
      numberOfAltAssertions++;
      for (int i = 0; i < _perm.length; i++) {
        // Only if it's not already been matched
        if (_matched[i] != true) {
          if (checkEquality((PrimitiveAssertion) ass, (PrimitiveAssertion) _perm[i]) == true) {
            _matched[i] = true;
            break;
          }
        }
      }
    }
    // If we don't even have the same number, it doesn't matter
    // how many have matched, it's not a full match.
    if (_perm.length == numberOfAltAssertions) {

      // Now check that all of the values in this permutation have
      // been met. If so, flag this permutation as being checked.
      for (int i = 0; i < _perm.length; i++) {
        if (_matched[i] == false) {
          break;
        }
        // If we got this far, it's all checked.
        int j = i + 1;
        if (j == _perm.length) {
          outcome = true;
        }
      }
    }
    return outcome;
  }

  public boolean matches(Assertion[] valueArray) {

    if (valueArray.length != _perm.length) {
      return false;
    }
    boolean outcome = false;
    for (int i = 0; i < _perm.length; i++) {
      _matched[i] = false;
    }

    for (int j = 0; j < valueArray.length; j++) {
      for (int i = 0; i < _perm.length; i++) {
        // Only if it's not already been matched
        if (_matched[i] != true) {
          if (checkEquality((PrimitiveAssertion) valueArray[j], (PrimitiveAssertion) _perm[i]) == true) {
            _matched[i] = true;
            break;
          }
        }
      }
    }

    // Now check that all of the values in this permutation have
    // been met. If so, flag this permutation as being checked.
    for (int i = 0; i < _perm.length; i++) {
      if (_matched[i] == false) {
        break;
      }
      // If we got this far, it's all checked.
      int j = i + 1;
      if (j == _perm.length) {
        outcome = true;
      }
    }

    return outcome;
  }

  boolean checkEquality(PrimitiveAssertion yours, PrimitiveAssertion ours) {
    boolean matched = false;

    if (ours.equals(yours)) {
      matched = true;
    } else if (ours.getName().equals(yours.getName())) {
      String ourValue = ours.getStrValue();
      String yourValue = yours.getStrValue();
      if (ourValue == null) {
        if (yourValue == null) {
          if (equalAttributes(ours, yours)) {
            matched = true;
          }
        }
      } else {
        if (ourValue.equals(yourValue)) {
          if (equalAttributes(ours, yours)) {
            matched = true;
          }
        }
      }
    }
    return matched;
  }

  public String permToString() {
    String result = "";
    for (int i = 0; i < _perm.length; i++) {
      result += _perm[i] + "\n";
    }
    return result;
  }

  public boolean equalAttributes(PrimitiveAssertion ours, PrimitiveAssertion yours) {

    boolean result = true;
    try {
      Hashtable ourAttributes = ours.getAttributes();
      Hashtable yourAttributes = yours.getAttributes();
      if (ourAttributes.size() == yourAttributes.size()) {
        int matchCount = 0;
        // Go through each of ourNames and see if we have a match.
        Enumeration ourKeys = ourAttributes.keys();
        while (ourKeys.hasMoreElements()) {
          QName ourQName = (QName) ourKeys.nextElement();
          String yourValue = yours.getAttribute(ourQName);
          if (yourValue == null) {
            result = false;
            break;
          }
          String ourValue = ours.getAttribute(ourQName);
          if (!yourValue.equals(ourValue)) {
            result = false;
            break;
          }
          matchCount++;
        }
        if (matchCount != ourAttributes.size()) {
          result = false;
        }
      } else {
        result = false;
      }
    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      result = false;
    }
    return result;
  }

  public String toString() {
    StringBuffer result = new StringBuffer("[Alternative]:");
    for (int i = 0; i < _perm.length; i++) {
      Assertion vpa = (Assertion) _perm[i];
      result.append("{" + vpa.toString() + "}");
    }
    return result.toString();
  }

}