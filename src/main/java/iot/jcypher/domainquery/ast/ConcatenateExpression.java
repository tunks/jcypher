/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.domainquery.ast;

public class ConcatenateExpression implements IASTObject {

	private Concatenator concatenator;
	
	public ConcatenateExpression(Concatenator concatenator) {
		super();
		this.concatenator = concatenator;
	}

	public Concatenator getConcatenator() {
		return concatenator;
	}

	@Override
	public String toString() {
		return "[" + concatenator + "]";
	}

	/*****************************************************************/
	public enum Concatenator {
		OR, BR_OPEN, BR_CLOSE
	}
}
