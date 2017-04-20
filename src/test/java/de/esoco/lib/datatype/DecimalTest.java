//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.datatype;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.function.Consumer;

import org.junit.Test;

import static de.esoco.lib.datatype.Decimal.decimal;
import static de.esoco.lib.datatype.Decimal.scaleFactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of {@link Decimal} class.
 *
 * @author eso
 */
public class DecimalTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testAdd()
	{
	}

	/***************************************
	 * Test of {@link Decimal#calcScale(long)}
	 */
	@Test
	public void testCalcScale()
	{
		long nTest  = Long.MAX_VALUE;
		int  nScale = 18;

		while ((nTest /= 10) > 0)
		{
			assertEquals(nScale--, Decimal.calcScale(nTest));
		}

		assertEquals(18, Decimal.MAX_SCALE);
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testCompareTo()
	{
		assertTrue(decimal(42).compareTo(decimal(41)) > 0);
		assertTrue(decimal(42).compareTo(decimal(43)) < 0);
		assertTrue(decimal(42).compareTo(decimal(42)) == 0);
		assertTrue(decimal(42).compareTo(decimal(41, 42)) > 0);
		assertTrue(decimal(42).compareTo(decimal(43, 42)) < 0);
		assertTrue(decimal(42).compareTo(decimal(42, 000)) == 0);
		assertTrue(decimal(1, 1).compareTo(decimal(1, 0)) > 0);
		assertTrue(decimal(1, 1).compareTo(decimal(1, 2)) < 0);
		assertTrue(decimal(1, 1).compareTo(decimal(1, 1)) == 0);
		assertTrue(decimal(1, 234).compareTo(decimal(1, 233)) > 0);
		assertTrue(decimal(1, 234).compareTo(decimal(1, 235)) < 0);
		assertTrue(decimal(1, 234).compareTo(decimal(1, 234)) == 0);
		assertTrue(decimal(-1, 234).compareTo(decimal(-1, 233)) < 0);
		assertTrue(decimal(-1, 234).compareTo(decimal(-1, 235)) > 0);
		assertTrue(decimal(-1, 234).compareTo(decimal(-1, 234)) == 0);
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testDecimalLong()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testDecimalLongInt()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testDecimalString()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testDivide()
	{
		assertEquals(decimal(2), decimal(10).divide(decimal(5)));
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testDoubleValue()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testEquals()
	{
		assertTrue(decimal(42).equals(decimal(42)));
		assertFalse(decimal(42).equals(decimal(43)));
		assertTrue(decimal(1, 2345).equals(decimal("1.2345")));
		assertTrue(decimal("-1.2345").equals(decimal("-1.2345")));
		assertFalse(decimal(-1, 234).equals(decimal(1, 234)));
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testFloatValue()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testIntValue()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testLongValue()
	{
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testMultiply()
	{
		testRange(-1000,
				  1000,
				  131,
				  3,
				  d1 ->
				  testRange(-100,
							100,
							139,
							3,
							d2 -> assertMultiplication(d1, d2)));
	}

	/***************************************
	 * Performance test
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testPerformance()
	{
		long t		    = System.currentTimeMillis();
		int  nLoopCount = 1_000_000;

		BigDecimal aSum = BigDecimal.ZERO.setScale(16);

		for (int i = 0; i < nLoopCount; i++)
		{
			BigDecimal val =
				new BigDecimal(i).setScale(6).multiply(new BigDecimal(i))
								 .divide(new BigDecimal("3.3"), 6);

			if (i % 1 == 0)
			{
				aSum = aSum.add(val);
			}
			else
			{
				aSum = aSum.subtract(val);
			}
		}

		t = System.currentTimeMillis() - t;
		System.out.printf("BIGDECIMAL: %s - %d.%03d\n",
						  aSum,
						  t / 1000,
						  t % 1000);
		t = System.currentTimeMillis();

		Decimal nSum = decimal(0, 0);

		for (int i = 0; i < nLoopCount; i++)
		{
			Decimal val = decimal(i).multiply(decimal(i)).divide(decimal(3, 3));

			if (i % 1 == 0)
			{
				nSum = nSum.add(val);
			}
			else
			{
				nSum = nSum.subtract(val);
			}
		}

		t = System.currentTimeMillis() - t;
		System.out.printf("DECIMAL   : %s - %d.%03d\n",
						  nSum,
						  t / 1000,
						  t % 1000);
	}

	/***************************************
	 * Test of {@link Decimal#scaleFactor(int)}
	 */
	@Test
	public void testScaleFactor()
	{
		assertEquals(1, Decimal.scaleFactor(0));
		assertEquals(10, Decimal.scaleFactor(1));
		assertEquals(100, Decimal.scaleFactor(2));
		assertEquals(1000, Decimal.scaleFactor(3));
		assertEquals(10000, Decimal.scaleFactor(4));
		assertEquals(100000, Decimal.scaleFactor(5));
		assertEquals(1000000, Decimal.scaleFactor(6));
		assertEquals(10000000, Decimal.scaleFactor(7));
		assertEquals(100000000, Decimal.scaleFactor(8));
		assertEquals(100000000, Decimal.scaleFactor(-8));
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testSubtract()
	{
		assertEquals(new BigDecimal("1.111"),
					 decimal(1, 234).subtract(decimal(0, 123)).toBigDecimal());
	}

	/***************************************
	 * Test of decimal method
	 */
	@Test
	public void testToString()
	{
		assertEquals("1.1", decimal(1, 1).toString());
		assertEquals("3.141592", decimal(3, 141592).toString());
	}

	/***************************************
	 * Asserts the correctness of a decimal multiplication.
	 *
	 * @param d1 The first operand
	 * @param d2 The second operand
	 */
	private void assertMultiplication(Decimal d1, Decimal d2)
	{
		BigDecimal aCompareValue =
			new BigDecimal(d1.toString()).multiply(new BigDecimal(d2.toString()));
		Decimal    aDecimalValue = d1.multiply(d2);

		aCompareValue =
			aCompareValue.setScale(aDecimalValue.scale(), RoundingMode.CEILING);

		try
		{
			assertEquals(aCompareValue.toString(), aDecimalValue.toString());
		}
		catch (AssertionError e)
		{
			System.out.printf("BD: %s != %s (%s * %s)\n",
							  aCompareValue,
							  aDecimalValue,
							  d1,
							  d2);
			throw e;
		}
	}

	/***************************************
	 * Tests a range of decimal numbers
	 *
	 * @param nStart The starting value
	 * @param nEnd   The end value
	 * @param nStep  The steps to iterate the range with (recommended to be a
	 *               prime to get a good distribution)
	 * @param nScale The scale of the fraction to test with
	 * @param fTest  The function to invoke with the test values
	 */
	private void testRange(int				 nStart,
						   int				 nEnd,
						   int				 nStep,
						   int				 nScale,
						   Consumer<Decimal> fTest)
	{
		for (long i = nStart; i < nEnd; i += nStep)
		{
			for (long f = -scaleFactor(nScale); f < scaleFactor(nScale);
				 f += nStep)
			{
				fTest.accept(decimal(i, f));
			}
		}
	}
}
