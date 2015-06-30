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

package net.openhft.saxophone.json.handler;

import java.io.IOException;

/**
 * Triggered on JSON array, object or standalone (top-level) boolean value:
 * {@code `true`} or {@code `false`}.
 *
 * @see net.openhft.saxophone.json.JsonParserBuilder#booleanHandler(BooleanHandler)
 */
public interface BooleanHandler extends JsonHandlerBase {
    /**
     * Handles a JSON array, object or standalone (top-level) boolean value:
     * {@code `true`} or {@code `false`}.
     *
     * @param value the boolean value
     * @return {@code true} if the parsing should be continued, {@code false} if it should be
     *         stopped immediately
     * @  if an error occurred during handling
     */
    boolean onBoolean(boolean value) throws IOException;
}
