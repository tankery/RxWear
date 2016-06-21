package com.patloew.rxwear;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mobvoi.android.common.api.Api;
import com.mobvoi.android.common.api.MobvoiApiClient;

import rx.Single;
import rx.SingleSubscriber;

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
public class MobvoiAPIClientSingle extends BaseSingle<MobvoiApiClient> {

    @SafeVarargs
    public static Single<MobvoiApiClient> create(@NonNull Context context, @NonNull Api... apis) {
        return Single.create(new MobvoiAPIClientSingle(context, apis));
    }

    MobvoiAPIClientSingle(Context ctx, Api[] apis) {
        super(ctx, apis);
    }

    @Override
    protected void onMobvoiApiClientReady(MobvoiApiClient apiClient, SingleSubscriber<? super MobvoiApiClient> observer) {
        observer.onSuccess(apiClient);
    }
}
