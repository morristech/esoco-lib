//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.app;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of the {@link CommandLine} class.
 *
 * @author eso
 */
public class CommandLineTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test assignment only CommandLine(String[], String...)
	 */
	@Test
	public void testAssignmentCommandLine()
	{
		String[]    args    = new String[] { "-val=test" };
		String[]    options = new String[] { "val=" };
		CommandLine cl	    = new CommandLine(args, options);

		assertEquals("test", cl.getOption("val"));

		try
		{
			args = new String[] { "-val" };
			cl   = new CommandLine(args, options);

			assertTrue("Mandatory option value missing", false);
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
	}

	/***************************************
	 * Test CommandLine(String[])
	 */
	@Test
	public void testCommandLine()
	{
		String[]    args = "-a_/b_-t1=123_-t2='ok ok'".split("_");
		CommandLine cl   = new CommandLine(args);

		assertOptions(cl);
	}

	/***************************************
	 * Test CommandLine(String[], String...)
	 */
	@Test
	public void testCommandLineWithOptions()
	{
		String[]    args    =
			"-a_/b_/t1=replaced_-t1=123_-t2='ok ok'_-T2=xy".split("_");
		String[]    options = new String[] { "a", "b", "t1=", "t2=", "T2=" };
		CommandLine cl	    = new CommandLine(args, options);

		assertOptions(cl);
		assertEquals("xy", cl.getOption("T2"));

		try
		{
			args = "-a_-b_-c".split("_");
			cl   = new CommandLine(args, options);

			assertTrue("Command line contains illegal argument c", false);
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}

		try
		{
			args = "-t1=".split("_");
			cl   = new CommandLine(args, options);

			assertTrue("missing option value", false);
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}

		try
		{
			args = "-t2".split("_");
			cl   = new CommandLine(args, options);

			assertTrue("missing option value", false);
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
	}

	/***************************************
	 * Test CommandLine(String[], Pattern)
	 */
	@Test
	public void testCommandLineWithPattern()
	{
		String[]    args = "--a_-b_-t1:=123_--t2:='ok ok'".split("_");
		Pattern     p    =
			CommandLine.createPattern("-{1,2}", ":=", "a", "b", "t1:=", "t2:=");
		CommandLine cl   = new CommandLine(args, p);

		assertOptions(cl);

		try
		{
			args = "--a_-b_-c".split("_");
			cl   = new CommandLine(args, p);

			assertTrue("Command line contains illegal argument c", false);
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
	}

	/***************************************
	 * Asserts the contents of a command line.
	 *
	 * @param cl The command line
	 */
	@SuppressWarnings("boxing")
	private void assertOptions(CommandLine cl)
	{
		assertTrue(cl.hasOption("a"));
		assertTrue(cl.hasOption("b"));
		assertFalse(cl.hasOption("c"));
		assertEquals(true, cl.getOption("a"));
		assertEquals(123, cl.getOption("t1"));
		assertEquals("ok ok", cl.getOption("t2"));
	}
}
