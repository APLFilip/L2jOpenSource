package com.l2jfrozen.gameserver.script.faenor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.script.ScriptContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.Parser;

/**
 * @author Luis Arias
 */
public abstract class FaenorParser extends Parser
{
	protected static FaenorInterface bridge = FaenorInterface.getInstance();
	protected final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.US);
	
	public final static boolean DEBUG = true;
	
	/*
	 * UTILITY FUNCTIONS
	 */
	public static String attribute(final Node node, final String attributeName)
	{
		return attribute(node, attributeName, null);
	}
	
	public static String element(final Node node, final String elementName)
	{
		return element(node, elementName, null);
	}
	
	public static String attribute(final Node node, final String attributeName, final String defaultValue)
	{
		try
		{
			return node.getAttributes().getNamedItem(attributeName).getNodeValue();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			if (defaultValue != null)
			{
				return defaultValue;
			}
			throw new NullPointerException("FaenorParser: attribute " + e.getMessage());
		}
	}
	
	public static String element(final Node parentNode, final String elementName, final String defaultValue)
	{
		try
		{
			final NodeList list = parentNode.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				final Node node = list.item(i);
				if (node.getNodeName().equalsIgnoreCase(elementName))
				{
					return node.getTextContent();
				}
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		if (defaultValue != null)
		{
			return defaultValue;
		}
		throw new NullPointerException();
		
	}
	
	public static boolean isNodeName(final Node node, final String name)
	{
		return node.getNodeName().equalsIgnoreCase(name);
	}
	
	public static double getPercent(final String percent)
	{
		return Double.parseDouble(percent.split("%")[0]) / 100.0;
	}
	
	protected static int getInt(final String number)
	{
		return Integer.parseInt(number);
	}
	
	protected static double getDouble(final String number)
	{
		return Double.parseDouble(number);
	}
	
	protected static float getFloat(final String number)
	{
		return Float.parseFloat(number);
	}
	
	protected static String getParserName(final String name)
	{
		return "faenor.Faenor" + name + "Parser";
	}
	
	@Override
	public abstract void parseScript(Node node, ScriptContext context);
}
