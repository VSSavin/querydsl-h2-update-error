package com.example.h2_update_error;

import com.example.h2_update_error.config.SqlScriptsConfig;
import com.example.h2_update_error.dao.SimpleUserRepository;
import com.example.h2_update_error.model.User;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLTemplates;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author vssavin on 19.06.2023
 */
public class SimpleRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRepositoryTest.class);

    private final Configuration configuration = initConfiguration();
    private final DataSource dataSource = initDatasource();
    private final SqlScriptsConfig scriptsConfig = new SqlScriptsConfig(dataSource);
    private final SimpleUserRepository repository = new SimpleUserRepository(dataSource, configuration);

    {
        ArrayList<String> sourceFiles = new ArrayList<>();
        sourceFiles.add("/init_test.sql");
        scriptsConfig.executeSqlScripts(dataSource, "", sourceFiles);
    }

    @Test
    public void userSelectTest() {
        List<User> users = repository.findByLogin("admin");
        Assert.assertTrue("Users list is empty!", users.size() > 0);
        User user = repository.findById(1L);
        Assert.assertNotNull("User is null!", user);
    }

    @Test
    public void userInsertTest() {
        User newUser = new User();
        newUser.setLogin("newUserLogin");
        newUser.setName("newUserName");
        newUser.setAuthority("ROLE_USER");
        newUser.setPassword("$23567ckx;vjb@@#");
        newUser.setEmail("test@test.com");
        newUser.setExpirationDate(new Date());
        newUser.setVerificationId("test-verification-id");
        User storedUser = repository.save(newUser);
        Assert.assertNotNull("User is null!", storedUser);
    }

    @Test
    public void userUpdateTest() {
        List<User> users = repository.findByLogin("admin");
        User user = users.get(0);
        user.setLogin("notadmin");
        repository.save(user);
        System.out.println();
    }


    private Configuration initConfiguration() {
        SQLTemplates h2templates = H2Templates.builder().build();
        return new Configuration(h2templates);
    }

    private DataSource initDatasource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try {
            dataSource.setDriverClassName("org.h2.Driver");
            String url = "jdbc:h2:file:./data/db;AUTO_SERVER=true";
            dataSource.setUrl(url);
            dataSource.setUsername("sa");
            dataSource.setPassword("");
        } catch (Exception e) {
            logger.error("Creating datasource error: ", e);
        }

        return dataSource;
    }
}
