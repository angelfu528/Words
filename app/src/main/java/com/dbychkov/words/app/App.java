/**
 * Copyright (C) dbychkov.com.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbychkov.words.app;

import android.app.Application;
import android.util.Log;

import com.cmcm.adsdk.CMAdManager;
import com.dbychkov.words.dagger.component.ApplicationComponent;
import com.dbychkov.words.dagger.component.DaggerApplicationComponent;
import com.dbychkov.words.dagger.module.ApplicationModule;
import com.dbychkov.words.util.LogHelper;

import java.util.LinkedList;

import me.kiip.sdk.Kiip;
import me.kiip.sdk.KiipFragmentCompat;
import me.kiip.sdk.Poptart;

/**
 * Class for maintaining global application state
 */
public class App extends Application implements Kiip.OnContentListener {

    private static final String TAG = LogHelper.makeLogTag(App.class);

    private static final String APP_KEY = "96e34d9ca500cd111fa724038869952d";
    private static final String APP_SECRET = "65abb915036e3d67b5954a58e5bb7ae3";


    private static App singleton;

    public static App getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        setUncaughtExceptionHandler();
        initApplicationComponent();

        //Initialize sdk
        //First parameter: context
        //Second parameter: Mid (the first four numbers of Posid)
        //Product channel ID, could be empty string if none
        CMAdManager.applicationInit(this, "1388", "");
        //开启Debug模式，默认不开启不会打印log
        CMAdManager.enableLog();

        //kiip
        // Set a global poptart queue to persist poptarts across Activities
        KiipFragmentCompat.setDefaultQueue(new LinkedList<Poptart>());

        // Instantiate and set the shared Kiip instance
        Kiip kiip = Kiip.init(this, APP_KEY, APP_SECRET);

        // Listen for Kiip events
        kiip.setOnContentListener(this);

        Kiip.setInstance(kiip);
    }

    private ApplicationComponent applicationComponent;

    private void initApplicationComponent() {
        applicationComponent = DaggerApplicationComponent
                .builder().applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    private void setUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                LogHelper.e(TAG, ex, "Exception escaped");
            }
        });
    }

    @Override
    public void onContent(Kiip kiip, String s, int i, String s1, String s2) {
        Log.d(TAG, "onContent content=" + s + " quantity=" + i + " transactionId=" + s1 + " signature=" + s2);

        // Add quantity amount of content to player's profile
        // e.g +20 coins to user's wallet
        // http://docs.kiip.com/en/guide/android.html#getting_virtual_rewards
    }
}
