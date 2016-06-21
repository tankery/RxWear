package com.patloew.rxwear;

import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.SingleSubscriber;

/* Copyright 2016 Patrick Löwenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
public class NodeGetConnectedSingle extends BaseSingle<List<Node>> {

    NodeGetConnectedSingle(RxWear rxWear, Long timeout, TimeUnit timeUnit) {
        super(rxWear, timeout, timeUnit);
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, final SingleSubscriber<? super List<Node>> subscriber) {
        setupWearPendingResult(Wearable.NodeApi.getConnectedNodes(apiClient), new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if (!getConnectedNodesResult.getStatus().isSuccess()) {
                    subscriber.onError(new StatusException(getConnectedNodesResult.getStatus()));
                } else {
                    subscriber.onSuccess(getConnectedNodesResult.getNodes());
                }
            }
        });

    }
}
