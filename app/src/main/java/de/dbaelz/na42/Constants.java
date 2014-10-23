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

public class Constants {
    // Logging Tag
    public static final String LOG_TAG = "NotAlways42";

    // Request codes for communication with GPGS
    public static final int REQUEST_CODE_SIGNIN = 10;
    public static final int REQUEST_CODE_SELECT_SAVEGAME = 20;
    public static final int REQUEST_CODE_MP_NEW_GAME = 21;
    public static final int REQUEST_CODE_MP_SEARCH_GAME = 22;

    // Singleplayer Savegame
    public static final String SAVEGAME_PARCEL = "savegame_parcel";


    public static final String SNAPSHOT_METADATA = "metadata";
}
