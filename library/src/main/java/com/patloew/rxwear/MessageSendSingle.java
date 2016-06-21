package com.patloew.rxwear;

import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Wearable;

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
public class MessageSendSingle extends BaseSingle<Integer> {

    private final String nodeId;
    private final String path;
    private final byte[] data;

    MessageSendSingle(RxWear rxWear, String nodeId, String path, byte[] data, Long timeout, TimeUnit timeUnit) {
        super(rxWear, timeout, timeUnit);
        this.nodeId = nodeId;
        this.path = path;
        this.data = data;
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, final SingleSubscriber<? super Integer> subscriber) {
        setupWearPendingResult(Wearable.MessageApi.sendMessage(apiClient, nodeId, path, data), new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    subscriber.onError(new StatusException(sendMessageResult.getStatus()));
                } else {
                    subscriber.onSuccess(sendMessageResult.getRequestId());
                }
            }
        });
    }
}
