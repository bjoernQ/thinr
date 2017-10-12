# Thinr

[![Download](https://api.bintray.com/packages/bjoernq/maven/thinr/images/download.svg) ](https://bintray.com/bjoernq/maven/thinr/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Thinr-green.svg?style=true)](https://android-arsenal.com/details/1/3645) [![Build Status](https://travis-ci.org/bjoernQ/thinr.svg?branch=master)](https://travis-ci.org/bjoernQ/thinr) [![License](https://img.shields.io/badge/license-Apache%202-green.svg?style=flat)](https://github.com/bjoernQ/thinr/blob/master/LICENSE.txt) [![Coverage Status](https://coveralls.io/repos/github/bjoernQ/thinr/badge.svg?branch=master)](https://coveralls.io/github/bjoernQ/thinr?branch=master)

 
## About

Thinr is a replacement for AsyncTask. It is a tiny library that makes things like the following possible in a leak free and configuration change aware way:

```java
Thinr.task(getContext(), "getQOTD", MainActivity.class, Void.class)
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
                        .execute(null, "ComponentID");
```

While the above example is not very exciting you might be able to estimate it's value considering it's

- life cycle aware
- leak free

Anything in "inBackground" will be executing on a background thread.

Anything in "onMain" will be executing when - and only when - the component is in a state that is fine for accessing the component.
Imagine that "inBackground" can be any time consuming blocking operation accessing the network and hitting the filesystem.

It's a beautiful replacement for AsyncTask (which it internally uses) when using Java 8.

You can also use it with Java 7 but Java 8 will make your experience much more exciting.

Currently the official support for Java 8 is limited to using Jack (see http://developer.android.com/preview/j8-jack.html ) but for sure the complete toolchain will support Java 8 sooner or later.

Additionally RetroLambda is supported starting with version 0.1.0.

If you still don't know what this will do for you have a look at other approaches making AsyncTask work (right) for you.

Don't get me wrong there is nothing wrong with AsyncTask - it just needs a lot of care to implement it in a leak and error free way.

Unfortunately those implementations add a lot of boiler plate code which made a lot of developers to come up with alternative solutions (which are also not easy to understand and do it right - and most of the time the code will look just ugly, hard to understand and hard to maintain).

If you ever did some non-trivial Android app you should know the pain I*m talking about.

## Objectives

The objectives of this tiny library are:

- easy usage
- easy to understand
- no boilerplate code involved (when using Java 8)
- easy to integrate
- no need to rewrite your whole code base to make use of it / can be introduced step-by-step where it is needed while leaving all the other code untouched
- non invasive (i.e. don't forces you to use a special style of programming)
- beautiful code (when using Java 8)

Additionally while the API is quite easy it comes with a lot of flexibility.

## Get started

The easiest way to get started is to look at the sample app. It's kept small to focus on the usage of Thinr.

## Integrate

This library is available on JCenter [ ![Download](https://api.bintray.com/packages/bjoernq/maven/thinr/images/download.svg) ](https://bintray.com/bjoernq/maven/thinr/_latestVersion)

Add the dependency to your app and make sure to use the jcenter repository:

```groovy
    compile 'de.mobilej:thinr:0.2.5'
```

## Version History

Version|Description|
|-------|-----------|
|0.0.0|initial public release|
|0.1.0|supports RetroLambda, possible to disable runtime checks|
|0.1.1|runtime checks now allow RetroLambda and Jacoco at the same time|
|0.2.0|fixed problem with InstantRun, added a Lint check, added more JavaDoc|
|0.2.1|whitelist serialVersionUID as a safe field for Lambdas|
|0.2.2|whitelist serialVersionUID as a safe field for Lambdas|
|0.2.3|Improved Lint check|
|0.2.4|Improved Lint check (less false positives)|
|0.2.5|You can specify the executor for background operations now|

## License

```
Copyright 2016,2017 Björn Quentin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
