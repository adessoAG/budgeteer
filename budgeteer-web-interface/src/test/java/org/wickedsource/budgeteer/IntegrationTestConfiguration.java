package org.wickedsource.budgeteer;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.wickedsource.budgeteer.service.security.BudgeteerMethodSecurityExpressionRoot;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.wickedsource.budgeteer")
public class IntegrationTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();

        properties.setProperty("budgeteer.mail.activate", "false");
        properties.setProperty("flyway.enabled", "false");
        pspc.setProperties(properties);
        return pspc;
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build();
    }

    @Bean
    @Primary
    public BudgeteerMethodSecurityExpressionRoot budgeteerMethodSecurityExpressionRootBean() {
        BudgeteerMethodSecurityExpressionRoot root = Mockito.mock(BudgeteerMethodSecurityExpressionRoot.class);

        Mockito.when(root.canReadInvoice(Mockito.anyLong())).thenReturn(true);
        Mockito.when(root.canReadContract(Mockito.anyLong())).thenReturn(true);
        Mockito.when(root.canReadBudget(Mockito.anyLong())).thenReturn(true);
        Mockito.when(root.canReadPerson(Mockito.anyLong())).thenReturn(true);
        Mockito.when(root.canReadProject(Mockito.anyLong())).thenReturn(true);

        return root;
    }

}
