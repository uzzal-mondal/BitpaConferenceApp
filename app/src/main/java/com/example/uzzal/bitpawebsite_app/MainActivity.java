package com.example.uzzal.bitpawebsite_app;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.github.kobakei.materialfabspeeddial.FabSpeedDial;
import io.github.kobakei.materialfabspeeddial.FabSpeedDialMenu;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String webAddress = "https://bitpa.org.bd/";
    WebView webView;
    FrameLayout frameLayout;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fabi = (FloatingActionButton) findViewById(R.id.fab_button);
        fabi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


         // handle downloading for this external storage permission.


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){

                String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,1);
            }
        }




        webView = (WebView) findViewById(R.id.webVie_id);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout_id);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_id);


        webView.setWebViewClient(new HelpClient());
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                setTitle("Loading....");
                if(newProgress == 100){

                    frameLayout.setVisibility(View.GONE); //** hide progressbar when page is load.
                    setTitle(view.getTitle()); //** get and setTitle and open page.
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);  // enable java script..

         //download handle

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie",cookies);
                request.addRequestHeader("User-Agent",userAgent);
                request.setDescription("Download File");  //set Description of when notification when download starts
                request.setTitle(URLUtil.guessFileName(url,contentDisposition,mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                //set default download directory
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(url,contentDisposition,mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });

        //** adding about app Activity



        // ** check Internet connection..!

        if(haveNetworkConnection()){
            webView.loadUrl(webAddress);
        }else {

            Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
        }
        progressBar.setProgress(0);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //lets add a fab menu to copy/share link of opend webpage
        // handle fab menu and item clicks

        FabSpeedDialMenu menu = new FabSpeedDialMenu(this);
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab);

        fabSpeedDial.addOnMenuItemClickListener(new FabSpeedDial.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(FloatingActionButton fab, TextView textView, int itemId) {
               //* handle item clicks for copy

                if(itemId==R.id.copyIt){
                    String s = webView.getUrl();  // get url opend page to copy
                    ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cb.setText(s);
                    Toast.makeText(MainActivity.this,"Link Coppied...",Toast.LENGTH_SHORT).show();
                }

                if(itemId==R.id.shareIt){

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plane");
                    String shareBody = webView.getUrl();  //this call of shareBody with sharring item
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"subject here");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent,"share via"));
                }

            }
        });

      registerForContextMenu(webView);

    }


    @Override
    protected void onStart() {
        super.onStart();
        //handle and open the url webview

        try {
            Intent intent = getIntent();
            Uri data = intent.getData();
            webView.loadUrl(data.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //using context to download image and long click


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

       final WebView.HitTestResult webViewhitestResult = webView.getHitTestResult();

       if(webViewhitestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
               webViewhitestResult.getType() == webViewhitestResult.SRC_IMAGE_ANCHOR_TYPE)
       {
           menu.setHeaderTitle("Download");
           menu.setHeaderIcon(R.drawable.ic_down);

           menu.add(0,1,0,"save - Download Image")
                   .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                       @Override
                       public boolean onMenuItemClick(MenuItem item) {

                          String downloadImageUrl = webViewhitestResult.getExtra();

                          if(URLUtil.isValidUrl(downloadImageUrl)){

                                //*handle downloading
                              DownloadManager.Request  request = new DownloadManager.Request(Uri.parse(downloadImageUrl));
                              request.allowScanningByMediaScanner();
                                //*show Notification
                              request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);

                              DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                              downloadManager.enqueue(request);

                               //* toast
                              Toast.makeText(MainActivity.this, "downloading......", Toast.LENGTH_SHORT).show();

                          }else {

                              Toast.makeText(MainActivity.this, "Sorry something wrong.....check internet", Toast.LENGTH_SHORT).show();
                          }


                          return false;
                       }
                   });

       }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
             //start activity when click
            startActivity(new Intent(MainActivity.this,AboutActivity.class));


            return true;
        }

        if (id == R.id.action_refresh) {

            //reload the eurl...
           webView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.

        int id = item.getItemId();


        if (id == R.id.androidId_home) {

         webView.loadUrl(webAddress);  //place home url here
            // Handle the camera action
        } else if (id == R.id.androidId_getTicket) {
            webView.loadUrl("https://bitpa.org.bd/product/ticket/");


        } else if (id == R.id.androidId_downTicket) {
            webView.loadUrl("https://bitpa.org.bd/my-account/downloads/");


        } else if (id == R.id.androidId_conference) {
            webView.loadUrl("https://bitpa.org.bd/conference/");


        } else if (id == R.id.androidId_videoGalery) {
            webView.loadUrl("https://www.youtube.com/watch?v=BJW16yMuuCc&list=PLVDwIY0B4TmLNdD4ceR4PbGuYqS9TXvA5");



        } else if (id == R.id.androidId_download) {

            webView.loadUrl("https://bitpa.org.bd/");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

     private class HelpClient extends WebViewClient{


         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {

       view.loadUrl(url);
       frameLayout.setVisibility(View.VISIBLE);
       return true;

         }
     }

     private boolean haveNetworkConnection(){

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

         ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

        for(NetworkInfo ni : networkInfos){

            if(ni.getTypeName().equalsIgnoreCase("WIFI"))
                if(ni.isConnected())
                    haveConnectedWifi = true;

            if(ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if(ni.isConnected())
                    haveConnectedMobile = true;
        }

        return haveConnectedWifi || haveConnectedMobile;
     }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //** check if the event was the back button and if there's history.

        if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){

            webView.goBack();
            return true;
        }
             // if it was not the back or there is no webpage history, bubble up to the default system behaviour.
        return super.onKeyDown(keyCode, event);
    }
}
