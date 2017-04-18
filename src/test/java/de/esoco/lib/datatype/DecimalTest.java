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

import org.junit.Test;

import static de.esoco.lib.datatype.Decimal.decimal;

import static org.junit.Assert.assertEquals;


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
//		Decimal _2_5 = decimal(2, 5);
//
//		assertEquals("6.25", _2_5.multiply(_2_5).toString());
	}

	/***************************************
	 * Performance test
	 */
	@Test
	public void testPerformance()
	{
		long	   t    = System.currentTimeMillis();
		BigDecimal aSum = BigDecimal.ZERO.setScale(16);

		for (int i = 0; i < 100_000_000; i++)
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
		System.out.printf("BIGD: %s - %d.%03d\n", aSum, t / 1000, t % 1000);
		t = System.currentTimeMillis();

		Decimal nSum = decimal(0, 0);

		for (int i = 0; i < 100_000_000; i++)
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
		System.out.printf("LONG: %s - %d.%03d\n", nSum, t / 1000, t % 1000);
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
}
