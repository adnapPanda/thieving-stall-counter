package com.thievingstallcounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ThievingStallCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ThievingStallCounterPlugin.class);
		RuneLite.main(args);
	}
}