package com.patloew.rxwear;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.DataItem;
import com.mobvoi.android.wearable.DataItemBuffer;
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
public class DataGetItemsObservable extends BaseObservable<DataItem> {

    private final Uri uri;

    DataGetItemsObservable(RxWear rxWear, Uri uri, Long timeout, TimeUnit timeUnit) {
        super(rxWear, timeout, timeUnit);
        this.uri = uri;
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, final Subscriber<? super DataItem> subscriber) {
        ResultCallback<DataItemBuffer> resultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(@NonNull DataItemBuffer dataItemBuffer) {
                try {
                    if(!dataItemBuffer.getStatus().isSuccess()) {
                        subscriber.onError(new StatusException(dataItemBuffer.getStatus()));
                    } else {
                        for (int i = 0; i < dataItemBuffer.getCount(); i++) {
                            if(subscriber.isUnsubscribed()) { break; }
                            subscriber.onNext(dataItemBuffer.get(i).freeze());
                        }

                        subscriber.onCompleted();
                    }
                } catch(Throwable throwable) {
                    subscriber.onError(throwable);
                } finally {
                    dataItemBuffer.release();
                }
            }
        };

        if(uri == null) {
            setupWearPendingResult(Wearable.DataApi.getDataItems(apiClient), resultCallback);
        } else {
            setupWearPendingResult(Wearable.DataApi.getDataItems(apiClient, uri), resultCallback);
        }
    }
}
