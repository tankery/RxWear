package com.patloew.rxwear;

import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.DataItemAsset;
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
public class DataGetFdForAssetSingle extends BaseSingle<DataApi.GetFdForAssetResult> {

    private final DataItemAsset dataItemAsset;
    private final Asset asset;

    DataGetFdForAssetSingle(RxWear rxWear, DataItemAsset dataItemAsset, Asset asset, Long timeout, TimeUnit timeUnit) {
        super(rxWear, timeout, timeUnit);
        this.dataItemAsset = dataItemAsset;
        this.asset = asset;
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, final SingleSubscriber<? super DataApi.GetFdForAssetResult> subscriber) {
        ResultCallback<DataApi.GetFdForAssetResult> resultCallback = new ResultCallback<DataApi.GetFdForAssetResult>() {
            @Override
            public void onResult(@NonNull DataApi.GetFdForAssetResult getFdForAssetResult) {
                if (!getFdForAssetResult.getStatus().isSuccess()) {
                    subscriber.onError(new StatusException(getFdForAssetResult.getStatus()));
                } else {
                    subscriber.onSuccess(getFdForAssetResult);
                }
            }
        };

        if(asset != null) {
            setupWearPendingResult(Wearable.DataApi.getFdForAsset(apiClient, asset), resultCallback);
        } else {
            setupWearPendingResult(Wearable.DataApi.getFdForAsset(apiClient, dataItemAsset), resultCallback);
        }
    }
}
