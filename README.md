# Reactive Wearable API Library for Android

[![Build Status](https://travis-ci.org/patloew/RxWear.svg?branch=master)](https://travis-ci.org/patloew/RxWear)  [ ![Download](https://api.bintray.com/packages/patloew/maven/RxWear/images/download.svg) ](https://bintray.com/patloew/maven/RxWear/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxWear-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3271) [![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)

This library wraps the Wearable API in [RxJava](https://github.com/ReactiveX/RxJava) Observables and Singles. No more managing MobvoiApiClients! Also, there are some helper classes, which ease communication between phone and wear app.

# Usage

Initialize RxWear once, preferably in your Application `onCreate()` via `RxWear.init(...)`. The RxWear class is very similar to the Wearable class provided by the Wearable API. Instead of `Wearable.MessageApi.sendMessage(apiClient, nodeId, path, data)` you can use `RxWear.Message.send(nodeId, path, data)`. 

There are also some helper classes to ease the putting/sending of data.
* `RxWear.Data.PutDataMap`: Use this to put a DataItem containing a DataMap to a path or a pathPrefix with an auto appended ID. 
* `RxWear.Data.PutSerializable`: Use this to put a DataItem containing a Serializable object to a path or a pathPrefix with an auto appended ID. 
* `RxWear.Message.SendDataMap`: Use this to send a message containing a DataMap to either one specific node or all remote nodes.
* `RxWear.Message.SendSerializable`: Use this to send a message containing a Serializable object to either one specific node or all remote nodes.

A few Observable Transformers are included to ease fetching the data. Since these include filtering, they cannot operate on Singles by default, but you can use `single.toObservable().compose(...)`.
* `DataEventGetDataMap`: Use this Transformer to get the DataMap from a DataEvent and optionally filter the events.
* `DataEventGetSerializable`: Use this Transformer to get a Serializable object from a DataEvent and optionally filter the events.
* `DataItemGetDataMap`: Use this Transformer to get the DataMap from a DataItem and optionally filter the items.
* `DataItemGetSerializable`: Use this Transformer to get a Serializable object from a DataItems and optionally filter the items.
* `MessageEventGetDataMap`: Use this Transformer to get the DataMap from a MessageEvent and optionally filter the events.
* `MessageEventGetSerializable`: Use this Transformer to get a Serializable object from a MessageEvent and optionally filter the events.

Example:

```java
RxWear.init(context);

// Phone App

RxWear.Message.SendDataMap.toAllRemoteNodes("/dataMap")
	    .putString("title", "Title")
	    .putString("message", "My message")
	    .toObservable()
	    .subscribe(requestId -> {
	    	/* do something */
	    });

RxWear.Data.PutSerializable.urgentTo("/serializable", serializable)
        .subscribe(dataItem -> {
	        /* do something */
        });

// Wear App

RxWear.Message.listen("/dataMap", MessageApi.FILTER_LITERAL)
        .compose(MessageEventGetDataMap.noFilter())
        .subscribe(dataMap -> {
            String title = dataMap.getString("title", getString(R.string.no_message));
            String message = dataMap.getString("message", getString(R.string.no_message_info));
            /* do something */
        });

RxWear.Data.listen("/serializable", DataApi.FILTER_LITERAL)
        .compose(DataEventGetSerializable.<MySerializableType>filterByType(DataEvent.TYPE_CHANGED))
        .subscribe(serializable -> {
            /* do something */
        });

```

An optional global default timeout for all Wearable API requests made through the library can be set via `RxWear.setDefaultTimeout(...)`. In addition, timeouts can be set when creating a new Observable by providing timeout parameters, e.g. `RxWear.Message.send(nodeId, path, data, 15, TimeUnit.SECONDS)`. These parameters override the default timeout. When a timeout occurs, a StatusException is provided via `onError()`. The timeouts specified here are only used for calls to the Wearable API, e.g. a timeout will not occur when a listener does not emit an item within the specified timeout. The RxJava timeout operators can be used for this use case.

You can also obtain a `Single<MobvoiApiClient>`, which connects on subscribe and disconnects on unsubscribe via `GoogleAPIClientSingle.create(...)`.

The following Exceptions are thrown in the lib and provided via `onError()`:

* `StatusException`: When the call to the Wearable API was not successful or timed out
* `GoogleAPIConnectionException`: When connecting to the GoogleAPIClient was not successful.
* `GoogleAPIConnectionSuspendedException`: When the MobvoiApiClient connection was suspended.

# Sample

A basic sample app is available in the `sample` and `wearsample` projects. 

# Setup

The lib is available on jCenter. Add the following to your `build.gradle`:

	dependencies {
	    compile 'com.patloew.rxwear:rxwear:1.2.0'
	}

# Credits

The code for managing the MobvoiApiClient is taken from the [Android-ReactiveLocation](https://github.com/mcharmas/Android-ReactiveLocation) library by Michał Charmas, which is licensed under the Apache License, Version 2.0.

# License

	Copyright 2016 Patrick Löwenstein

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.