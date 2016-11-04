/*
* Copyright 2016 Angela Sanchez
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
* */

package com.example.xyzreader.remote;

import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {
            url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json");
        } catch (Exception e) {
            e.getMessage();
        }

        BASE_URL = url;
    }
}
