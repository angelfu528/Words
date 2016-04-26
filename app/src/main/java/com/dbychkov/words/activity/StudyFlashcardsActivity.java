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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import butterknife.*;
import me.kiip.sdk.Kiip;
import me.kiip.sdk.Modal;
import me.kiip.sdk.Notification;
import me.kiip.sdk.Poptart;

import com.dbychkov.domain.Flashcard;
import com.dbychkov.words.R;
import com.dbychkov.words.adapter.CardPagerAdapter;
import com.dbychkov.words.anim.ZoomOutPageTransformer;
import com.dbychkov.words.app.App;
import com.dbychkov.words.dagger.component.ActivityComponent;
import com.dbychkov.words.fragment.CardContainerFragment;
import com.dbychkov.words.presentation.StudyFlashcardsActivityPresenter;
import com.dbychkov.words.view.StudyFlashcardsView;
import com.dbychkov.words.widgets.ViewPagerCustomDuration;

import javax.inject.Inject;
import java.util.List;

/**
 * Study session activity
 */
public class StudyFlashcardsActivity extends BaseActivity implements StudyFlashcardsView,
        Modal.OnShowListener, Modal.OnDismissListener, DialogInterface.OnShowListener, Notification.OnShowListener, Notification.OnClickListener, Notification.OnDismissListener, DialogInterface.OnDismissListener {

    public static final String EXTRA_LESSON_ID = "lessonId";

    @Inject
    StudyFlashcardsActivityPresenter presenter;

    @Bind(R.id.knowButton)
    ImageView knowButton;

    @Bind(R.id.dontKnowButton)
    ImageView dontKnowButton;

    @Bind(R.id.view_pager)
    ViewPagerCustomDuration viewPager;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.flashcards_activity_title)
    String title;

    @BindString(R.string.flashcards_activity_lesson_ended_title)
    String lessonEndedTitle;

    @BindString(R.string.flashcards_activity_lesson_ended_text)
    String lessonEndedText;

    @BindString(R.string.flashcards_activity_words_learnt_title)
    String wordsLearntTitle;

    @BindString(R.string.flashcards_activity_words_learnt_text)
    String wordsLearntText;

    @BindColor(R.color.know_button_color)
    int knowButtonColor;

    @BindColor(R.color.dont_know_button_color)
    int dontKnowButtonColor;

    @Bind(R.id.main_container)
    RelativeLayout relativeLayout;

    private long lessonId;

    private CardPagerAdapter adapter;

    public static Intent createIntent(Context context, Long lessonId) {
        Intent intent = new Intent(context, StudyFlashcardsActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_cards);
        ButterKnife.bind(this);
        initExtra();
        initToolbar();
        initButtons();
        initPresenter();

        // Listen for poptart events
        getKiipHelper().getKiipFragment().setOnShowListener(this);
        getKiipHelper().getKiipFragment().setOnDismissListener(this);
    }

    private void initExtra() {
        lessonId = getIntent().getLongExtra(FlashcardsActivity.EXTRA_LESSON_ID, -1L);
    }

    private void initPresenter() {
        presenter.setView(this);
        presenter.initialize(lessonId);
    }

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_100, R.anim.slide_out_right_100);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ab.setTitle(title);
        } catch (Exception ex) {
        }
    }

    private void initButtons() {
        knowButton.setColorFilter(knowButtonColor);
        dontKnowButton.setColorFilter(dontKnowButtonColor);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showLessonEndedDialog() {
        try {
            final String momentId = "hello_world";
            Double momentValue = 10.0;
            // Delegate to be called after Kiip#saveMoment is complete
            Kiip.Callback cb = new Kiip.Callback() {
                @Override
                public void onFailed(Kiip kiip, Exception exception) {
                    Log.e("tag", "Failed to save moment " + momentId, exception);

                    showError(exception);
                }

                @Override
                public void onFinished(Kiip kiip, Poptart poptart) {
                    Log.d("tag", "Moment saved poptart=" + poptart);
                    // WARNING: poptart may be null if no reward was given, but
                    // KiipHelper#showPoptart checks for this
                    showPoptart(poptart);
                }
            };

            if (momentValue == null) {
                Kiip.getInstance().saveMoment(momentId, cb);
            } else {
                Kiip.getInstance().saveMoment(momentId, momentValue.doubleValue(), cb);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getBaseContext(), "Invalid moment_value", Toast.LENGTH_LONG).show();
        }
        buildDialog(lessonEndedText, lessonEndedTitle);
    }

    @Override
    public void showAllWordsLearntDialog() {
        buildDialog(wordsLearntText, wordsLearntTitle);
    }

    private void buildDialog(String message, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void renderFlashcards(List<Flashcard> wordsFromLesson) {
        adapter = new CardPagerAdapter(getFragmentManager(), wordsFromLesson);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setAdapter(adapter);
        viewPager.setScrollDurationFactor(3);
    }

    public Fragment findFragmentByPosition(int position) {
        return getFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":"
                + adapter.getItemId(position));
    }

    @Override
    public boolean showCardBack(int position) {
        CardContainerFragment cardContainerFragment = ((CardContainerFragment) findFragmentByPosition(position));
        if (!cardContainerFragment.isFlipped()) {
            cardContainerFragment.flipCard();
            return true;
        }
        return false;
    }

    @OnClick(R.id.knowButton)
    public void onKnowButtonClicked() {
        presenter.knowWordButtonPressed();
    }

    @OnClick(R.id.dontKnowButton)
    public void onDontKnowButtonClicked() {
        presenter.dontKnowWordButtonPressed();
    }

    @Override
    public void showFlashcard(int flashCardNumber) {
        viewPager.setCurrentItem(flashCardNumber);
    }

    @Override
    public void onDismiss(Modal modal) {
        Log.d("tag", "Modal#onDismiss");
    }

    @Override
    public void onShow(Modal modal) {
        Log.d("tag", "Modal#onShow");
    }

    @Override
    public void onShow(DialogInterface dialog) {
        Log.d("tag", "Poptart#onShow");

        final Poptart poptart = (Poptart)dialog;
        final Notification notification = poptart.getNotification();
        final Modal modal = poptart.getModal();

        // Listen for notification events
        if (notification != null) {
            notification.setOnShowListener(this);
            notification.setOnClickListener(this);
            notification.setOnDismissListener(this);
        }

        // Listen for modal events
        if (modal != null) {
            modal.setOnShowListener(this);
            modal.setOnDismissListener(this);
        }
    }

    @Override
    public void onShow(Notification notification) {
        Log.d("tag", "Notification#onShow");
    }

    @Override
    public void onClick(Notification notification) {
        Log.d("tag", "Notification#onClick");
    }

    @Override
    public void onDismiss(Notification notification) {
        Log.d("tag", "Notification#onDismiss");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d("tag", "Poptart#onDismiss");
    }
}

