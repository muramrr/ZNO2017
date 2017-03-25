package com.muram4ik.zno2017;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.muram4ik.zno2017.util.IabBroadcastReceiver;
import com.muram4ik.zno2017.util.IabBroadcastReceiver.IabBroadcastListener;
import com.muram4ik.zno2017.util.IabHelper;
import com.muram4ik.zno2017.util.IabHelper.IabAsyncInProgressException;
import com.muram4ik.zno2017.util.IabResult;
import com.muram4ik.zno2017.util.Inventory;
import com.muram4ik.zno2017.util.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IabBroadcastListener
{
    // Does the user have the premium upgrade?
    private static final String APP_PREFERENCES_PREMIUM = "premium";
    //именем файла настроек
    private static final String APP_PREFERENCES = "mysettings";
    private static final String APP_PREFERENCES_SSL = "accepting";
    private SharedPreferences mSettings;
    private boolean mOk;
    private boolean mIsPremium;
    private DrawerLayout drawer;
    private WebView mWebView;
    private AdView mAdView;
    private Toolbar toolbar;
    private InterstitialAd mInterstitialAd;
    // public key из админки Google Play
    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjASB" + "f+OY17jVoVDiwPYjGaGTfWdsJjuuOhi0Tk0SdSRvFh7DJOXDb/oCNCtTdD3TCiAVWFIG97DMd6AFEGdV2a2WFdZ" + "4oE5KuMGkpxsIcHH54rU+cC9QE9KW1hgFD8i4+4IWs6OY9nN1uxrPBzvVDbV4ZmaVJ/3CTNiGgt+JIRw7cPXMFc" + "9TY3cMS0qzTIycfNq4xoj4d4YBjREWzTcmOOmm9ky56Vf1Yn/5KT3V9UScliCPDcq69C9JUkX39NFzIis2CHVt//" + "xBu2tG+L1eHiMPir8wxIiG+b7uCmoisiU9nfGZmNQ6wjm9ebr8VK9JsZ7yaTJXxgN//YYBHZ0dAwIDAQAB";
    //Debug TAG
    private static final String TAG = "zno2017";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    static final String SKU_ADS_DISABLE = "zno2017.noads";
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    private int count_ads = 0;


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //settings
        // грузим настройки
        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        loadData();
        if (mIsPremium != true)
        {
            //баннер
            //test
            //MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
            MobileAds.initialize(getApplicationContext(), "ca-app-pub-8880682166113696/6970362964");
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            //
            //межстраничное
            mInterstitialAd = new InterstitialAd(this);
            //test
            //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            mInterstitialAd.setAdUnitId("ca-app-pub-8880682166113696/8447096169");
            mInterstitialAd.setAdListener(new AdListener()
            {
                @Override
                public void onAdClosed ()
                {
                    requestNewInterstitial();
                }
            });
            requestNewInterstitial();
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSavePassword(true);
        mWebView.setWebViewClient(new SSLTolerentWebViewClient());
        mWebView.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey (View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                    }
                }
                return false;
            }
        });
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.loadUrl("http://testportal.gov.ua/");
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
        {
            public void onIabSetupFinished (IabResult result)
            {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess())
                {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try
                {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e)
                {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    // Слушатель восстановления покупок
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished (IabResult result, Inventory inventory)
        {
            Log.d(TAG, "Query inventory finished.");
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;
            // Is it a failure?
            if (result.isFailure())
            {
                complain("Failed to query inventory: " + result);
                return;
            }
            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_ADS_DISABLE);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Received a broadcast notification that the inventory of items has changed
    @Override
    public void receivedBroadcast ()
    {
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try
        {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e)
        {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    //покупаем премиум без рекламы
    public void purchase_noAds (MenuItem item)
    {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "7468616e6b20796f7520666f72207075726368617365";

        try
        {
            mHelper.launchPurchaseFlow(this, SKU_ADS_DISABLE, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabAsyncInProgressException e)
        {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false);
        }
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload (Purchase p)
    {
        String payload = p.getDeveloperPayload();
        return payload == "7468616e6b20796f7520666f72207075726368617365";
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished (IabResult result, Purchase purchase)
        {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure())
            {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase))
            {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");
            if (purchase.getSku().equals(SKU_ADS_DISABLE))
            {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished (Purchase purchase, IabResult result)
        {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess())
            {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");

                saveData();
                alert("You've purchased NoAds function.");
            } else
            {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

    //межстраничное реклама
    private void requestNewInterstitial ()
    {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void notworkingmethod (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Ви нічого не купували", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Ignore SSL certificate errors
    // SSL Error Tolerant Web View Client
    private class SSLTolerentWebViewClient extends WebViewClient
    {
        public void onReceivedSslError (WebView view, final SslErrorHandler handler, SslError error)
        {
            if (mOk == true) handler.proceed();
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.create();
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError())
                {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";
                alertDialog.setTitle("SSL Certificate Error");
                alertDialog.setMessage(message);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        // Ignore SSL certificate errors
                        handler.proceed();
                        mOk = true;
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putBoolean(APP_PREFERENCES_SSL, mOk);
                        editor.apply();
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        handler.cancel();
                    }
                });
                alertDialog.show();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url)
        {
            view.loadUrl(url);
            if (url.contains(".pdf")) PDFTools.openPDFThroughGoogleDrive(MainActivity.this, url);
            return true;
        }

    }

    @Override
    public void onBackPressed ()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected (MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_main)
        {
            mWebView.loadUrl("http://testportal.gov.ua/");
            drawer.closeDrawers();
            counter_ads();
        } else if (id == R.id.nav_infopage)
        {
            mWebView.loadUrl("https://zno.testportal.com.ua/info/login");
            drawer.closeDrawers();
            counter_ads();
        } else if (id == R.id.nav_programsZNO2017)
        {
            mWebView.loadUrl("http://testportal.gov.ua/programs2017/");
            drawer.closeDrawers();
            counter_ads();
        }
        //TODO
        //отдельный метод public void nav_registration_Click(MenuItem item)
        //else if (id == R.id.nav_registration)
        //{

        //}
        else if (id == R.id.nav_trainingZNO)
        {
            mWebView.loadUrl("https://zno.yandex.ua/");
            drawer.closeDrawers();
            counter_ads();
        } else if (id == R.id.nav_support)
        {
            Intent playMarket = new Intent(Intent.ACTION_VIEW);
            playMarket.setData(Uri.parse("market://details?id=com.muram4ik.zno2017"));
            startActivity(playMarket);
        } else if (id == R.id.nav_vstupinfo)
        {
            mWebView.loadUrl("http://vstup.info/");
            drawer.closeDrawers();
            counter_ads();
        } else if (id == R.id.nav_abitpoisk)
        {
            mWebView.loadUrl("https://abit-poisk.org.ua/");
            drawer.closeDrawers();
            counter_ads();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //регистрация еще не началась
    public void nav_registration_Click (MenuItem item)
    {
        mWebView.loadUrl("https://zno.testportal.com.ua/registration");
        drawer.closeDrawers();
        counter_ads();
    }

    //подача документов еще не началась
    public void nav_online_docs (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Подача документів онлайн на данний момент ще не розпочалася", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        counter_ads();
    }

    void complain (String message)
    {
        alert("Error: " + message);
    }

    void alert (String message)
    {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    //при 4 количествах кликах по кнопках вылазит реклама
    public void counter_ads ()
    {
        count_ads++;
        if (count_ads == 4)
        {
            count_ads = 0;
            if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
        }
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        //if (mSettings.contains(APP_PREFERENCES_SSL))
        // Получаем число из настроек
        //mOk = mSettings.getBoolean(APP_PREFERENCES_SSL, true);


    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null)
        {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null)
        {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen (boolean set)
    {
        findViewById(R.id.main).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    public void updateUi () {mAdView.destroy();}

    void saveData ()
    {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_PREMIUM, mIsPremium);
        editor.apply();
        Log.d(TAG, "Saved data: premium = " + String.valueOf(mIsPremium));
    }

    void loadData ()
    {
        if (mSettings.contains(APP_PREFERENCES_SSL))
            mOk = mSettings.getBoolean(APP_PREFERENCES_SSL, true);
        if (mSettings.contains(APP_PREFERENCES_PREMIUM))
            mIsPremium = mSettings.getBoolean(APP_PREFERENCES_PREMIUM, true);
    }
}