package com.dwipal; 

import java.awt.*;
/** This class implements the "DoEvents" function, which allows
 * the event queue to perform its events.
 */		
   
public class DwEvtQueue extends java.awt.EventQueue { 
	
/** Create a new instance of the class
 */
public DwEvtQueue() {
}

/** execute the event queue
 */
public void doEvents() 
{ 
Toolkit toolKit = Toolkit.getDefaultToolkit(); 
EventQueue evtQueue = toolKit.getSystemEventQueue(); 

// loop whilst there are events to process 
while (evtQueue.peekEvent() != null) 
{ 
try 
{ 
// if there are then get the event 
AWTEvent evt = evtQueue.getNextEvent(); 
// and dispatch it 
super.dispatchEvent(evt); 
} 
catch (java.lang.InterruptedException e) 
{ 
// if we get an exception in getNextEvent() 
// do nothing 
; 
} 
} 
} 
} 

