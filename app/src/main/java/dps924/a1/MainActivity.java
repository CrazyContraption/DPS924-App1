package dps924.a1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.security.Key;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * Handles any inputs made to the calculator, including converting them to a KeyTypes string, and attempts to push them into the Calculator
     * @param view The view that called this action, normally a button in these instances
     */
    public void keyPressed(View view) {
        if (view.getId() == View.NO_ID) return;
        String keyName = view.getResources().getResourceName(view.getId()).replace("dps924.a1:id/", "");
        Calculator.push(keyName);
        Calculator.updateDisplay();
    }

    /**
     * Handles the request to show/hide the advanced options of the Calculator
     * @param view The view that called this action, normally a button in these instances
     */
    public void toggleMode(View view) {
        Calculator.toggleMode((Button)view, (LinearLayout) findViewById(R.id.advancedItems));
    }

    /**
     * Called when the app is starting, useful for binding initial properties, used here to setup the display window.
     * @param savedInstanceState If I'm honest? No clue what this is
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText display = findViewById(R.id.displayPort);
        display.setMovementMethod(new ScrollingMovementMethod());
        Calculator.setDisplay(display);
    }
}