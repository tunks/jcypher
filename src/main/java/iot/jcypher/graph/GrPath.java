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

package iot.jcypher.graph;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.graph.internal.GrId;
import iot.jcypher.query.result.util.ResultHandler;

public class GrPath {

	private ResultHandler resultHandler;
	private int rowIndex;
	
	private GrId startNodeId;
	private GrId endNodeId;
	private List<GrId> relationIds;
	
	GrPath(ResultHandler resultHandler, GrId startNodeId,
			GrId endNodeId, List<GrId> relationIds, int rowIndex) {
		super();
		this.resultHandler = resultHandler;
		this.rowIndex = rowIndex;
		this.startNodeId = startNodeId;
		this.endNodeId = endNodeId;
		this.relationIds = relationIds;
	}

	public GrNode getStartNode() {
		return this.resultHandler.getNode(this.startNodeId, this.rowIndex);
	}
	
	public GrNode getEndNode() {
		return this.resultHandler.getNode(this.endNodeId, this.rowIndex);
	}
	
	public List<GrRelation> getRelations() {
		List<GrRelation> rels = new ArrayList<GrRelation>(this.relationIds.size());
		for (GrId rid : this.relationIds) {
			rels.add(this.resultHandler.getRelation(rid));
		}
		return rels;
	}
	
	public int getLength() {
		return this.relationIds.size();
	}
}
