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
package de.esoco.lib.manage;

/********************************************************************
 * Interface for objects that are transactional, i.e. support the committing or
 * rollback of state changes.
 *
 * @author eso
 */
public interface Transactional
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * This method must be invoked to make any changes that have been made to
	 * the state of the implementing object (or underlying objects) permanent.
	 *
	 * @throws Exception Any exception may be thrown if the operation fails
	 */
	public void commit() throws Exception;

	/***************************************
	 * This method must be invoked to revert any changes that have been made to
	 * the state of the implementing object (or underlying objects).
	 *
	 * @throws Exception Any exception may be thrown if the operation fails
	 */
	public void rollback() throws Exception;
}