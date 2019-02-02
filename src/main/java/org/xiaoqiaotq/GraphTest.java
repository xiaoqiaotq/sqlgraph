package org.xiaoqiaotq;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import com.google.common.graph.*;
import org.xiaoqiaotq.resource.*;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


public class GraphTest {
    MutableNetwork<Class<? extends Resource>, SQLConfig.TableEdge> graph;
    //resource==>table 映射
    Map<String, String> resourceToTable = new HashMap<>();

    //1. foreign key 关系怎么表示，用edge表示network要全局唯一
    //2. table alias
    //2. Entity <==> table
    //3.  sql field <==> java field transfer ?
    public static void main(String[] args) {
        GraphTest graphTest = new GraphTest();
        graphTest.init();
    }
    public void init(){
        SQLConfig sqlConfig = new SQLConfig.Builder()
                //server
                .addRelation(Server.class, OperatingSystem.class, "operatingSystemId")
                .addRelation(Server.class, OperatingSystem.class, "agentOsId")

                .addRelation(Server.class, Server.class, "realServerId")
                .addRelation(Server.class, Group.class, "groupId")
                .addRelation(Server.class, Device.class, "deviceId")
                //networkDevice
                .addRelation(NetworkDevice.class, Device.class, "deviceId")
                //group
                .addRelation(Group.class, App.class, "appId")
                //app
                .addRelation(App.class, Product.class, "productId")
                //product
                .addRelation(Product.class, ProductLine.class, "productLineId")
                //productLine
                .addRelation(ProductLine.class, ProductLine.class, "pid")
                //server cpu
                .addRelation(ServerCpu.class, Server.class,  "serverId")
                //server ram
                .addRelation(ServerRam.class, Server.class, "serverId")
                //Device
                .addRelation(Device.class, Idc.class, "idcId")
                .addRelation(Device.class, Room.class, "roomId")
                .addRelation(Device.class, Rack.class, "rackId")
                .addRelation(Device.class, DeviceModel.class, "deviceModelId")
                //Idc
                .addRelation(Idc.class, LogicIdc.class, "logicIdcId")
                //DeviceModel
                .addRelation(DeviceModel.class, Manufacturer.class, "manufactureId")
                //ResourceLabel
                .addRelation(ResourceLabel.class, Server.class, "resourceId")
                //Circle
                .addRelation(CircleDirectory.class, Circle.class, "circleId")
                //ServerExtension
                .addRelation(ServerExtension.class, Server.class, "serverId")
                .build();
        graph = sqlConfig.graph();
//        Set<Class<? extends Resource>> serverSuccessors = graph.successors(Server.class);
//        Set<Class<? extends Resource>> serverPredecessors = graph.predecessors(Server.class);
//        Set<Class<? extends Resource>> groupSuccessors = graph.successors(Group.class);

//        System.err.println(serverSuccessors);
//        System.err.println(serverPredecessors);
//        System.err.println(groupSuccessors);

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


//        boolean connected = graph.hasEdgeConnecting(Server.class, Group.class);
//        String s1 = graph.edgeConnectingOrNull(Server.class, Group.class);
//        String s2 = graph.edgeConnectingOrNull(Group.class, Server.class);
//        Set<String> s3 = graph.edgesConnecting(Server.class, OperatingSystem.class);
//        Set<String> edges = graph.incidentEdges(Server.class);
//        System.err.println("s1==> "+s1);
//        System.err.println("s2==> "+s2);
//        String leftJoinString = MessageFormat.format(" left join `{0}` on `{0}`.id = `{1}`.{2}", Group.class.getSimpleName(), Server.class.getSimpleName(), s1);
//        System.err.println("leftJoinString: "+leftJoinString);

        //从DTO field 判断关联关系
        // @SQLJoinField(
        //  Class<? extends Resource>, 必填
        //  field, 默认取field,或者配置field和column转换规则
        //  joinKey, 当fk parallel是需显示指定
        //  tableToAlias, 默认resource的tableName()
        //  joinType, oneToOne,manyToOne,
        //  reverse, 是否反转


        //TODO
        //1.找出source class 中fields
        //2.找出field中含有注解SQLJoinField，获取field中需要关联的表
        //
        Class<? extends Resource> source = Server.class;
        String sourceTableName = resourceToTable.get(source.getSimpleName());
        String sourceTableAlias = sourceTableName + "_";

        Map<String, List<Class<? extends Resource>>> joinFieldResourcePath = new HashMap<>();
        Map<String, SQLJoinField> joinFieldMetaData = new HashMap<>();
        Map<String, String> fieldSelectClause = new HashMap<>();

        Field[] declaredFields = source.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String fieldName = declaredField.getName();
            String resourceFieldName = fieldName;
            String tableAlias = sourceTableAlias;
            if(declaredField.isAnnotationPresent(SQLJoinField.class)){
                SQLJoinField joinField = declaredField.getAnnotation(SQLJoinField.class);
                Class<? extends Resource> resource = joinField.resource();
                resourceFieldName = joinField.field();
                String joinKey = joinField.joinKey();
                tableAlias = resourceToTable.get(resource.getSimpleName()) + "_" + joinKey;
                boolean reverse = joinField.reverse();
                joinFieldMetaData.put(fieldName, joinField);
                List<Class<? extends Resource>> resources = reverse ? findInPath(resource, source) : findInPath(source, resource);
                joinFieldResourcePath.put(fieldName, resources);

            }
            String column = CaseFormat.LOWER_CAMEL.to(sqlConfig.sqlColumnFormat, resourceFieldName);
            String selectClauseStr = String.format("%s.`%s` as %s", tableAlias, column, fieldName);
            fieldSelectClause.put(fieldName, selectClauseStr);
        }



