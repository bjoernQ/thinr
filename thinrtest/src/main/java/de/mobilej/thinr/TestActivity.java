/*
 *    Copyright (C) 2016 Björn Quentin
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.mobilej.thinr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import de.mobilej.thinrtest.R;

public class TestActivity extends Activity {

    public static int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.enableDefaults();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        if (savedInstanceState != null) {
            ((TextView) findViewById(R.id.text)).setText(savedInstanceState.getString("text"));
            ((TextView) findViewById(R.id.text2)).setText(savedInstanceState.getString("text2"));
        }


        Button button3 = (Button) findViewById(R.id.button3);
        if (button3 != null) {
            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count = 1;
                }
            });
        }


        Button button = (Button) findViewById(R.id.button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean started = Thinr.task(getApplicationContext(), "myTask", TestActivity.class, String.class)
                            .onMain((target2, param4) -> {
                                ((TextView) target2.findViewById(R.id.text)).setText("start");
                                ((TextView) target2.findViewById(R.id.text2)).setText("start");
                                return param4;
                            })
                            .inBackground(String.class, TestActivity::doInBackground)
                            .inBackground(String.class, (appCtx, param, flowCtrl) -> param + "Lambda")
                            .onMain(String.class, TestActivity::doThisOnMain)
                            .inBackground(String.class, (appCtx1, param1, flowCtrl) -> {
                                for (int i = 0; i < 10; i++) {
                                    SystemClock.sleep(100);
                                    System.out.println("Cancelled=" + flowCtrl.isCancelled());
                                }
                                return param1 + "abc";
                            })
                            .endsOnMain((target, param2) -> {
                                System.out.println("doThisOnMain2");
                                ((TextView) target.findViewById(R.id.text2)).setText(param2);
                            })
                            .onCancel((target1, param3) -> {
                                ((TextView) target1.findViewById(R.id.text)).setText("cancel");
                                ((TextView) target1.findViewById(R.id.text2)).setText("cancel");
                            })
                            .execute("Hello!" + (count++), "component");

                    if (!started) {
                        Toast.makeText(TestActivity.this, "Already running", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        Button button2 = (Button) findViewById(R.id.button2);
        if (button2 != null) {
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thinr.cancel("myTask", "component");
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("text", "" + ((TextView) findViewById(R.id.text)).getText());
        outState.putString("text2", "" + ((TextView) findViewById(R.id.text2)).getText());
    }

    @Override
    protected void onPause() {
        Thinr.onPause("component");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        if (checkBox != null) {
            if (checkBox.isChecked()) {
                Thinr.cancel("myTask", "component");
            }
        }

        Thinr.onResume("component", this);
    }

    String x = "XXXX";

    private String doThisOnMain(String param) {
        System.out.println("doThisOnMain:" + param);

        TextView tv = (TextView) findViewById(R.id.text);
        if (tv != null) {
            tv.setText(param);
        }

        return param + "onMain" + x;
    }

    private static String doInBackground(Context appCtx, String param, FlowControl flowControl) {
        System.out.println("doInBackground:" + param);
        for (int i = 0; i < 35; i++) {
            SystemClock.sleep(100);
            System.out.println("Cancelled=" + flowControl.isCancelled());
        }
        return param + "inBackground";
    }

}
