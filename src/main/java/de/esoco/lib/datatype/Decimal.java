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

import java.util.Objects;


/********************************************************************
 * A decimal implementation with limited precision but more effective processing
 * than {@link BigDecimal}.
 *
 * @author eso
 */
public class Decimal extends Number
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private long nInteger;
	private long nFraction = 0;
	private int  nScale    = 0;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance from a string.
	 *
	 * @param sDecimal A string to parse as a decimal
	 */
	public Decimal(String sDecimal)
	{
		Objects.requireNonNull(sDecimal);

		String[] aParts = sDecimal.split(".");

		if (aParts.length == 0 || aParts.length > 2)
		{
			throw new IllegalArgumentException("Invalid decimal string: " +
											   sDecimal);
		}

		nInteger = Long.parseLong(aParts[0]);

		if (aParts.length == 2)
		{
			nFraction = Integer.parseInt(aParts[1]);
			nScale    = aParts[1].length();
		}
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nInteger  The integer part of the decimal value
	 * @param nFraction The fraction part of the decimal value
	 */
	public Decimal(long nInteger, long nFraction)
	{
		this.nInteger  = nInteger;
		this.nFraction = nFraction;

		do
		{
			nScale++;
			nFraction /= 10;
		}
		while (nFraction > 0);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method to create a new decimal from a string.
	 *
	 * @param  sDecimal A string to parse as a decimal
	 *
	 * @return The new decimal
	 */
	public static Decimal decimal(String sDecimal)
	{
		return new Decimal(sDecimal);
	}

	/***************************************
	 * Factory method to create a new decimal from an integer value without
	 * fraction.
	 *
	 * @param  nInteger The integer part of the decimal value
	 *
	 * @return The new decimal
	 */
	public static Decimal decimal(long nInteger)
	{
		Decimal aDecimal = decimal(nInteger, 0);

		aDecimal.nScale = 0;

		return aDecimal;
	}

	/***************************************
	 * Factory method to create a new decimal from the integer and fraction
	 * parts.
	 *
	 * @param  nInteger  The integer part of the decimal value
	 * @param  nFraction The fraction part of the decimal value
	 *
	 * @return The new decimal
	 */
	public static Decimal decimal(long nInteger, long nFraction)
	{
		return new Decimal(nInteger, nFraction);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds another decimal value to this one and returns a new decimal with the
	 * resulting value.
	 *
	 * @param  rOther The other decimal to add
	 *
	 * @return The new decimal value
	 */
	public Decimal add(Decimal rOther)
	{
		return new Decimal(nInteger + rOther.nInteger,
						   nFraction + rOther.nFraction);
	}

	/***************************************
	 * Divides this decimal value by another and returns a new decimal with the
	 * resulting value.
	 *
	 * @param  rOther The other decimal to divide by
	 *
	 * @return The new decimal value
	 */
	public Decimal divide(Decimal rOther)
	{
		return new Decimal(rOther.nInteger != 0 ? nInteger / rOther.nInteger
												: nInteger,
						   rOther.nFraction != 0 ? nFraction / rOther.nFraction
												 : nFraction);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public double doubleValue()
	{
		return Double.parseDouble(toString());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue()
	{
		return Float.parseFloat(toString());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int intValue()
	{
		return (int) longValue();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public long longValue()
	{
		return nInteger;
	}

	/***************************************
	 * Multiplies this decimal value with another and returns a new decimal with
	 * the resulting value.
	 *
	 * @param  rOther The other decimal to multiply with
	 *
	 * @return The new decimal value
	 */
	public Decimal multiply(Decimal rOther)
	{
		return new Decimal(nInteger * rOther.nInteger,
						   nFraction * rOther.nFraction);
	}

	/***************************************
	 * Subtracts another decimal value from this one and returns a new decimal
	 * with the resulting value.
	 *
	 * @param  rOther The other decimal to subtract
	 *
	 * @return The new decimal value
	 */
	public Decimal subtract(Decimal rOther)
	{
		return new Decimal(nInteger - rOther.nInteger,
						   nFraction - rOther.nFraction);
	}

	/***************************************
	 * Returns a {@link BigDecimal} representation of this decimal.
	 *
	 * @return The big decimal value
	 */
	public BigDecimal toBigDecimal()
	{
		return new BigDecimal(toString());
	}

	/***************************************
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return nInteger + "." + nFraction;
	}
}
