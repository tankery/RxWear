package com.patloew.rxwear;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.Api;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.PendingResult;
import com.mobvoi.android.common.api.Result;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.TimeUnit;

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
 *
 * ---------------
 *
 * FILE MODIFIED by Patrick Löwenstein, 2016
 *
 */
public abstract class BaseRx<T> {
    private final Context ctx;
    private final Api[] services;
    private final Long timeoutTime;
    private final TimeUnit timeoutUnit;

    protected BaseRx(@NonNull RxWear rxWear, Long timeout, TimeUnit timeUnit) {
        this.ctx = rxWear.getContext();
        this.services = new Api[] {Wearable.API };

        if(timeout != null && timeUnit != null) {
            this.timeoutTime = timeout;
            this.timeoutUnit = timeUnit;
        } else {
            this.timeoutTime = RxWear.getDefaultTimeout();
            this.timeoutUnit = RxWear.getDefaultTimeoutUnit();
        }
    }

    protected BaseRx(@NonNull Context ctx, @NonNull Api[] services) {
        this.ctx = ctx;
        this.services = services;
        timeoutTime = null;
        timeoutUnit = null;
    }

    protected final <T extends Result> void setupWearPendingResult(PendingResult<T> pendingResult, ResultCallback<T> resultCallback) {
        if(timeoutTime != null && timeoutUnit != null) {
            pendingResult.setResultCallback(resultCallback, timeoutTime, timeoutUnit);
        } else {
            pendingResult.setResultCallback(resultCallback);
        }
    }

    protected final MobvoiApiClient createApiClient(ApiClientConnectionCallbacks apiClientConnectionCallbacks) {

        MobvoiApiClient.Builder apiClientBuilder = new MobvoiApiClient.Builder(ctx);


        for (Api service : services) {
            apiClientBuilder.addApi(service);
        }

        apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

        MobvoiApiClient apiClient = apiClientBuilder.build();

        apiClientConnectionCallbacks.setClient(apiClient);

        return apiClient;
    }

    protected void onUnsubscribed(MobvoiApiClient locationClient) { }

    protected abstract class ApiClientConnectionCallbacks implements
            MobvoiApiClient.ConnectionCallbacks,
            MobvoiApiClient.OnConnectionFailedListener {

        protected MobvoiApiClient apiClient;

        protected ApiClientConnectionCallbacks() { }

        public void setClient(MobvoiApiClient client) {
            this.apiClient = client;
        }
    }
}
