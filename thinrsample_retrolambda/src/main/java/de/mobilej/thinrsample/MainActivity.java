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

package de.mobilej.thinrsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mobilej.thinr.Thinr;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    private static final Executor exec = Executors.newFixedThreadPool(10);

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        View button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thinr.task(MainActivity.this, "getQOTD", MainActivity.class, Void.class)
                        .onMain((target, param) -> {
                            target.textView.setText(target.getApplicationContext().getString(R.string.loading_message));
                            return null;
                        })
                        .inBackground(
                                (appCtx, param, flowControl) -> {
                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url("http://api.icndb.com/jokes/random")
                                            .build();

                                    try {
                                        Response response = client.newCall(request).execute();
                                        JSONObject json = new JSONObject(response.body().string());
                                        return json.getJSONObject("value").getString("joke");
                                    } catch (Exception e) {
                                        return null;
                                    }
                                }
                        )
                        .endsOnMain((target, qotd) -> {
                            if (qotd != null) {
                                target.textView.setText(qotd);
                            } else {
                                target.textView.setText(target.getApplicationContext().getString(R.string.error_message));
                            }
                        })
                        .execute(null, "MainActivity", exec);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thinr.onResume("MainActivity", this);
    }

    @Override
    protected void onPause() {
        Thinr.onPause("MainActivity");
        super.onPause();
    }

}
