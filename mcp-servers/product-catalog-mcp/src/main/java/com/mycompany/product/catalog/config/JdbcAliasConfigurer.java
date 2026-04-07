package com.mycompany.product.catalog.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.event.CamelContextStartingEvent;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workaround for Quarkus + Camel JDBC component integration issue.
 *
 * <p>Problem:
 * In recent versions of Quarkus with Camel, the default DataSource (the one without @Named qualifier)
 * is not automatically registered in Camel's registry with the name "default". This causes issues when
 * using the JDBC component with URIs like "jdbc:default?..." because Camel cannot find a DataSource
 * named "default".
 *
 * <p>Solution:
 * This class listens for Camel context startup and programmatically binds the unnamed/default DataSource
 * to the name "default" in Camel's registry, making it available for JDBC component URIs.
 *
 * <p>Usage:
 * After this configuration is in place, you can use:
 * .to("jdbc:default?useHeadersAsParameters=true")
 *
 * <p>Note:
 * This workaround may become unnecessary in future versions if Quarkus/Camel integration
 * is updated to automatically register the default DataSource with the "default" name.
 */
@ApplicationScoped
public class JdbcAliasConfigurer {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcAliasConfigurer.class);

  /**
   * Binds the default (unnamed) DataSource to Camel registry as "default".
   *
   * <p>This method executes when Camel context is starting up, ensuring the DataSource
   * is available before any routes that might reference "jdbc:default" are initialized.
   *
   * @param event Camel context starting event
   */
  void onCamelContextStarting(@Observes CamelContextStartingEvent event) {
    CamelContext camel = event.getContext();
    Registry registry = camel.getRegistry();

    // Skip if something is already bound as "default"
    if (registry.lookupByName("default") != null) {
      LOG.debug("Camel registry already contains a DataSource named 'default' – nothing to do.");
      return;
    }

    BeanManager bm = CDI.current().getBeanManager();

    // Find the DataSource bean that has NO @Named qualifier (the default one from quarkus.datasource.*)
    @SuppressWarnings("unchecked")
    Bean<DataSource> targetBean = (Bean<DataSource>) bm.getBeans(DataSource.class)
        .stream()
        .filter(bean -> bean.getQualifiers()
            .stream()
            .noneMatch(a -> a.annotationType().equals(Named.class)))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No un-qualified DataSource bean found; cannot bind 'default' for Camel."));

    // Get a proper CDI reference to the DataSource bean
    CreationalContext<DataSource> ctx = bm.createCreationalContext(targetBean);
    DataSource ds = (DataSource) bm.getReference(targetBean, DataSource.class, ctx);

    // Register it in Camel's registry with the name "default"
    registry.bind("default", ds);
    LOG.info("Bound un-qualified DataSource bean ({}) as 'default' in Camel registry.",
        targetBean.getBeanClass().getSimpleName());
  }
}
