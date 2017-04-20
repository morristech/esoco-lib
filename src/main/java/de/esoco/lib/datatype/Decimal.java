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

import static java.lang.Math.abs;
import static java.lang.Math.max;


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

	/**
	 * The maximum scale of a decimal (= the maximum decimal digits that fit in
	 * a signed long (2^63))
	 */
	public static final int MAX_SCALE = calcScale(Long.MAX_VALUE / 10);

	//~ Instance fields --------------------------------------------------------

	private final long nInteger;
	private final long nFraction;
	private final int  nScale;

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
		else
		{
			nFraction = 0;
			nScale    = 0;
		}
	}

	/***************************************
	 * Creates a new instance from integer and fraction parts. If the value is
	 * negative the sing must only be set on the integer part!
	 *
	 * @param nInteger  The integer part of the decimal value, including sign
	 * @param nFraction The fraction part of the decimal value (>= 0)
	 */
	public Decimal(long nInteger, long nFraction)
	{
		this(nInteger, nFraction, calcScale(nFraction));
	}

	/***************************************
	 * Internal constructor to create a new instance with an explicit scale.
	 *
	 * @param nInteger  The integer part of the decimal value
	 * @param nFraction The fraction part of the decimal value (>=0)
	 * @param nScale    The scale (>=0)
	 */
	private Decimal(long nInteger, long nFraction, int nScale)
	{
		this.nInteger  = nInteger;
		this.nFraction = abs(nFraction);
		this.nScale    = abs(nScale);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Calculates the scale of a fraction.
	 *
	 * @param  n The fraction value
	 *
	 * @return The calculated scale
	 */
	public static int calcScale(long n)
	{
		n = abs(n);

		return n < 100_000
			   ? (n < 100 ? n < 10 ? 1 : 2
						  : n < 1_000 ? 3 : n < 10_000 ? 4 : 5)
			   : (n < 10_000_000
				  ? n < 1_000_000 ? 6 : 7
				  : (n < 100_000_000
					 ? 8
					 : (n < 1_000_000_000
						? 9
						: (n < 100_000_000_000_000L
						   ? (n < 100_000_000_000L
							  ? n < 10_000_000_000L ? 10 : 11
							  : n < 1_000_000_000_000L
							  ? 12 : n < 10_000_000_000_000L ? 13 : 14)
						   : (n < 10_000_000_000_000_000L
							  ? n < 1_000_000_000_000_000L ? 15 : 16
							  : n < 100_000_000_000_000_000L
							  ? 17 : n < 1_000_000_000_000_000_000L ? 18 : 19)))));
	}

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
		return new Decimal(nInteger, 0, 0);
	}

	/***************************************
	 * Factory method to create a new decimal from the integer and fraction
	 * parts.
	 *
	 * @param  nInteger  The integer part of the decimal value
	 * @param  nFraction The fraction part of the decimal value (>= 0)
	 *
	 * @return The new decimal
	 */
	public static Decimal decimal(long nInteger, long nFraction)
	{
		return new Decimal(nInteger, nFraction);
	}

	/***************************************
	 * Returns the decimal factor for a certain scale. The resulting valie is
	 * effectively 10^nScale.
	 *
	 * @param  nScale The scale to return the factor for (must be >= 0)
	 *
	 * @return The decimal scale factor
	 */
	static long scaleFactor(int nScale)
	{
		long nFactor = 1;

		nScale = abs(nScale);

		if (nScale > 0)
		{
			if (nScale < 7)
			{
				nFactor =
					nScale == 1
					? 10
					: nScale == 2
					  ? 100
					  : nScale == 3
					  ? 1000
					  : nScale == 4 ? 10_000
									: nScale == 5 ? 100_000 : 1_000_000;
			}
			else
			{
				nFactor = 10_000_000;

				while (nScale-- > 7)
				{
					nFactor *= 10;
				}
			}
		}

		return nFactor;
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
	 * Returns the fraction part of this decimal.
	 *
	 * @return The fraction part
	 */
	public final long fraction()
	{
		return nFraction;
	}

	/***************************************
	 * Returns the integer part of this decimal.
	 *
	 * @return The integer part
	 */
	public final long integer()
	{
		return nInteger;
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
		long nMultInt     = nInteger * rOther.nInteger;
		long nMultFrac    = nFraction * rOther.nFraction;
		int  nMultScale   = nScale + rOther.nScale;
		int  nMaxScale    = max(nScale, rOther.nScale);
		long nScaleFactor = scaleFactor(nMaxScale);

		long nCrossMult;
		long cm1 = abs(nInteger * rOther.nFraction);
		long cm2 = abs(nFraction * rOther.nInteger);

		if (nScale == rOther.nScale)
		{
			nCrossMult = cm1 + cm2;
		}
		else if (nScale > rOther.nScale)
		{
			nCrossMult = cm1 * scaleFactor(nScale - rOther.nScale) + cm2;
		}
		else
		{
			nCrossMult = cm1 + cm2 * scaleFactor(rOther.nScale - nScale);
		}

		long nCrossFrac = nCrossMult % nScaleFactor;

		if (nCrossFrac > 0)
		{
			if (nMultScale == nMaxScale)
			{
				nMultFrac += nCrossFrac;
			}
			else if (nMultScale > nMaxScale)
			{
				nMultFrac += nCrossFrac * scaleFactor(nMultScale - nMaxScale);
			}
		}

		long nMultScaleFactor = scaleFactor(nMultScale);

		if (nMultInt > 0)
		{
			nMultInt += nMultFrac / nMultScaleFactor;
			nMultInt += nCrossMult / nScaleFactor;
		}
		else
		{
			nMultInt -= nMultFrac / nMultScaleFactor;
			nMultInt -= nCrossMult / nScaleFactor;
		}

		nMultFrac %= nMultScaleFactor;

		while (nMultScale > nScale && nMultFrac % 10 == 0)
		{
			// reduce scale by removing trailing zeros in fraction
			nMultScale--;
			nMultFrac /= 10;
		}

		return new Decimal(nMultInt, nMultFrac, nMultScale);
	}

	/***************************************
	 * Returns the scale of this decimal.
	 *
	 * @return The scale
	 */
	public final int scale()
	{
		return nScale;
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
	@SuppressWarnings("boxing")
	public String toString()
	{
		return nScale != 0
			   ? String.format("%d.%0" + nScale + "d", nInteger, nFraction)
			   : Long.toString(nInteger);
	}
}
