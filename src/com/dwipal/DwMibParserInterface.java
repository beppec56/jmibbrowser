package com.dwipal;

public interface DwMibParserInterface
{
	void newMibParseToken(DwSnmpMibRecord rec);
	void parseMibError(String s);
}
