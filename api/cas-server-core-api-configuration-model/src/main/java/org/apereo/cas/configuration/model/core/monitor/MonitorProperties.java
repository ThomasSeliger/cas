package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.monitor.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MonitorProperties")
public class MonitorProperties implements Serializable {
    private static final long serialVersionUID = -7047060071480971606L;

    /**
     * The free memory threshold for the memory monitor.
     * If the amount of free memory available reaches this point
     * the memory monitor will report back a warning status as a health check.
     */
    private int freeMemThreshold = 10;

    /**
     * Options for monitoring the status a nd production of TGTs.
     */
    @NestedConfigurationProperty
    private TicketGrantingTicketMonitorProperties tgt = new TicketGrantingTicketMonitorProperties();

    /**
     * Options for monitoring the status a nd production of STs.
     */
    @NestedConfigurationProperty
    private ServiceTicketMonitorProperties st = new ServiceTicketMonitorProperties();

    /**
     * Options for monitoring the Load on a production server.
     * Load averages are "system load averages" that show the running thread
     * (task) demand on the system as an average number of running plus waiting
     * threads. This measures demand, which can be greater than what the system
     * is currently processing.
     */
    @NestedConfigurationProperty
    private ServerLoadMonitorProperties load = new ServerLoadMonitorProperties();

    /**
     * Warning options that generally deal with cache-based resources, etc.
     */
    @NestedConfigurationProperty
    private MonitorWarningProperties warn = new MonitorWarningProperties();

    /**
     * Options for monitoring JDBC resources.
     */
    @NestedConfigurationProperty
    private JdbcMonitorProperties jdbc = new JdbcMonitorProperties();

    /**
     * Options for monitoring LDAP resources.
     */
    private List<LdapMonitorProperties> ldap = new ArrayList<>(0);

    /**
     * Options for monitoring Memcached resources.
     */
    @NestedConfigurationProperty
    private MemcachedMonitorProperties memcached = new MemcachedMonitorProperties();

    /**
     * Options for monitoring MongoDb resources.
     */
    @NestedConfigurationProperty
    private MongoDbMonitorProperties mongo = new MongoDbMonitorProperties();

    /**
     * Properties relevant to endpoint security, etc.
     */
    @NestedConfigurationProperty
    private ActuatorEndpointsMonitorProperties endpoints = new ActuatorEndpointsMonitorProperties();

}
