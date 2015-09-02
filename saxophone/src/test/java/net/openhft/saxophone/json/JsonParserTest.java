/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.saxophone.json;

import com.google.gson.JsonElement;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.saxophone.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public final class JsonParserTest {

    public static final String WRONG_NESTED_ARRAYS = "[[], [[[]]";
    public static final String BEYOND_MAX_LONG = "9223372036854775808";
    public static final String BEYOND_MIN_LONG = "-9223372036854775809";

    public static Bytes stringToBytes(String json) {
            return Bytes.from(json);
    }

    @Test
    @Ignore
    public void testInts() {
        test("{\"k1\": 1, \"k2\": 2}");
        test("[-1, 1, 0, -0]");
        test("[9223372036854775807, -9223372036854775808]");
    }

    @Ignore
    @Test(expected = ParseException.class)
    public void testMaxLongOverflowSimple() {
        testSimple(BEYOND_MAX_LONG);
    }
    @Ignore
    @Test(expected = ParseException.class)
    public void testMaxLongOverflowPull() {
        testPull(BEYOND_MAX_LONG);
    }

    @Ignore
    @Test(expected = ParseException.class)
    public void testMinLongOverflowSimple() {
        testSimple(BEYOND_MIN_LONG);
    }
    @Ignore
    @Test(expected = ParseException.class)
    public void testMinLongOverflowPull() {
        testPull(BEYOND_MIN_LONG);
    }

    @Ignore
    @Test
    public void testDoubles() {
        test("{\"k1\": -1.0, \"k2\": 1.0}");
        test("[1.0, 2.0, 3.0]");
        test("[9.223372e+18, 9.223372e-18, 9.223372E+18, 9.223372E-18]");
    }

    @Test
    public void testBooleans() {
        test("{\"k1\": true, \"k2\": false}");
        test("[true, false]");
        test("true");
        test("false");
    }

    @Test
    public void testNullValue() {
        test("{\"k1\": null}");
        test("[null]");
        test("null");
    }

    @Test
    public void testStrings() {
        test("{\"k1\": \"v1\", \"\": \"v2\"}"); // empty key
        test("[\"v1\", \"v2\"]");
        test("\"v1\"");
        test("\"\""); // empty
    }

    @Ignore
    @Test
    public void testEscape() {
        test("\" \\n \\t \\\" \\f \\r \\/ \\\\ \\b \"");
    }

    @Test
    @Ignore("TODO fix")
    public void testSurrogates() {
        test("{\"k1\":\"\\uD83D\\uDE03\"}");
    }

    @Test
    public void testNestedObjects() {
        test("{\"k1\": {\"k2\": {}}}");
    }

    @Test
    public void testNestedArrays() {
        test("[[], [[]]]");
    }

    @Test(expected = ParseException.class)
    public void testWrongNestedArraysSimple() {
        testSimple(WRONG_NESTED_ARRAYS);
    }

    @Test(expected = ParseException.class)
    public void testWrongNestedArraysPull() {
        testPull(WRONG_NESTED_ARRAYS);
    }

    /** To test the ability of state stack to grow */
    @Test
    public void testVeryDeepStructure() {
        String json = "";
        // our parser parses even 10000 nested objects, but
        // "reference" Gson parser fails with StackOverflowError
        for (int i = 0; i < 1000; i++) {
            json += "{\"\":[";
        }
        for (int i = 0; i < 1000; i++) {
            json += "]}";
        }
        test(json);
    }

    /** To test the ability of lexer buf to grow */
    @Test
    public void testVeryLongLiterals() {
        String key = "";
        for (int i = 0; i < 10000; i++) {
            key += "key";
        }
        String value = "";
        for (int i = 0; i < 10000; i++) {
            key += "value";
        }
        test("{\"" + key + "\": \"" + value + "\"}");
    }

    private void test(String json) {
        testSimple(json);
        testPull(json);
    }
    
    private void testSimple(String json) {
        StringWriter stringWriter = new StringWriter();
        JsonParser p = JsonParser.builder().applyAdapter(new WriterAdapter(stringWriter)).build();
        p.parse(stringToBytes(json));
        p.finish();
        com.google.gson.JsonParser referenceParser = new com.google.gson.JsonParser();
        JsonElement o1 = referenceParser.parse(json);
        JsonElement o2 = referenceParser.parse(stringWriter.toString());
        assertEquals(o1, o2);
    }

    private void testPull(String json) {
        StringWriter stringWriter = new StringWriter();
        JsonParser p = JsonParser.builder().applyAdapter(new WriterAdapter(stringWriter)).build();
        Bytes jsonBytes = stringToBytes(json);
        Bytes parseBytes = Bytes.elasticByteBuffer();
        for (long i = 0; i < jsonBytes.capacity(); i++) {
            parseBytes.writeByte(jsonBytes.readByte(i));
            p.parse(parseBytes);
        }
        p.finish();
        com.google.gson.JsonParser referenceParser = new com.google.gson.JsonParser();
        JsonElement o1 = referenceParser.parse(json);
        JsonElement o2 = referenceParser.parse(stringWriter.toString());
        assertEquals(o1, o2);
    }
}
