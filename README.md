# NStack

Add explanation here

## Usage

in Application Class:
NStack.init(context, applicationId, restApiKey);
NStack.getStack().enableDebug();
NStack.getStack().translationClass(Translation.class);
        
In Activity, Fragment or ViewGroup (or any class with views as fields/children)

@BindView(R.id.text_view)

@Translate("section.key")

TextView textView;

and in onCreate/onStart or onResume:
NStack.getStack().translate(this);

<h2>Download</h2>

Gradle: 

    dependencies {
      compile 'dk.nodes.nstack:nstack:0.76'
    }

### Setup from AndroidManifest.xml
If using version >= 0.76 NStack can also be configured from the manifest. Simply add meta values to the application object like this:

```
<meta-data android:name="dk.nodes.nstack.appId" android:value="appid" />
        <meta-data android:name="dk.nodes.nstack.apiKey" android:value="apikey" />
```

## Version control simulator
Inorder to test version control easily version 0.76 and above makes it possible to enable a version control simulator.
This should ONLY be added to debug builds. To use make a file called AndroidManifest.xml and place it under /src/debug. File content should look like this:

```
<?xml version="1.0" encoding="utf-8"?>
<manifest package="dk.nodes.alkafuel"
          xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">

    <application>
        <activity
            android:name="dk.nodes.nstack.NStackDebugActivity"
            android:icon="@drawable/ic_launcher"
            android:label="APPNAME Debug menu"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="stateHidden|adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

What this does is add another launcher activity but only in debug builds. This result in the app getting and extra icon with label being the one in android:label. This activity can be started independently of the main app and version control simulation can be enabled disabled. Changes persist til app is force quitted either by the user or the Android runtime.

## Check Example project to see all of the uses
