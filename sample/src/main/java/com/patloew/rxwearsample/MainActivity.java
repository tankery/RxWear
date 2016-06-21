package com.patloew.rxwearsample;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.patloew.rxwear.MobvoiAPIConnectionException;
import com.patloew.rxwear.RxWear;
import com.patloew.rxwear.transformers.DataItemGetDataMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private EditText titleEditText;
    private EditText messageEditText;
    private Button sendButton;
    private EditText persistentEditText;
    private Button setPersistentButton;

    private CompositeSubscription subscription = new CompositeSubscription();
    private Observable<Boolean> validator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxWear.init(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        titleEditText = (EditText) findViewById(R.id.et_title);
        messageEditText = (EditText) findViewById(R.id.et_message);
        sendButton = (Button) findViewById(R.id.bt_send);
        persistentEditText = (EditText) findViewById(R.id.et_persistenttext);
        setPersistentButton = (Button) findViewById(R.id.bt_set_persistent);

        subscription.add(RxView.clicks(sendButton)
                .doOnNext(click -> hideKeyboard())
                .flatMap(click2 -> validate())
                .filter(isValid -> isValid)
                .flatMap(valid ->
                        RxWear.Message.SendDataMap.toAllRemoteNodes("/message")
                            .putString("title", titleEditText.getText().toString())
                            .putString("message", messageEditText.getText().toString())
                            .toObservable()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestId -> Snackbar.make(coordinatorLayout, "Sent message", Snackbar.LENGTH_LONG).show(),
                        throwable -> {
                            Log.e("MainActivity", "Error on sending message", throwable);

                            if(throwable instanceof MobvoiAPIConnectionException) {
                                Snackbar.make(coordinatorLayout, "Android Wear app is not installed", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(coordinatorLayout, "Could not send message", Snackbar.LENGTH_LONG).show();
                            }
                        })
        );

        subscription.add(RxView.clicks(setPersistentButton)
                .doOnNext(click -> hideKeyboard())
                .flatMap(click2 -> RxWear.Data.PutDataMap.to("/persistentText").putString("text", persistentEditText.getText().toString()).toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataItem1 -> Snackbar.make(coordinatorLayout, "Set persistent text", Snackbar.LENGTH_LONG).show(),
                        throwable -> {
                            Log.e("MainActivity", "Error on setting persistent text", throwable);

                            if(throwable instanceof MobvoiAPIConnectionException) {
                                Snackbar.make(coordinatorLayout, "Android Wear app is not installed", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(coordinatorLayout, "Could not set persistent text", Snackbar.LENGTH_LONG).show();
                            }
                        }));

        subscription.add(RxWear.Data.get("/persistentText")
                .compose(DataItemGetDataMap.noFilter())
                .map(dataMap -> dataMap.getString("text"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(text -> persistentEditText.setText(text)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sendButton.getWindowToken(), 0);
    }

    private Observable<Boolean> validate() {
        if(validator == null) {
            validator = Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
                boolean valid = true;

                if(TextUtils.isEmpty(titleEditText.getText())) {
                    titleEditText.setError("Please enter title");
                    valid = false;
                }

                if(TextUtils.isEmpty(messageEditText.getText())) {
                    messageEditText.setError("Please enter message");
                    valid = false;
                }

                subscriber.onNext(valid);
                subscriber.onCompleted();
            });
        }

        return validator;
    }
}
