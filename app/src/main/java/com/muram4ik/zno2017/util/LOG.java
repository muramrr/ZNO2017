package com.muram4ik.zno2017.util;

/**
 * Created by Anderson on 21.09.2016.
 */

import android.util.Log;


public final class LOG
{

    private static String TAG = "com.muram4ik.zno2017.util";
    public static boolean DEBUG = true;

    public static void i (String tag, String msg)
    {
        Log.i(TAG + (tag != null && !tag.equals("") ? "_" + tag : ""), msg);
    }

    public static void d (String msg)
    {
        d(null, msg);
    }

    public static void d (String tag, String msg)
    {
        if (DEBUG) Log.d(TAG + (tag != null && !tag.equals("") ? "_" + tag : ""), msg);
    }

    public static void e (String msg)
    {
        e(null, msg);
    }

    public static void e (String tag, String msg)
    {
        if (DEBUG) Log.e(TAG + (tag != null && !tag.equals("") ? "_" + tag : ""), msg);
    }
}