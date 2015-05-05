package com.groupit;

import android.content.Context;

public class ScreenUtils {

    Context con;

    public ScreenUtils(Context con) {
        this.con = con;
    }

    public int getPixels() {
        // 500 * 4 * 0.6
        float density = con.getResources().getDisplayMetrics().density;

        if (density >= 4.0) {
            return 650;
        }
        if (density >= 3.0) {
            return 500;
        }
        if (density >= 2.0) {
            return 450;
        }
        if (density >= 1.5) {
            return 375;
        }
        if (density >= 1.0) {
            return 200;
        }

        return 150;
    }
}
