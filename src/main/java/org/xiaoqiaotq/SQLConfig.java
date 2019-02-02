/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xiaoqiaotq;

import com.google.common.base.CaseFormat;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

public final class SQLConfig {
    final MutableNetwork<Class<? extends Resource>, TableEdge> graph;

    final CaseFormat sqlColumnFormat;

    SQLConfig(Builder builder) {
        this.graph = builder.graph;
        this.sqlColumnFormat = builder.sqlColumnFormat;
    }

    public CaseFormat sqlColumnFormat() {
        return sqlColumnFormat;
    }

    public String relations() {
        return graph.toString();
    }

    public MutableNetwork<Class<? extends Resource>, TableEdge>  graph() {
        return graph;
    }

    @Override public String toString() {
        return graph.toString();
    }

    public SqlManager createManager() {
        return new SqlManager(this);
    }

    public static class Builder {
        MutableNetwork<Class<? extends Resource>, TableEdge> graph = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
        
        CaseFormat sqlColumnFormat = CaseFormat.LOWER_UNDERSCORE;


        public Builder() {
        }

        public Builder sqlColumnFormat(CaseFormat sqlColumnFormat) {
            if (sqlColumnFormat == null) throw new NullPointerException("sqlColumnFormat == null");
            this.sqlColumnFormat = sqlColumnFormat;
            return this;
        }

        public Builder addRelation(Class<? extends Resource> source,Class<? extends Resource> target, String relationKey) {
            graph.addEdge(source, target,new TableEdge(source,relationKey));
            return this;
        }


        public SQLConfig build() {
            if (graph.edges().isEmpty()) throw new IllegalStateException("relation == null");
            return new SQLConfig(this);
        }
    }
    public static class TableEdge{
        private Class<? extends Resource> source;
        private String relationKey;

        public TableEdge(Class<? extends Resource> source, String relationKey) {
            this.source = source;
            this.relationKey = relationKey;
        }

        public Class<? extends Resource> getSource() {
            return source;
        }

        public String getRelationKey() {
            return relationKey;
        }
    }
}

