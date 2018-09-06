//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/********************************************************************
 * Base class that invokes an arbitrary method on a target object. Even
 * non-public methods can be used, the implementation will try to invoke
 * setAccessible() on them. In environments where this is not allowed an
 * exception will be thrown. Therefore applications that are built for
 * environments that have limited accessibility (like applets) should only use
 * public methods to dispatch to.
 *
 * <p>The availability of the referenced method will be tested directly on
 * construction (and if missing, an IllegalArgumentException will be thrown).
 * Therefore any problems arising from missing dispatch methods should be
 * detectable during development time as long as the method dispatchers are
 * created during application startup or in unit tests. If not, the developer
 * should expect the exception and handle it according to his needs.</p>
 *
 * <p>This class is not abstract so that instances from it can be created and
 * used directly. It also implements the ResultRunner interface so that
 * dispatchers for no-argument methods can be used in threads and under other
 * conditions where a Runnable interface is required. If a runnable instance is
 * needed for a parameterized method the subclass {@link MethodArgDispatcher}
 * can be used instead.</p>
 *
 * <p>This class may also be subclassed for implementations that need a
 * reflection-based dispatch mechanism (e.g. like generic event listeners).
 * Subclasses only need to forward (or generate) the constructor arguments and
 * invoke the {@link #dispatch(Object[]) dispatch()} method to perform the
 * method call.</p>
 *
 * <p>The generic parameter, as in ResultRunner, defines the type of result
 * returned by the dispatch() and execute() methods. If different result types
 * are possible, this parameter must be a common base class of all possible
 * results.</p>
 *
 * @author eso
 */
public class MethodDispatcher<T>
{
	//~ Instance fields --------------------------------------------------------

	private final Object rTarget;
	private final Method rMethod;
	private boolean		 bUseArgs = true;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new method dispatcher for a method without parameters.
	 *
	 * @param rTarget The target on which the method shall be invoked
	 * @param sMethod The name of the method to invoke
	 */
	public MethodDispatcher(Object rTarget, String sMethod)
	{
		this(rTarget, sMethod, (Class[]) null);
	}

	/***************************************
	 * Creates a new method dispatcher for a certain method.
	 *
	 * @param rTarget The target on which the method shall be invoked
	 * @param rMethod The method to invoke
	 */
	public MethodDispatcher(Object rTarget, Method rMethod)
	{
		this.rTarget = rTarget;
		this.rMethod = rMethod;
	}

	/***************************************
	 * Creates a new method dispatcher that will invoke a certain method on the
	 * target object when one of the dispatch methods is invoked. It will look
	 * for a method with the given name and parameter types that allow it to be
	 * invoked with the given parameter types. If no such method exists an
	 * IllegalArgumentException will be thrown.
	 *
	 * <p>The given method may be non-public. It will then be tried to invoke
	 * setAccessible() on it. In environments where that is not possible an
	 * exception will be thrown. Therefore applications that are built for
	 * environments that have limited accessibility (like applets) should only
	 * use public methods to dispatch to.</p>
	 *
	 * @param  rTarget     The target on which the method shall be invoked
	 * @param  sMethod     The name of the method to invoke
	 * @param  rParamTypes An array containing the method parameter types; may
	 *                     be empty or NULL for no-parameter methods
	 *
	 * @throws IllegalArgumentException If no matching method could be found
	 */
	public MethodDispatcher(Object		rTarget,
							String		sMethod,
							Class<?>... rParamTypes)
	{
		this(rTarget, sMethod, false, rParamTypes);
	}

	/***************************************
	 * Constructor for subclassing that creates a new method dispatcher which
	 * will invoke a certain method on the target object when one of the
	 * dispatch methods is invoked. It will first look for a method with the
	 * given name and parameter types that would allow it to be invoked with the
	 * given parameter types. If such is not found but the bNoParamsOptional
	 * parameter is TRUE it will look for a method with no parameters. If that
	 * also doesn't exist an IllegalArgumentException will be thrown.
	 *
	 * <p>This constructor is non-public because it is intended to be used
	 * internally or by subclasses. Subclasses may allow to optionally use a
	 * non-parameter variant of the method by setting the bNoParamsOptional
	 * parameter to TRUE. In such a case the {@link #dispatch(Object[])} method
	 * will ignore any arguments and invoke the no-argument method.</p>
	 *
	 * <p>The method to be used may be non-public. It will then be tried to
	 * invoke setAccessible() on it. In environments where that is not possible
	 * an exception will be thrown. Therefore applications that are built for
	 * environments that have limited accessibility (like applets) should only
	 * use public methods to dispatch to.</p>
	 *
	 * @param  rTarget           The target on which the method shall be invoked
	 * @param  sMethod           The name of the method to invoke
	 * @param  bNoParamsOptional If TRUE a method with no parameters will be
	 *                           tried if no method with matching parameter
	 *                           types could be found
	 * @param  rParamTypes       The method parameter types; may either be empty
	 *                           or NULL for no-parameter methods
	 *
	 * @throws IllegalArgumentException If no matching method could be found
	 */
	protected MethodDispatcher(Object	   rTarget,
							   String	   sMethod,
							   boolean	   bNoParamsOptional,
							   Class<?>... rParamTypes)
	{
		this.rTarget = rTarget;
		this.rMethod =
			getMethod(rTarget.getClass(),
					  sMethod,
					  bNoParamsOptional,
					  rParamTypes);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Convenience method for the invocation of no-argument methods.
	 *
	 * @return The value returned by the invoked method (NULL for void methods)
	 */
	public T dispatch()
	{
		return dispatch((Object[]) null);
	}

	/***************************************
	 * Dispatches a call to the actual method this dispatcher is registered for.
	 *
	 * @param  rArgs The argument values to be used for the method call
	 *
	 * @return The value returned by the invoked method (NULL for void methods)
	 */
	@SuppressWarnings("unchecked")
	public T dispatch(Object... rArgs)
	{
		return (T) ReflectUtil.invoke(rTarget,
									  rMethod,
									  bUseArgs ? rArgs : null);
	}

	/***************************************
	 * Gets a certain method from a class. This method first looks for a method
	 * with the given name and parameters of the given types. If such is not
	 * found but the argument bNoParamsOptional is TRUE it will look for a
	 * method with no parameters. If that also doesn't exist an
	 * IllegalArgumentException will be thrown.
	 *
	 * <p>This method will also search for non-public methods and tries to set
	 * them as accessible for the purpose of dispatching.</p>
	 *
	 * @param  rClass            The class to search
	 * @param  sMethod           The name of the method
	 * @param  bNoParamsOptional If TRUE a method with no parameters will be
	 *                           tried if no method with matching parameter
	 *                           types could be found
	 * @param  rParamTypes       An array containing the method parameter types;
	 *                           may be empty or NULL for no-parameter methods
	 *
	 * @return The method according to the arguments
	 *
	 * @throws IllegalArgumentException If no matching method could be found
	 */
	private Method getMethod(Class<?>    rClass,
							 String		 sMethod,
							 boolean	 bNoParamsOptional,
							 Class<?>... rParamTypes)
	{
		boolean bHasParams = (rParamTypes != null) && (rParamTypes.length > 0);
		Method  m		   =
			ReflectUtil.findMethod(rClass, sMethod, rParamTypes);

		if (m == null && bNoParamsOptional && bHasParams)
		{
			bUseArgs = false;

			// if not found search a variant without argument
			m = ReflectUtil.findMethod(rClass, sMethod, (Class[]) null);
		}

		if (m == null)
		{
			throw new IllegalArgumentException("INIT: no dispatch method or wrong event handler class: " +
											   sMethod);
		}

		if (!Modifier.isPublic(m.getModifiers()))
		{
			m.setAccessible(true);
		}

		return m;
	}
}
