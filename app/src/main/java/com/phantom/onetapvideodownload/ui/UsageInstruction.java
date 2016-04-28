package com.phantom.onetapvideodownload.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;

public class UsageInstruction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_instruction);

        TextView t2 = (TextView) findViewById(R.id.description);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
