/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package fastmapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Only 6 fields
 */
public class SmallObject {

    private String field1;

    private Integer field2;

    private int field3;

    private Character field4;

    private char field5;

    private Object field6;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public Integer getField2() {
        return field2;
    }

    public void setField2(Integer field2) {
        this.field2 = field2;
    }

    public int getField3() {
        return field3;
    }

    public void setField3(int field3) {
        this.field3 = field3;
    }

    public Character getField4() {
        return field4;
    }

    public void setField4(Character field4) {
        this.field4 = field4;
    }

    public char getField5() {
        return field5;
    }

    public void setField5(char field5) {
        this.field5 = field5;
    }

    public Object getField6() {
        return field6;
    }

    public void setField6(Object field6) {
        this.field6 = field6;
    }

    public static Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();
        map.put("field1", "1");
        map.put("field2", "2");
        map.put("field3", "3");
        map.put("field4", "4");
        map.put("field5", "5");
        return map;
    }
}