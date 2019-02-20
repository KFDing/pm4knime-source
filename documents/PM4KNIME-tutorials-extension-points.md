Extension Points On PM4KNIME
====
There are two extension points on KNIME to Eclipse, `org.knime.workbench.repository.categories` 
and `org.knime.workbench.repository.nodes` .  
In context of ProM for process mining, we have categories 
* Reader
* Writer
* Discovery
* Conformance Checking

Nodes we have implemented :
* Discovery 
    * Alpha Miner
  * Inductiver Miner
* Reader & Writer
    * Event Log Reader & Writer
    * Petri Net Reader & Writer
    * Process Tree Reader & Writer

* Conformance Checking


## Development Guide
### NodeDialog
1. Tab
Node dialog is organized into tabs, and components can be put into tabs.  
New tabs are placed right and behind already existing tabs. The specified title must be unique. 
Additional tabs can be created by calling createNewTab(String). 
The specified string is displayed as title of the new tab. Components added after this call 
will be placed into the new tab.
2. Group
Use default method to create group in a tab, the components added later before the next group belong 
to this group. The layout of components can be changed by setHorizontalPlacement().
```
createNewGroup("Your choice");
setHorizontalPlacement(true);
addDialogComponent(new DialogComponentStringSelection(
      selection, "Select one:", TestNodeModel.SELECTION));
```
3. Dialog Components
Each dialog component needs a SettingsModel. This setting model holds the actual value of the component 
and does the storing, loading and validation.   
* When you instantiate the component you must provide a settings model.   
* when you want to load the entered value in your NodeModel, you use an instance of that same SettingsModel.  

