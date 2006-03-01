package com.dwipal;

import java.util.*;

public class DwSnmpMibTreeHash {
	
	Hashtable treeHash;
	
	DwSnmpMibTreeHash() {
		treeHash=new Hashtable();
	}
	
	public Object  get(String key) {
		return (treeHash.get(key));
	}
	
	public void put(String key,Object value) {
		treeHash.put(key,value);
	}
	
	public boolean containsKey(String key) {
		return (treeHash.containsKey(key));
	}
	
	public Enumeration elements() {
		return (treeHash.elements());
	}
}


