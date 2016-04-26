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

package com.dbychkov.words.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dbychkov.words.app.App;
import com.dbychkov.words.dagger.component.ActivityComponent;
import com.dbychkov.words.dagger.component.ApplicationComponent;
import com.dbychkov.words.dagger.component.DaggerActivityComponent;
import com.dbychkov.words.dagger.module.ActivityModule;
import com.dbychkov.words.util.KiipHelper;

import me.kiip.sdk.Poptart;

/**
 * Base activity for all the activities in the app
 */
public abstract class BaseActivity extends AppCompatActivity implements KiipHelper.Listener {

    private ActivityComponent activityComponent;
    private KiipHelper mKiipHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityComponent();
        injectActivity(getActivityComponent());
        mKiipHelper = new KiipHelper(this, this);
        mKiipHelper.onCreate(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    public ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    private void initActivityComponent() {
        this.activityComponent = DaggerActivityComponent
                .builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    public abstract void injectActivity(ActivityComponent component);

    @Override
    protected void onStart() {
        super.onStart();
        mKiipHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKiipHelper.onStop(this);
    }

    public KiipHelper getKiipHelper() {
        return mKiipHelper;
    }

    public void showPoptart(Poptart poptart) {
        mKiipHelper.getKiipFragment().showPoptart(poptart);
    }

    public void showError(Exception exception) {
        getKiipHelper().showAlert("Kiip Error", exception);
    }

    // Session Listeners from extending BaseFragmentActivity

    @Override
    public void onStartSession(KiipHelper kiipHelper, Poptart poptart, Exception e) {
        if (poptart != null) {
            showPoptart(poptart);
        }
        if (e != null) {
            showError(e);
        }
    }

    @Override
    public void onEndSession(KiipHelper kiipHelper, Exception e) {
        // no-op
    }
}
