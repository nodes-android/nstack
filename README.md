# NStack

Add explanation here

## Usage

in Application Class:

```NStack.init(context, applicationId, restApiKey);
   NStack.getStack().enableDebug();
   NStack.getStack().translationClass(Translation.class);```
        
In Activity, Fragment or ViewGroup (or any class with views as fields/children)

@BindView(R.id.text_view)

@Translate("section.key")

TextView textView;

and in onCreate/onStart or onResume

NStack.getStack().translate(this);

<h2>Download</h2>

Gradle: 

    dependencies {
      compile 'dk.nodes.nstack:nstack:0.62'
    }

## Check Example project to see all of the uses
