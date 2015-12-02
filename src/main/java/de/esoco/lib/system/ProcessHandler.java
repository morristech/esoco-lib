//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.system;

import de.esoco.lib.io.StreamUtil;
import de.esoco.lib.thread.ResultRunner;

import java.io.IOException;


/********************************************************************
 * Handles a system process and provides access to the output generated by it.
 *
 * @author eso
 */
public class ProcessHandler extends ResultRunner<String>
{
	//~ Instance fields --------------------------------------------------------

	private ProcessBuilder rProcessBuilder;
	private int			   nMaxOutputSize;
	private int			   nExitValue = Integer.MAX_VALUE;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new process handler instance.
	 *
	 * @param rProcessBuilder A process builder object initialized to create
	 *                        instances of the handled process
	 * @param nMaxOutputSize  The maximum output size that will be processed
	 */
	public ProcessHandler(ProcessBuilder rProcessBuilder, int nMaxOutputSize)
	{
		this.rProcessBuilder = rProcessBuilder;
		this.nMaxOutputSize  = nMaxOutputSize;

		rProcessBuilder.redirectErrorStream(true);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Executes the system process and returns it's output.
	 *
	 * @return A string containing the process output
	 *
	 * @throws IOException If executing the system process fails
	 */
	@Override
	public String execute() throws IOException
	{
		String  sProcessResult = null;
		Process aProcess	   = null;

		try
		{
			aProcess = rProcessBuilder.start();

			byte[] aData =
				StreamUtil.readAll(aProcess.getInputStream(),
								   256,
								   nMaxOutputSize);

			nExitValue     = aProcess.exitValue();
			sProcessResult = new String(aData);
		}
		catch (IllegalThreadStateException e)
		{
			aProcess.destroy();
			throw new IOException("Process did not terminate");
		}

		return sProcessResult;
	}

	/***************************************
	 * Returns the last exit value produced by a process execution. If no
	 * process has been excuted {@link Integer#MAX_VALUE} will be returned.
	 *
	 * @return The last exit value or Integer.MAX_VALUE for none
	 */
	public int getLastExitValue()
	{
		return nExitValue;
	}
}