        // 1 开始计算，传入变量fields
        List<String> fields = Arrays.asList("appName");
        Map<String, List<Class<? extends Resource>>> customFieldPath = joinFieldResourcePath;
        if(!fields.isEmpty()){
            customFieldPath = joinFieldResourcePath.entrySet().stream().filter(e -> fields.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        Map<String, List<Class<? extends Resource>>> aa = customFieldPath;
        //haha math.random
        Comparator<String> comparator = Comparator.comparingDouble(a -> (aa.get(a).size() + Math.random()));
        TreeMap<String, List<Class<? extends Resource>>> treeMap = new TreeMap(comparator.reversed());
        treeMap.putAll(customFieldPath);
//        List<List<Class<? extends Resource>>> paths = new ArrayList<>(fieldResourceMap.values());
//        paths.sort((Comparator.comparingInt(List::size)));
//        Collections.reverse(paths);

        Set<String> finalLeftStringSet = new LinkedHashSet<>();
        for (Map.Entry<String,List<Class<? extends Resource>>> entry : treeMap.entrySet()) {
            String fieldName = entry.getKey();
            List<Class<? extends Resource>> resourcePath = entry.getValue();
            SQLJoinField sqlJoinField = joinFieldMetaData.get(fieldName);
            String joinKey = sqlJoinField.joinKey();
            boolean joinByClause = sqlJoinField.joinByClause();
            SQLClause[] sqlClauses = sqlJoinField.joinOnSQLClause();
            Set<String> leftJoinStrSet;
            if (joinByClause) {
                Set<String> clauseStringSet = new LinkedHashSet<>();
                for (SQLClause c : sqlClauses) {
                    String clauseStr;
                    String lrAlias = resourceToTable.get(c.lr().getSimpleName()) + "_";
                    String lf = CaseFormat.LOWER_CAMEL.to(sqlConfig.sqlColumnFormat, c.lf());
                    if (c.rv().isEmpty()) {
                        String rrAlias = resourceToTable.get(c.rr().getSimpleName()) + "_";
                        String rf = CaseFormat.LOWER_CAMEL.to(sqlConfig.sqlColumnFormat, c.rf());
                        clauseStr = String.format("%s.`%s` = %s.`%s`", lrAlias, lf, rrAlias, rf);
                    }else{
                        clauseStr = String.format("%s.%s = %s", lrAlias, lf, c.rv());
                    }
                    clauseStringSet.add(clauseStr);
                }
                String clauseString = String.join(" and ", clauseStringSet);
                Class<? extends Resource> resource = sqlJoinField.resource();
                String resourceTable = resourceToTable.get(resource.getSimpleName());
                String resourceAlias = resourceTable+"_";
                String leftJoinStr = String.format("left join `%s` as %s on %s", resourceTable, resourceAlias,clauseString);
                leftJoinStrSet = Collections.singleton(leftJoinStr);
            }
            else if(sqlJoinField.reverse()){
                leftJoinStrSet = this.printReverseJoinRelation(resourcePath, joinKey);
            }else {
                leftJoinStrSet = this.printJoinRelation(resourcePath, joinKey);
                System.err.println(resourcePath);
            }
            finalLeftStringSet.addAll(leftJoinStrSet);
        }
        String selectClause = fieldSelectClause.values().stream().collect(Collectors.joining(", "));
        if(!fields.isEmpty()){
             selectClause = fieldSelectClause.entrySet().stream().filter(e -> fields.contains(e.getKey())).map(Map.Entry::getValue).collect(Collectors.joining(", "));
        }

        String select = "select "+selectClause+" from `" + sourceTableName + "` as " + sourceTableAlias;
        String leftJoinString = String.join(" ", finalLeftStringSet);
        System.err.println(select+leftJoinString);
        System.err.println("hahaah");
//
//        for (List<Class<? extends Resource>> list : paths) {
//            if (!relations.containsAll(list)) {
//                relations.addAll(list);
//                graphTest.printReverseJoinRelation(list, reverse,"");
//                System.err.println(list);
//            }
//        }

//        Set<Class<? extends Resource>> toResources = Sets.newHashSet(Rack.class, ProductLine.class, Product.class, OperatingSystem.class);
//        Set<Class<? extends Resource>> fromResources = Sets.newHashSet(ResourceLabel.class);
//
//        Map<String,List<Class<? extends Resource>>> fieldPathMap = new HashMap<>();
//        List<List<Class<? extends Resource>>> fromPaths = new ArrayList<>();
//        List<List<Class<? extends Resource>>> toPaths = new ArrayList<>();
//
//        for (Class<? extends Resource> toResource : toResources) {
//            Long start1 = System.currentTimeMillis();
//            List<Class<? extends Resource>> path = this.findInPath(source, toResource);
//            fromPaths.add(path);
//            fieldPathMap.put(toResource.getName(), path);
//            System.err.println(source.getSimpleName() +"==>"+ toResource.getSimpleName() + " cost: " + (System.currentTimeMillis() - start1));
//        }
//        for (Class<? extends Resource> fromResource : fromResources) {
//            Long start1 = System.currentTimeMillis();
//            List<Class<? extends Resource>> path = this.findInPath(fromResource, source);
//            toPaths.add(path);
//            System.err.println(fromResource.getSimpleName() +"==>"+ source.getSimpleName() + " cost: " + (System.currentTimeMillis() - start1));
//        }
//
////        grahpTest.findInPath(ResourceLabel, Server);
//        this.mergeToFinalPath(fromPaths,false);
//        this.mergeToFinalPath(toPaths,true);



    }


    public  List<Class<? extends Resource>> findInPath(Class<? extends Resource> source,Class<? extends Resource> target){
        Long start = System.currentTimeMillis();
        Iterable<Class<? extends Resource>> resourceTypes = Traverser.forGraph(graph).breadthFirst(source);
        System.err.println("bfs cost: " + (System.currentTimeMillis() - start));
        ArrayDeque<Class<? extends Resource>> queue = new ArrayDeque();
        for (Class<? extends Resource> value : resourceTypes) {
//            System.err.println(value);
            if (value == target) {
                break;
            }
            queue.push(value);
        }
        //path
        List<Class<? extends Resource>> shortestPathList = new ArrayList();
        Class<? extends Resource> currentResource = target;
        shortestPathList.add(target);
        while (!queue.isEmpty()){
            Class<? extends Resource> resource = queue.pop();
            if(graph.hasEdgeConnecting(resource, currentResource))
            {
                shortestPathList.add(resource);
                currentResource = resource;
                if(resource == source)
                    break;
            }
        }
        Collections.reverse(shortestPathList);
//        System.err.println(shortestPathList);
        return shortestPathList;
    }

    public  List<Class<? extends Resource>> mergeToFinalPath(List<List<Class<? extends Resource>>> paths,boolean reverse){
//        List<String> list1 = Lists.newArrayList("a", "b", "c");
//        List<String> list2 = Lists.newArrayList("a", "b");
//        List<String> list4 = Lists.newArrayList("h", "i", "j");
//        List<String> list3 = Lists.newArrayList("a", "b", "c", "d");
//        List<String> list5 = Lists.newArrayList("h", "m", "n");

        paths.sort(Comparator.comparingInt(List::size));
        Collections.reverse(paths);

        Set<Class<? extends Resource>> relations = Sets.newLinkedHashSet();
//        for (List<Class<? extends Resource>> list : paths) {
//            if (!relations.containsAll(list)) {
//                relations.addAll(list);
//                printReverseJoinRelation(list, reverse,"");
//                System.err.println(list);
//            }
//        }
//        System.err.println(relations);
        return new ArrayList<>(relations);
    }

    public Set<String> printJoinRelation(List<Class<? extends Resource>> path,String joinKey) {
        Set<String> leftJoinStringSet = new LinkedHashSet<>();
        Class<? extends Resource> first = path.get(0);
        for (int i = 1; i < path.size() ; i++) {
            String firstResourceName = first.getSimpleName();
            String firstAlias = resourceToTable.get(firstResourceName)+"_";
            Class<? extends Resource> second = path.get(i);
            Set<SQLConfig.TableEdge> relationKeys = graph.edgesConnecting(first, second);
            String secondResourceName = second.getSimpleName();
            String secondTableName = resourceToTable.get(secondResourceName);
            String secondAlias = secondTableName+"_"+joinKey;
            if (!relationKeys.isEmpty()) {
                String relationKey = "";
                if(relationKeys.size()==1){
                    SQLConfig.TableEdge edge = relationKeys.iterator().next();
                    relationKey = edge.getRelationKey();
                } else {
                    //TODO parallel get from field , field relationKey must specify
                    relationKey = joinKey;
//                    secondAlias = tableToAlias.get(relationKey);
                    System.err.println("parallel join............");
                }
                //relationKey 转换
                relationKey = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, relationKey);
                String leftJoinString = MessageFormat.format(" left join `{0}` as {1} on {1}.id = {2}.`{3}`", secondTableName, secondAlias, firstAlias, relationKey);
                System.err.println(leftJoinString);

                System.err.println("=================================");
                leftJoinStringSet.add(leftJoinString);
            } else {
                throw new RuntimeException(firstResourceName + secondResourceName + "配置错误");
            }
            first = second;
        }
        return leftJoinStringSet;
    }

