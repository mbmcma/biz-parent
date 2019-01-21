/**
 * Copyright © 2018 organization waynechu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.waynechu.dynamic.datasource;

import cn.waynechu.dynamic.datasource.provider.DynamicDataSourceProvider;
import cn.waynechu.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import cn.waynechu.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源核心组件
 *
 * @author zhuwei
 * @date 2019/1/15 17:29
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean {
    /**
     * 分组标识。如 order-slave1，order-slave2 会划分到 order 组中
     */
    public static final String GROUP_FLAG = "-";

    /**
     * 主数据源，该数据源用于当拦截器未生效时，选择该数据源
     * 比如初始化时 LazyConnectionDataSourceProxy 设置 defaultAutoCommit 和 defaultTransactionIsolation
     */
    private DataSource primaryDataSource;

    @Setter
    protected DynamicDataSourceProvider provider;
    @Setter
    protected Class<? extends DynamicDataSourceStrategy> strategy;

    /**
     * 单数据源
     */
    private Map<String, DataSource> singleDataSource = new LinkedHashMap<>();
    /**
     * 分组数据库
     */
    private Map<String, DynamicGroupDataSource> groupDataSources = new ConcurrentHashMap<>();

    @Override
    protected DataSource determineDataSource() {
        return getDataSource(DynamicDataSourceContextHolder.peek());
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, DataSource> dataSources = provider.loadDataSources();
        log.info("读取到 [{}] 个数据源，开始动态数据源分组...", dataSources.size());
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            if (primaryDataSource == null && entry.getKey().contains(DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG)) {
                primaryDataSource = entry.getValue();
            }
            addDataSource(entry.getKey(), entry.getValue());
        }

        // 当数据源全部为单数据源类型时，选择第一个作为主数据源
        if (primaryDataSource == null) {
            for (Map.Entry<String, DataSource> entry : singleDataSource.entrySet()) {
                if (entry != null) {
                    primaryDataSource = entry.getValue();
                    break;
                }
            }
        }
    }

    /**
     * 添加数据源
     *
     * @param dataSourceName 数据源名称
     * @param dataSource     数据源
     */
    private void addDataSource(String dataSourceName, DataSource dataSource) {
        if (dataSourceName.contains(GROUP_FLAG)) {
            addGroupDataSource(dataSourceName, dataSource);
        } else {
            singleDataSource.put(dataSourceName, dataSource);
            log.info("添加单数据源 [{}]", dataSourceName);
        }
    }

    /**
     * 添加组数据源
     *
     * @param dataSourceName 数据源名称
     * @param dataSource     数据源
     */
    private void addGroupDataSource(String dataSourceName, DataSource dataSource) {
        String dataSourceType = "";
        String groupName = dataSourceName.split(GROUP_FLAG)[0];
        if (groupDataSources.containsKey(groupName)) {
            if (dataSourceName.contains(DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG)) {
                groupDataSources.get(groupName).addMaster(dataSource);
                dataSourceType = DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG;
            } else {
                groupDataSources.get(groupName).addSlave(dataSource);
                dataSourceType = DynamicDataSourceContextHolder.DATASOURCE_SALVE_FLAG;
            }
        } else {
            try {
                DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(groupName, strategy.newInstance());
                if (dataSourceName.contains(DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG)) {
                    groupDatasource.addMaster(dataSource);
                    dataSourceType = DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG;
                } else {
                    groupDatasource.addSlave(dataSource);
                    dataSourceType = DynamicDataSourceContextHolder.DATASOURCE_SALVE_FLAG;
                }
                groupDataSources.put(groupName, groupDatasource);
            } catch (Exception e) {
                log.error("数据源 [{}] 分组失败", dataSourceName, e);
                singleDataSource.remove(dataSourceName);
            }
        }
        log.info("添加数据源 [{}] 至 [{}] 分组，该数据源类型为 [{}]", dataSourceName, groupName, dataSourceType);
    }

    /**
     * 获取数据源
     *
     * @param lookUpKey 用于获取数据源的key
     * @return 数据源
     */
    public DataSource getDataSource(String lookUpKey) {
        DataSource dataSource;
        if (StringUtils.isEmpty(lookUpKey)) {
            dataSource = primaryDataSource;
        } else {
            String[] splitStr = lookUpKey.split(GROUP_FLAG);
            String groupName = splitStr[0];
            String dataSourceType = splitStr[1];

            if (groupDataSources.get(groupName) != null) {
                dataSource = getFromGroupDataSource(groupName, dataSourceType);
            } else {
                dataSource = singleDataSource.get(groupName);
            }
        }

        if (dataSource instanceof DruidDataSource) {
            String dataSourceName = ((DruidDataSource) dataSource).getName();
            log.debug("使用动态数据源 [{}]", dataSourceName);
        }
        return dataSource;
    }

    /**
     * 从组数据源中获取数据源
     *
     * @param groupName      组名
     * @param dataSourceType 数据源类型 master|slave
     * @return 数据源
     */
    private DataSource getFromGroupDataSource(String groupName, String dataSourceType) {
        DataSource dataSource;
        if (DynamicDataSourceContextHolder.DATASOURCE_MASTER_FLAG.equals(dataSourceType)) {
            dataSource = groupDataSources.get(groupName).determineMaster();
        } else if (DynamicDataSourceContextHolder.DATASOURCE_SALVE_FLAG.equals(dataSourceType)) {
            dataSource = groupDataSources.get(groupName).determineSlave();
        } else {
            throw new IllegalArgumentException("Unknown datasource type.");
        }
        return dataSource;
    }
}
