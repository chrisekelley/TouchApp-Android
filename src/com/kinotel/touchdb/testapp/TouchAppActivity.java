package com.kinotel.touchdb.testapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.couchbase.touchdb.TDBody;
import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDStatus;
import com.couchbase.touchdb.listener.TDListener;
import com.couchbase.touchdb.router.TDRouter;
import com.couchbase.touchdb.router.TDURLConnection;
import com.couchbase.touchdb.support.Base64;

public class TouchAppActivity extends Activity {

    public static final String TAG = "TouchAppActivity";
    private TDListener listener;
    private WebView webView;

    /** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String filesDir = getFilesDir().getAbsolutePath();
        TDServer server = null;
        try {
            server = new TDServer(filesDir);
            listener = new TDListener(server, 8888);
            listener.start();
        } catch (IOException e) {
            Log.e(TAG, "Unable to create TDServer", e);
        }
        
        String path = filesDir + "/touchapp.sqlite3";
        File destination = new File(path);
        Log.d(TAG, "Checking for touchdb at " + path);
        if (!destination.exists()) {
        	TDDatabase db = null;
        	try {
        		//TDDatabase db = server.getDatabaseNamed("default");
        		db = TDDatabase.createEmptyDBAtPath(path);
        	} catch (Exception e) {
        		Log.e(TAG, "TouchDB fail ", e);
        	}

        	//TDBlobStore attachments = db.getAttachments();

        	Map<String,Object> result = null;
        	TDURLConnection conn = null;

        	conn = sendRequest(server, "PUT", "/touchapp", null, null);
        	result = (Map<String, Object>) parseJSONResponse(conn);

        	//String dbName = db.getName();

        	TDStatus status = new TDStatus();
        	String htmlString = null;
        	try {
        		htmlString = readAsset(getAssets(), "index.html");
        	} catch (IOException e) {
        		Log.e(TAG, "Error: ", e);
        	}
        	Map<String,Object> doc1 = new HashMap<String,Object>();
        	doc1.put("foo", "bar");
        	//Map<String,Object> result = (Map<String,Object>)sendBody(server, "PUT", "/db/hello", doc1, TDStatus.CREATED, null);
        	//        TDRevision rev = db.putRevision(new TDRevision(doc1), null, false, status);
        	//        String docId = rev.getDocId();
        	//        Log.d(TAG, "make rev " + rev.toString() + " docId: " + docId);

        	//result = (Map<String,Object>)sendBody(server, "PUT", "/touchapp/doc1", doc1);
        	//String revID = (String)result.get("rev");
        	//        status = db.insertAttachmentForSequenceWithNameAndType(htmlString.getBytes(), rev.getSequence(), "index.html", "text/html", rev.getGeneration());
        	//        Log.d(TAG, "make attachment status: " + status.toString());
        	//        TDAttachment attachment = db.getAttachmentForSequence(rev.getSequence(), "index.html", status);

        	String base64 = Base64.encodeBytes(htmlString.getBytes());
        	Map<String,Object> attachment = new HashMap<String,Object>();
        	attachment.put("content_type", "text/html");
        	attachment.put("data", base64);
        	Map<String,Object> attachmentDict = new HashMap<String,Object>();
        	attachmentDict.put("index.html", attachment);
        	//		Map<String,Object> properties = new HashMap<String,Object>();
        	//		properties.put("foo", 1);
        	//		properties.put("bar", false);
        	doc1.put("_attachments", attachmentDict);
        	//doc1.put("_rev", revID);
        	result = (Map<String,Object>)sendBody(server, "PUT", "/touchapp/doc1", doc1);
        	if (result != null) {
        		String revID2 = (String)result.get("rev");
        	}
        	// Get the revision:
        	//        TDRevision gotRev = db.getDocumentWithIDAndRev(rev.getDocId(), rev.getRevId(), EnumSet.noneOf(TDDatabase.TDContentOptions.class));
        	//        Map<String,Object> gotAttachmentDict = (Map<String,Object>)gotRev.getProperties().get("_attachments");
        	//
        	//String attachURL = dbName + "/" + docId + "/index.html";

        }
        
        String ipAddress = "0.0.0.0";
        Log.d(TAG, ipAddress);
		String host = ipAddress;
		int port = 8888;
        String urlPrefix = "http://" + host + ":" + Integer.toString(port) + "/";
        String attachURL = urlPrefix + "touchapp/doc1/index.html";
        webView = new WebView(TouchAppActivity.this);
        webView.setWebViewClient(new CustomWebViewClient());		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setDomStorageEnabled(true);

		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		webView.requestFocus(View.FOCUS_DOWN);
	    webView.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                case MotionEvent.ACTION_UP:
	                    if (!v.hasFocus()) {
	                        v.requestFocus();
	                    }
	                    break;
	            }
	            return false;
	        }
	    });
        setContentView(R.layout.main);
		setContentView(webView);
		webView.loadUrl(attachURL);
    }
    
	private class CustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("tel:")) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
				startActivity(intent);
			} else if (url.startsWith("http:") || url.startsWith("https:")) {
				view.loadUrl(url);
			}
			return true;
		}
	}
	
	public static String readAsset(AssetManager assets, String path) throws IOException {
		InputStream is = assets.open(path);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		return new String(buffer);
	}
	
    public static Object sendBody(TDServer server, String method, String path, Object bodyObj) {
        Log.v(TAG, "bodyObj --> " + bodyObj);
        TDURLConnection conn = sendRequest(server, method, path, null, bodyObj);
        Object result = parseJSONResponse(conn);
        Log.v(TAG, String.format("%s %s --> %d", method, path, conn.getResponseCode()));
        Log.v(TAG, "result --> " + result);
        return result;
    }
    
    static TDURLConnection sendRequest(TDServer server, String method, String path, Map<String,String> headers, Object bodyObj) {
        try {
            URL url = new URL("touchdb://" + path);
            TDURLConnection conn = (TDURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            if(headers != null) {
                for (String header : headers.keySet()) {
                    conn.setRequestProperty(header, headers.get(header));
                }
            }
            Map<String, List<String>> allProperties = conn.getRequestProperties();
            if(bodyObj != null) {
                conn.setDoInput(true);
                ObjectMapper mapper = new ObjectMapper();
                OutputStream os = conn.getOutputStream();
                os.write(mapper.writeValueAsBytes(bodyObj));
            	Log.v(TAG, "sendRequest - bodyObj: " + bodyObj);
            }

            TDRouter router = new TDRouter(server, conn);
            router.start();
            return conn;
        } catch (MalformedURLException e) {
        	Log.e(TAG, "Error:", e);
        } catch(IOException e) {
        	Log.e(TAG, "Error:", e);
        }
        return null;
    }
    
    static Object parseJSONResponse(TDURLConnection conn) {
        Object result = null;
        TDBody responseBody = conn.getResponseBody();
        if(responseBody != null) {
            byte[] json = responseBody.getJson();
            String jsonString = null;
            if(json != null) {
                jsonString = new String(json);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    result = mapper.readValue(jsonString, Object.class);
                } catch (Exception e) {
                	Log.e(TAG, "Error:", e);
                }
            }
        }
        return result;
    }
    
}