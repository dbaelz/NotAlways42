/*
 * Copyright 2014 Daniel Bälz
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

package de.dbaelz.na42;

public enum RoundState {
    NOT_PLAYED(0),
    WON(1),
    LOST(2);

    private int state;

    private RoundState(int state) {
        this.state = state;
    }

    public int getValue() {
        return state;
    }
}
