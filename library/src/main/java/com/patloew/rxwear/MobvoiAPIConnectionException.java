package com.patloew.rxwear;

import com.mobvoi.android.common.ConnectionResult;

/* Copyright (C) 2015 Michał Charmas (http://blog.charmas.pl)
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
public class MobvoiAPIConnectionException extends RuntimeException {
    private final ConnectionResult connectionResult;

    MobvoiAPIConnectionException(String detailMessage, ConnectionResult connectionResult) {
        super(detailMessage);
        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }

    public boolean wasResolutionUnsuccessful() {
        if(connectionResult != null) {
            return connectionResult.hasResolution();
        } else {
            return false;
        }
    }
}
