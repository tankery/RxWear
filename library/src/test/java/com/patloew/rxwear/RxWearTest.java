package com.patloew.rxwear;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.Api;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.PendingResult;
import com.mobvoi.android.common.api.Result;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.common.api.Status;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataItem;
import com.mobvoi.android.wearable.DataItemAsset;
import com.mobvoi.android.wearable.DataItemBuffer;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;
import com.patloew.rxwear.events.NodeEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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

@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({ ContextCompat.class, Wearable.class, Status.class, ConnectionResult.class, BaseRx.class })
@SuppressStaticInitializationFor("com.mobvoi.android.wearable.Wearable")
public class RxWearTest {

    @Mock Context ctx;

    @Mock MobvoiApiClient apiClient;
    @Mock Status status;
    @Mock ConnectionResult connectionResult;
    @Mock PendingResult pendingResult;

    @Mock Uri uri;
    @Mock DataItem dataItem;
    @Mock DataItemBuffer dataItemBuffer;

    @Mock DataApi dataApi;
    @Mock MessageApi messageApi;
    @Mock NodeApi nodeApi;

    @Mock RxWear rxWear;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Wearable.class);
        Whitebox.setInternalState(Wearable.class, dataApi);
        Whitebox.setInternalState(Wearable.class, messageApi);
        Whitebox.setInternalState(Wearable.class, nodeApi);

        when(ctx.getApplicationContext()).thenReturn(ctx);
    }

    //////////////////
    // UTIL METHODS //
    //////////////////


    // Mock MobvoiApiClient connection success behaviour
    private <T> void setupBaseObservableSuccess(final BaseObservable<T> baseObservable) {
        setupBaseObservableSuccess(baseObservable, apiClient);
    }

    // Mock MobvoiApiClient connection success behaviour
    private <T> void setupBaseObservableSuccess(final BaseObservable<T> baseObservable, final MobvoiApiClient apiClient) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Subscriber<? super T> subscriber = ((BaseObservable.ApiClientConnectionCallbacks)invocation.getArguments()[0]).subscriber;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        baseObservable.onMobvoiApiClientReady(apiClient, subscriber);
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseObservable).createApiClient(Matchers.any(BaseRx.ApiClientConnectionCallbacks.class));
    }

    // Mock MobvoiApiClient connection success behaviour
    private <T> void setupBaseSingleSuccess(final BaseSingle<T> baseSingle) {
        setupBaseSingleSuccess(baseSingle, apiClient);
    }

    // Mock MobvoiApiClient connection success behaviour
    private <T> void setupBaseSingleSuccess(final BaseSingle<T> baseSingle, final MobvoiApiClient apiClient) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final SingleSubscriber<? super T> subscriber = ((BaseSingle.ApiClientConnectionCallbacks)invocation.getArguments()[0]).subscriber;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        baseSingle.onMobvoiApiClientReady(apiClient, subscriber);
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseSingle).createApiClient(Matchers.any(BaseRx.ApiClientConnectionCallbacks.class));
    }

    // Mock MobvoiApiClient connection error behaviour
    private <T> void setupBaseObservableError(final BaseObservable<T> baseObservable) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Subscriber<? super T> subscriber = ((BaseObservable.ApiClientConnectionCallbacks)invocation.getArguments()[0]).subscriber;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        subscriber.onError(new MobvoiAPIConnectionException("Error connecting to MobvoiApiClient.", connectionResult));
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseObservable).createApiClient(Matchers.any(BaseRx.ApiClientConnectionCallbacks.class));
    }

    // Mock MobvoiApiClient connection error behaviour
    private <T> void setupBaseSingleError(final BaseSingle<T> baseSingle) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final SingleSubscriber<? super T> subscriber = ((BaseSingle.ApiClientConnectionCallbacks)invocation.getArguments()[0]).subscriber;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        subscriber.onError(new MobvoiAPIConnectionException("Error connecting to MobvoiApiClient.", connectionResult));
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseSingle).createApiClient(Matchers.any(BaseRx.ApiClientConnectionCallbacks.class));
    }

    @SuppressWarnings("unchecked")
    private void setPendingResultValue(final Result result) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ResultCallback)invocation.getArguments()[0]).onResult(result);
                return null;
            }
        }).when(pendingResult).setResultCallback(Matchers.<ResultCallback>any());
    }

    private static void assertError(TestSubscriber sub, Class<? extends Throwable> errorClass) {
        sub.assertError(errorClass);
        sub.assertNoValues();
        sub.assertUnsubscribed();
    }

    @SuppressWarnings("unchecked")
    private static void assertSingleValue(TestSubscriber sub, Object value) {
        sub.assertCompleted();
        sub.assertUnsubscribed();
        sub.assertValue(value);
    }

    private static void assertNoValue(TestSubscriber sub) {
        sub.assertCompleted();
        sub.assertUnsubscribed();
        sub.assertNoValues();
    }


    //////////////////////
    // OBSERVABLE TESTS //
    //////////////////////


    // MobvoiApiClientObservable

    @Test
    public void GoogleAPIClientObservable_Success() {
        TestSubscriber<MobvoiApiClient> sub = new TestSubscriber<>();
        MobvoiAPIClientSingle single = PowerMockito.spy(new MobvoiAPIClientSingle(ctx, new Api[] {}));

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, apiClient);
    }

    @Test
    public void GoogleAPIClientObservable_ConnectionException() {
        TestSubscriber<MobvoiApiClient> sub = new TestSubscriber<>();
        final MobvoiAPIClientSingle single = PowerMockito.spy(new MobvoiAPIClientSingle(ctx, new Api[] {}));

        setupBaseSingleError(single);
        Single.create(single).subscribe(sub);

        assertError(sub, MobvoiAPIConnectionException.class);
    }

    /********
     * DATA *
     ********/

    // DataListenerObservable

    @Test
    public void DataListenerObservable_Success() {
        TestSubscriber<DataEvent> sub = new TestSubscriber<>();
        DataListenerObservable observable = PowerMockito.spy(new DataListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(DataApi.DataListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        sub.assertNoTerminalEvent();
        sub.assertNoValues();
    }

    @Test
    public void DataListenerObservable_StatusException() {
        TestSubscriber<DataEvent> sub = new TestSubscriber<>();
        DataListenerObservable observable = PowerMockito.spy(new DataListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(DataApi.DataListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // DataDeleteItemsSingle

    @Test
    public void DataDeleteItemsObservable_Success() {
        TestSubscriber<Integer> sub = new TestSubscriber<>();
        DataApi.DeleteDataItemsResult result = Mockito.mock(DataApi.DeleteDataItemsResult.class);
        DataDeleteItemsSingle single = PowerMockito.spy(new DataDeleteItemsSingle(rxWear, uri, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getNumDeleted()).thenReturn(1);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.deleteDataItems(apiClient, uri)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, 1);
    }

    @Test
    public void DataDeleteItemsSingle_StatusException() {
        TestSubscriber<Integer> sub = new TestSubscriber<>();
        DataApi.DeleteDataItemsResult result = Mockito.mock(DataApi.DeleteDataItemsResult.class);
        DataDeleteItemsSingle single = PowerMockito.spy(new DataDeleteItemsSingle(rxWear, uri, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getNumDeleted()).thenReturn(1);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.deleteDataItems(apiClient, uri)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // DataPutItemSingle

    @Test
    public void DataPutItemSingle_Success() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        PutDataRequest putDataRequest = Mockito.mock(PutDataRequest.class);
        DataApi.DataItemResult result = Mockito.mock(DataApi.DataItemResult.class);
        DataPutItemSingle single = PowerMockito.spy(new DataPutItemSingle(rxWear, putDataRequest, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getDataItem()).thenReturn(dataItem);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.putDataItem(apiClient, putDataRequest)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, dataItem);
    }

    @Test
    public void DataPutItemSingle_StatusException() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        PutDataRequest putDataRequest = Mockito.mock(PutDataRequest.class);
        DataApi.DataItemResult result = Mockito.mock(DataApi.DataItemResult.class);
        DataPutItemSingle single = PowerMockito.spy(new DataPutItemSingle(rxWear, putDataRequest, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getDataItem()).thenReturn(dataItem);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.putDataItem(apiClient, putDataRequest)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // DataGetItemsObservable

    @Test
    public void DataGetItemsObservable_Uri_Success() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        DataGetItemsObservable observable = PowerMockito.spy(new DataGetItemsObservable(rxWear, uri, null, null));

        when(dataItemBuffer.getCount()).thenReturn(0);
        when(dataItemBuffer.getStatus()).thenReturn(status);
        setPendingResultValue(dataItemBuffer);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.getDataItems(apiClient, uri)).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertNoValue(sub);
    }

    @Test
    public void DataGetItemsObservable_Uri_StatusException() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        DataGetItemsObservable observable = PowerMockito.spy(new DataGetItemsObservable(rxWear, uri, null, null));

        when(dataItemBuffer.getCount()).thenReturn(0);
        when(dataItemBuffer.getStatus()).thenReturn(status);
        setPendingResultValue(dataItemBuffer);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.getDataItems(apiClient, uri)).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    @Test
    public void DataGetItemsObservable_Success() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        DataGetItemsObservable observable = PowerMockito.spy(new DataGetItemsObservable(rxWear, null, null, null));

        when(dataItemBuffer.getCount()).thenReturn(0);
        when(dataItemBuffer.getStatus()).thenReturn(status);
        setPendingResultValue(dataItemBuffer);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.getDataItems(apiClient)).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertNoValue(sub);
    }

    @Test
    public void DataGetItemsObservable_StatusException() {
        TestSubscriber<DataItem> sub = new TestSubscriber<>();
        DataGetItemsObservable observable = PowerMockito.spy(new DataGetItemsObservable(rxWear, null, null, null));

        when(dataItemBuffer.getCount()).thenReturn(0);
        when(dataItemBuffer.getStatus()).thenReturn(status);
        setPendingResultValue(dataItemBuffer);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.getDataItems(apiClient)).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // DataGetFdForAssetSingle

    @Test
    public void DataGetFdForAssetSingle_DataItemAsset_Success() {
        TestSubscriber<DataApi.GetFdForAssetResult> sub = new TestSubscriber<>();
        DataItemAsset dataItemAsset = Mockito.mock(DataItemAsset.class);
        DataApi.GetFdForAssetResult result = Mockito.mock(DataApi.GetFdForAssetResult.class);
        DataGetFdForAssetSingle single = PowerMockito.spy(new DataGetFdForAssetSingle(rxWear, dataItemAsset, null, null, null));

        when(result.getStatus()).thenReturn(status);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.getFdForAsset(apiClient, dataItemAsset)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, result);
    }

    @Test
    public void DataGetFdForAssetSingle_DataItemAsset_StatusException() {
        TestSubscriber<DataApi.GetFdForAssetResult> sub = new TestSubscriber<>();
        DataItemAsset dataItemAsset = Mockito.mock(DataItemAsset.class);
        DataApi.GetFdForAssetResult result = Mockito.mock(DataApi.GetFdForAssetResult.class);
        DataGetFdForAssetSingle single = PowerMockito.spy(new DataGetFdForAssetSingle(rxWear, dataItemAsset, null, null, null));

        when(result.getStatus()).thenReturn(status);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.getFdForAsset(apiClient, dataItemAsset)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    @Test
    public void DataGetFdForAssetSingle_Asset_Success() {
        TestSubscriber<DataApi.GetFdForAssetResult> sub = new TestSubscriber<>();
        Asset asset = Mockito.mock(Asset.class);
        DataApi.GetFdForAssetResult result = Mockito.mock(DataApi.GetFdForAssetResult.class);
        DataGetFdForAssetSingle single = PowerMockito.spy(new DataGetFdForAssetSingle(rxWear, null, asset, null, null));

        when(result.getStatus()).thenReturn(status);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(dataApi.getFdForAsset(apiClient, asset)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, result);
    }

    @Test
    public void DataGetFdForAssetSingle_Asset_StatusException() {
        TestSubscriber<DataApi.GetFdForAssetResult> sub = new TestSubscriber<>();
        Asset asset = Mockito.mock(Asset.class);
        DataApi.GetFdForAssetResult result = Mockito.mock(DataApi.GetFdForAssetResult.class);
        DataGetFdForAssetSingle single = PowerMockito.spy(new DataGetFdForAssetSingle(rxWear, null, asset, null, null));

        when(result.getStatus()).thenReturn(status);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(dataApi.getFdForAsset(apiClient, asset)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    /***********
     * MESSAGE *
     ***********/

    // MessageListenerObservable

    @Test
    public void MessageListenerObservable_Success() {
        TestSubscriber<MessageEvent> sub = new TestSubscriber<>();
        MessageListenerObservable observable = PowerMockito.spy(new MessageListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(messageApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(MessageApi.MessageListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        sub.assertNoTerminalEvent();
        sub.assertNoValues();
    }

    @Test
    public void MessageListenerObservable_StatusException() {
        TestSubscriber<MessageEvent> sub = new TestSubscriber<>();
        MessageListenerObservable observable = PowerMockito.spy(new MessageListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(false);
        when(messageApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(MessageApi.MessageListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // MessageSendSingle

    @Test
    public void MessageSendSingle_Success() {
        TestSubscriber<Integer> sub = new TestSubscriber<>();
        MessageApi.SendMessageResult result = Mockito.mock(MessageApi.SendMessageResult.class);
        String nodeId = "nodeId";
        String path = "path";
        byte[] data = new byte[] {};
        MessageSendSingle single = PowerMockito.spy(new MessageSendSingle(rxWear, nodeId, path, data, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getRequestId()).thenReturn(1);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(messageApi.sendMessage(apiClient, nodeId, path, data)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, 1);
    }

    @Test
    public void MessageSendSingle_StatusException() {
        TestSubscriber<Integer> sub = new TestSubscriber<>();
        MessageApi.SendMessageResult result = Mockito.mock(MessageApi.SendMessageResult.class);
        String nodeId = "nodeId";
        String path = "path";
        byte[] data = new byte[] {};
        MessageSendSingle single = PowerMockito.spy(new MessageSendSingle(rxWear, nodeId, path, data, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getRequestId()).thenReturn(1);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(messageApi.sendMessage(apiClient, nodeId, path, data)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    /********
     * NODE *
     ********/

    // NodeListenerObservable

    @Test
    public void NodeListenerObservable_Success() {
        TestSubscriber<NodeEvent> sub = new TestSubscriber<>();
        NodeListenerObservable observable = PowerMockito.spy(new NodeListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(nodeApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(NodeApi.NodeListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        sub.assertNoTerminalEvent();
        sub.assertNoValues();
    }

    @Test
    public void NodeListenerObservable_StatusException() {
        TestSubscriber<NodeEvent> sub = new TestSubscriber<>();
        NodeListenerObservable observable = PowerMockito.spy(new NodeListenerObservable(rxWear, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(false);
        when(nodeApi.addListener(Matchers.any(MobvoiApiClient.class), Matchers.any(NodeApi.NodeListener.class))).thenReturn(pendingResult);

        setupBaseObservableSuccess(observable);
        Observable.create(observable).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // NodeGetConnectedSingle

    @Test
    public void NodeGetConnectedSingle_Success() {
        TestSubscriber<List<Node>> sub = new TestSubscriber<>();
        NodeApi.GetConnectedNodesResult result = Mockito.mock(NodeApi.GetConnectedNodesResult.class);
        NodeGetConnectedSingle single = PowerMockito.spy(new NodeGetConnectedSingle(rxWear, null, null));

        List<Node> nodeList = new ArrayList<>();

        when(result.getStatus()).thenReturn(status);
        when(result.getNodes()).thenReturn(nodeList);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(nodeApi.getConnectedNodes(apiClient)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, nodeList);
    }

    @Test
    public void NodeGetConnectedSingle_StatusException() {
        TestSubscriber<List<Node>> sub = new TestSubscriber<>();
        NodeApi.GetConnectedNodesResult result = Mockito.mock(NodeApi.GetConnectedNodesResult.class);
        NodeGetConnectedSingle single = PowerMockito.spy(new NodeGetConnectedSingle(rxWear, null, null));

        List<Node> nodeList = new ArrayList<>();

        when(result.getStatus()).thenReturn(status);
        when(result.getNodes()).thenReturn(nodeList);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(nodeApi.getConnectedNodes(apiClient)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // NodeGetLocalSingle

    @Test
    public void NodeGetLocalSingle_Success() {
        TestSubscriber<Node> sub = new TestSubscriber<>();
        NodeApi.GetLocalNodeResult result = Mockito.mock(NodeApi.GetLocalNodeResult.class);
        Node node = Mockito.mock(Node.class);
        NodeGetLocalSingle single = PowerMockito.spy(new NodeGetLocalSingle(rxWear, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getNode()).thenReturn(node);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(nodeApi.getLocalNode(apiClient)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertSingleValue(sub, node);
    }

    @Test
    public void NodeGetLocalSingle_StatusException() {
        TestSubscriber<Node> sub = new TestSubscriber<>();
        NodeApi.GetLocalNodeResult result = Mockito.mock(NodeApi.GetLocalNodeResult.class);
        Node node = Mockito.mock(Node.class);
        NodeGetLocalSingle single = PowerMockito.spy(new NodeGetLocalSingle(rxWear, null, null));

        when(result.getStatus()).thenReturn(status);
        when(result.getNode()).thenReturn(node);
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(nodeApi.getLocalNode(apiClient)).thenReturn(pendingResult);

        setupBaseSingleSuccess(single);
        Single.create(single).subscribe(sub);

        assertError(sub, StatusException.class);
    }
}
