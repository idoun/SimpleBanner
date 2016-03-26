package net.idoun.simplebanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends AppCompatActivity {

    public static final String PREF_USER_TEXT = "USER_TEXT";
    private static final String TAG = "EditActivity";
    public static final String ACTION_APPWIDGET_UPDATE = "net.idoun.intent.action.APPWIDGET_UPDATE";

    private EditText inputEditText;

    private String text = "";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        inputEditText = (EditText)findViewById(R.id.edit_input);
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
        Button saveButton = (Button)findViewById(R.id.save_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = inputEditText.getText().toString();
                Log.d(TAG, "onClick: " + currentText);
                if (!currentText.isEmpty() && !text.equals(currentText)) {
                    prefs.edit().putString(PREF_USER_TEXT, currentText).apply();
                    updateMyWidgets(EditActivity.this);
                    finish();
                }
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(PREF_USER_TEXT)) {
            text = prefs.getString(PREF_USER_TEXT, "");

            inputEditText.setText(text);
        }
    }

    private void updateMyWidgets(Context context) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(updateIntent);
    }
}
