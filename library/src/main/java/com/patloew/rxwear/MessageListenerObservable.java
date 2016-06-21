package com.patloew.rxwear;

import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.common.api.Status;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import rx.Subscriber;

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
public class MessageListenerObservable extends BaseObservable<MessageEvent> {

    private MessageApi.MessageListener listener;

    MessageListenerObservable(RxWear rxWear, Long timeout, TimeUnit timeUnit) {
        super(rxWear, timeout, timeUnit);
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, final Subscriber<? super MessageEvent> subscriber) {
        listener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                subscriber.onNext(messageEvent);
            }
        };

        ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (!status.isSuccess()) {
                    subscriber.onError(new StatusException(status));
                }
            }
        };

        setupWearPendingResult(Wearable.MessageApi.addListener(apiClient, listener), resultCallback);
    }


    @Override
    protected void onUnsubscribed(MobvoiApiClient apiClient) {
        Wearable.MessageApi.removeListener(apiClient, listener);
    }
}
