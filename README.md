# Hello TouchApp-Android!

This is a port of [HelloTouchApp](https://github.com/jchris/HelloTouchApp) to Android. It illustrates some of the differences between the iOS and Android TouchDB implementations.

[TouchDB](https://github.com/couchbaselabs/TouchDB-Android/) is a lightweight mobile database that syncs with CouchDB and offers a similar (but not identical) REST-style API.

A [CouchApp](http://couchapp.org) is an HTML5 app served directly from CouchDB. 
A TouchApp is an HTML5 app served from TouchDB. But there really does not need to be any difference between the two. One may push a couchapp to TouchDB.

## How is this Android port different from the iOS version?

This version uses REST syntax for interaction with the TouchDB in order to simulate how Android developers might use it. 

The iOS version uses CouchCocoa framework, which is a medium-level Objective-C API.

### Creating the database

#### Android

REST:

````java
conn = sendRequest(server, "PUT", "/touchapp", null, null);
````

#### iOS

CouchCocoa framework:

````
CouchDatabase *database = [server databaseNamed: @"default"];
````

### Creating the document and attachment

#### Android

The Android version puts a document that already has the attachment:

````java
Map<String,Object> doc1 = new HashMap<String,Object>();
doc1.put("foo", "bar");
String base64 = Base64.encodeBytes(htmlString.getBytes());
Map<String,Object> attachment = new HashMap<String,Object>();
attachment.put("content_type", "text/html");
attachment.put("data", base64);
Map<String,Object> attachmentDict = new HashMap<String,Object>();
attachmentDict.put("index.html", attachment);

doc1.put("_attachments", attachmentDict);
result = (Map<String,Object>)sendBody(server, "PUT", "/touchapp/doc1", doc1);
````

#### IOS

The iOS version puts the document first, then does a revision that adds the attachment.

````
CouchDocument *doc = [database documentWithID:@"hello"];
RESTOperation* op = [doc putProperties:[NSDictionary dictionaryWithObject: @"bar"
                                                                   forKey: @"foo"]];
[op wait]; 
CouchRevision *rev = doc.currentRevision;
CouchAttachment* attach = [rev createAttachmentWithName:@"index.html" type:@"text/html; charset=utf-8"];
op = [attach PUT:[htmlString dataUsingEncoding:NSUTF8StringEncoding]];
NSLog(@"make attachment %@",attach);
````

### Creating the URL

The method that creates the attachment provides a handy unversionedURL field which is used by the app to launch the correct URL; however, 
this must be constructed manually by the Android version.

#### Android


````java
String ipAddress = "0.0.0.0";
Log.d(TAG, ipAddress);
String host = ipAddress;
int port = 8888;
String urlPrefix = "http://" + host + ":" + Integer.toString(port) + "/";
String attachURL = urlPrefix + "touchapp/doc1/index.html";
 ````
 
#### IOS

````
NSURL *attachURL = attach.unversionedURL;
NSLog(@"attachURL %@",attachURL);
[self.webView loadRequest:[NSURLRequest requestWithURL:attachURL]];
````

## How do I run it?

Clone it, open in Eclipse, and hit run. (Tested on Galaxy Nexus and Galaxy Tab 7")

## Can this be easier?

If you already have a CouchApp, you could simply do "couchapp push mob" to get the couchapp into the TouchDB. 

## Are there other examples?

Check out [Android-Coconut-TouchDB](https://github.com/vetula/Android-Coconut-TouchDB), which is a more comprehensive CouchApp. 
The [TouchDB-Android Couchapp example] (http://vetula.blogspot.com/2012/03/touchdb-android-couchapp-example.html) has a bit more detail on that example.

