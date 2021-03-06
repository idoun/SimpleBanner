/*
    Copyright 2016 idoun

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package net.idoun.simplebanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import petrov.kristiyan.colorpicker.ColorPicker;

/**
 * User can change the options of each banner widget in this Activity. Entering point is the widget
 * and its id is the key for customizing it.
 */
public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";

    public static final String ACTION_APPWIDGET_UPDATE = "net.idoun.intent.action.APPWIDGET_UPDATE";
    public static final String EXTRA_WIDGET_ID = "WIDGET_ID";

    public static final String PREF_USER_TEXT = "USER_TEXT_";
    public static final String PREF_USER_TEXT_SIZE = "USER_TEXT_SIZE_";
    public static final String PREF_USER_TEXT_COLOR = "USER_TEXT_COLOR_";

    public static final int DEFAULT_FONT_SIZE = 20;
    public static final int MIN_FONT_SIZE = 10;
    public static final int TOAST_DISPLAY_DELAY = 1500; // millis

    private TextView previewTextView;
    private EditText inputEditText;
    private SeekBar textSizeSeekBar;
    private TextView textSizeTextView;
    private View selectedColorView;

    private SharedPreferences prefs;

    private String widgetText = "";
    private int fontSize;
    private int textColor = Color.WHITE; // default

    /**
     * To prevent accumulating multiple toast.
     */
    private long lastToastDisplayedTime;

    private String widgetKey;
    private String fontSizeKey;
    private String textColorKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        previewTextView = findViewById(R.id.preview_text);
        inputEditText = findViewById(R.id.edit_input);
        textSizeSeekBar = findViewById(R.id.text_size_bar);
        textSizeTextView = findViewById(R.id.display_text_size);
        selectedColorView = findViewById(R.id.selected_color);

        int appWidgetId = getIntent().getIntExtra(EXTRA_WIDGET_ID, -1);
        widgetKey = PREF_USER_TEXT + appWidgetId;
        fontSizeKey = PREF_USER_TEXT_SIZE + appWidgetId;
        textColorKey = PREF_USER_TEXT_COLOR + appWidgetId;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(widgetKey)) {
            widgetText = prefs.getString(widgetKey, "");

            inputEditText.setText(widgetText);
            inputEditText.setSelection(widgetText.length());

            previewTextView.setText(widgetText);
        }
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                previewTextView.setText(inputEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        initBottomButtonArea();

        initFontSizeController();
        if (prefs.contains(textColorKey)) {
            textColor = prefs.getInt(textColorKey, Color.WHITE);
            previewTextView.setTextColor(textColor);
            selectedColorView.setBackgroundColor(textColor);
        }
        selectedColorView.setOnClickListener(this::showColorPicker);
    }

    private void showColorPicker(View v) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                Log.d(TAG, "setOnFastChooseColorListener: " + position + ", color:" + color);
                // display color
                selectedColorView.setBackgroundColor(color);
                previewTextView.setTextColor(color);
            }

            @Override
            public void onCancel() {
            }
        })
                .setColors(R.array.color_picker)
                .setDefaultColorButton(previewTextView.getCurrentTextColor())
                .setTitle(getString(R.string.select_text_color))
                .setColumns(5)
                .show();
    }

    /**
     * Initiate the bottom area that has cancel and save buttons.
     */
    private void initBottomButtonArea() {
        Button cancelButton = findViewById(R.id.cancel_button);
        Button saveButton = findViewById(R.id.save_button);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                Toast.makeText(EditActivity.this, R.string.toast_not_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                String currentText = inputEditText.getText().toString();
                Log.d(TAG, "onClick: " + currentText);
                SharedPreferences.Editor editor = prefs.edit();
                boolean modified = false;

                if (!currentText.isEmpty() && !widgetText.equals(currentText)) {
                    editor.putString(widgetKey, currentText);
                    modified = true;
                }

                int currentTextSize = textSizeSeekBar.getProgress();
                if (currentTextSize != fontSize) {
                    editor.putInt(fontSizeKey, currentTextSize);
                    modified = true;
                }

                int currentTextColor = previewTextView.getCurrentTextColor();
                if (currentTextColor != textColor) {
                    editor.putInt(textColorKey, currentTextColor);
                    modified = true;
                }

                Context context = EditActivity.this;
                if (modified) {
                    editor.apply();
                    updateMyWidgets(context);
                    Toast.makeText(context, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.toast_not_saved, Toast.LENGTH_SHORT).show();
                }

                finish();
            });
        }
    }

    /**
     * Initiate the views to control the font size.
     */
    private void initFontSizeController() {
        fontSize = DEFAULT_FONT_SIZE;
        if (prefs.contains(fontSizeKey)) {
            fontSize = prefs.getInt(fontSizeKey, fontSize);
        }

        textSizeSeekBar.setProgress(fontSize);
        textSizeTextView.setText(String.format(Locale.getDefault(), "%d", fontSize));
        previewTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress < MIN_FONT_SIZE) {
                    if ((System.currentTimeMillis() - lastToastDisplayedTime) > TOAST_DISPLAY_DELAY) {
                        Toast.makeText(EditActivity.this, R.string.toast_font_size_min,
                                Toast.LENGTH_SHORT).show();
                        lastToastDisplayedTime = System.currentTimeMillis();
                    }

                    textSizeSeekBar.setProgress(MIN_FONT_SIZE);
                    textSizeTextView.setText(String.format(Locale.getDefault(), "%d", MIN_FONT_SIZE));
                    previewTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MIN_FONT_SIZE);
                    return;
                }
                textSizeTextView.setText(String.format(Locale.getDefault(), "%d", progress));
                previewTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Sending an intent to simple banner widget.
     *
     * @param context Context to send an intent.
     */
    private void updateMyWidgets(Context context) {
        Intent updateIntent = new Intent(this, SimpleBannerAppWidgetProvider.class);
        updateIntent.setAction(ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(updateIntent);
    }
}
