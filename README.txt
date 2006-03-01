SNMP MIB Browser V1.0
---------------------

Please read License.txt before using this utility.

Author: Dwipal Desai (mibbrowser@dwipal.com)

How to Run:
----------

Step 1: Make sure that you have JDK1.3 or later installed 
	on your machine and the BIN directory is in the 
	system path. (You should be able to run "java" 
	from command line).

Step 2: Go to the directory in which you have extracted the
	files, and run "mib.bat"

Step 3: Enjoy !

Notes and Features:
-------------------

* 100% Pure Java based, can run on any OS with JDK1.3 installed.

* Supports SNMP-V1 mibs.

* You can dynamically load new mibs into the existing tree using the
	"Load Mib" button at the bottom-left corner of the browser.

* Standard mibs included with this package.

* To include mib files every time the browser starts, just copy it
	into the "mibs" folder. All files in that folder will be
	automatically loaded.

* Allows Get feature for single node, tables as well as for entire tree.

* Allows Set feature (Only String values).

* Click on "Details" button to get the details about the node selected
	in the tree.

* You can also type in a OID in the OID text field to directly query
	its values.

* Wildcard (*) is supported in OID field, must be at the end of the OID.
	Example, ".1.3.6.1.2.1.1.*" gets the values of System group.

* Range of numbers can be specified (useful for querying tables)
	Example, ".1.3.6.1.2.1.2.2.1.(1-22).(1-n)" gets the details
	for the interfaces.

* "Error Ignore" feature allows to load the mib files with errors in it.
	
* A log file "mibbrowser.log" for all the activities is created in the 
	folder running the browser.


------------------------------------------------------------------------
