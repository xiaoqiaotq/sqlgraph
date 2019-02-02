package org.xiaoqiaotq.resource;

import org.xiaoqiaotq.Resource;
import org.xiaoqiaotq.SQLClause;
import org.xiaoqiaotq.SQLJoinField;

import java.util.List;

public class Server implements Resource {

    private Long id;

    private String name;

    private Long operatingSystemId;

    @SQLJoinField(field = "name", resource = OperatingSystem.class, joinKey = "operatingSystemId")
    private String operatingSystemName;

    private Long agentOsId;

    @SQLJoinField(field = "type", resource = OperatingSystem.class, joinKey = "agentOsId")
    private String agentOsType;

    @SQLJoinField(field = "name", resource = OperatingSystem.class, joinKey = "agentOsId")
    private String agentOsName;

    private Long groupId;

    @SQLJoinField(field = "name", resource = Group.class)
    private String groupName;

    @SQLJoinField(field = "id", resource = App.class)
    private Long appId;

    @SQLJoinField(field = "uk", resource = App.class)
    private String appUk;

    @SQLJoinField(field = "name", resource = App.class)
    private String appName;

    @SQLJoinField(field = "id", resource = Product.class)
    private Long productId;

    @SQLJoinField(field = "name", resource = Product.class)
    private String productName;

    @SQLJoinField(field = "id", resource = ProductLine.class)
    private Long productLineId;

    @SQLJoinField(field = "name", resource = ProductLine.class)
    private String productLineName;

    @SQLJoinField(field = "name", resource = ServerExtension.class, reverse = true)
    private String serverExtensionName;

    @SQLJoinField(field = "labelId",resource = ResourceLabel.class, joinByClause = true,
            joinOnSQLClause = {
                    @SQLClause(lr=Server.class,lf = "id",rr=ResourceLabel.class,rf="resourceId"),
                    @SQLClause(lr=ResourceLabel.class,lf = "resourceType",rv="'server'")}
                    )
    private List<Long> labelIds;

}