    public Set<String> printReverseJoinRelation(List<Class<? extends Resource>> path, String joinKey) {
        if (path.size() != 2) {
            throw new RuntimeException(joinKey + "只支持相邻的节点："+path);
        }
        Class<? extends Resource> first = path.get(0);
        Class<? extends Resource> second = path.get(1);
        Set<SQLConfig.TableEdge> tableEdges = graph.edgesConnecting(first, second);

        String firstResourceName = first.getSimpleName();
        String firstTableName = resourceToTable.get(firstResourceName);
        String firstAlias = firstTableName+"_"+joinKey;

        String secondResourceName = second.getSimpleName();
        String secondAlias = resourceToTable.get(Server.class.getSimpleName())+"_";
        if (!tableEdges.isEmpty()) {
            String relationKey = "";
            if(tableEdges.size()==1){
                SQLConfig.TableEdge edge = tableEdges.iterator().next();
                relationKey = edge.getRelationKey();
            } else {
                //TODO parallel get from field , field relationKey must specify
                relationKey = joinKey;
                System.err.println("parallel join............");
            }
            //relationKey 转换
            relationKey = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, relationKey);
            String leftJoinString = MessageFormat.format(" left join `{0}` as {1} on {2}.id = {1}.`{3}`", firstTableName, firstAlias, secondAlias, relationKey);
            System.err.println(leftJoinString);

            System.err.println("=================================");
            return Collections.singleton(leftJoinString);
        } else {
            throw new RuntimeException(firstResourceName + secondResourceName + "配置错误");
        }
    }
}
