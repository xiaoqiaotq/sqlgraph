package org.xiaoqiaotq;

import com.google.common.graph.MutableNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlManager {
    private final MutableNetwork<Class<? extends Resource>, SQLConfig.TableEdge> graph;
    //resource==>table 映射
    Map<String, String> resourceToTable = new HashMap<>();

    Map<String, String> tableToAlias = new HashMap<>();

    private SQLConfig sqlConfig;

    SqlManager(SQLConfig sqlConfig) {
        this.sqlConfig = sqlConfig;
        this.graph = sqlConfig.graph;
    }

    public String generateSql(Class<? extends Resource> source){
        return null;
    }

    public void init(){
        long start = System.currentTimeMillis();
        resourceToTable = graph.nodes().stream().collect(Collectors.toMap(Class::getSimpleName, c -> {
            Resource resource = null;
            try {
                resource = c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resource.tableName();
        }));
        System.err.println("newInstance cost: " + (System.currentTimeMillis() - start));

    }
}
