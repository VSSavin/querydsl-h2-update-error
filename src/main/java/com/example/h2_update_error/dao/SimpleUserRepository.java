package com.example.h2_update_error.dao;

import com.example.h2_update_error.model.QUser;
import com.example.h2_update_error.model.User;
import com.example.h2_update_error.querydsl.CustomH2QueryFactory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.querydsl.core.types.Projections.bean;

/**
 * @author vssavin on 19.06.2023
 */
public class SimpleUserRepository {
    private static final QUser users = new QUser("users");

    private static final QBean<User> userBean = bean(User.class, users.id, users.login, users.name, users.password, users.email,
            users.authority, users.expiration_date, users.verification_id);

    private AbstractSQLQueryFactory<?> queryFactory;
    private final DataSource dataSource;
    private final Configuration configuration;

    public SimpleUserRepository(DataSource dataSource, Configuration configuration) {
        this.dataSource = dataSource;
        this.configuration = configuration;
    }

    public List<User> findByLogin(String login) {
        return prepareSelectQuery(false).where(users.login.eq(login)).fetch();
    }

    public User findById(Long id) {
        return prepareSelectQuery(false).where(users.id.eq(id)).fetchOne();
    }

    public User save(User user) {
        //Check if entity exists or not exists
        User entityFromDatabase = null;
        if (user.getId() != null) {
            entityFromDatabase = prepareSelectQuery(false).where(users.id.eq(user.getId())).fetchOne();
        } else {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(users.login.eq(user.getLogin())).and(users.email.eq(user.getEmail()))
                    .and(users.name.eq(user.getName())).and(users.password.eq(user.getPassword()));
            entityFromDatabase = prepareSelectQuery(false).where(builder).fetchOne();
        }
        if (entityFromDatabase != null) {
            List<Path<?>> updateListFields = new ArrayList<>();
            List<Path<?>> updateListValues = new ArrayList<>();
            updateListFields.add(users.login);
            updateListFields.add(users.name);
            updateListFields.add(users.password);
            updateListFields.add(users.email);
            updateListFields.add(users.authority);
            updateListFields.add(users.expiration_date);
            if (user.getVerificationId() != null) updateListFields.add(users.verification_id);

            String loginValue = user.getLogin();
            String nameValue = user.getName();
            String passwordValue = user.getPassword();
            String emailValue = user.getEmail();
            String authorityValue = user.getAuthority();
            String verificationIdValue = user.getVerificationId();

            updateListValues.add(Expressions.stringPath(loginValue));
            updateListValues.add(Expressions.stringPath(nameValue));
            updateListValues.add(Expressions.stringPath(passwordValue));
            updateListValues.add(Expressions.stringPath(emailValue));
            updateListValues.add(Expressions.stringPath(authorityValue));
            updateListValues.add(Expressions.dateTimePath(Date.class, user.getExpirationDate().toString()));
            if (verificationIdValue!= null)
                updateListValues.add(Expressions.stringPath(verificationIdValue));

            if (queryFactory instanceof CustomH2QueryFactory) {
                ((CustomH2QueryFactory) queryFactory).h2Update(new RelationalPathBase<User>(users.getType(), users.getMetadata(), "", "users"))
                        .where(users.id.eq(entityFromDatabase.getId()))
                        .set(updateListFields, updateListValues)
                        .execute();
            } else {
                queryFactory.update(new RelationalPathBase<User>(users.getType(), users.getMetadata(), "", "users"))
                        .where(users.id.eq(entityFromDatabase.getId()))
                        .set(updateListFields, updateListValues)
                        .execute();
            }


        } else {

            queryFactory
                    .insert(new RelationalPathBase<User>(users.getType(), users.getMetadata(), "", "users"))
                    .columns(users.login, users.name, users.password, users.email, users.authority,
                            users.expiration_date, users.verification_id)
                    .values(user.getLogin(), user.getName(), user.getPassword(), user.getEmail(),
                            user.getAuthority(), user.getExpirationDate(), user.getVerificationId())
                    .execute();
        }


        return user;
    }

    private AbstractSQLQuery<User,?> prepareSelectQuery(boolean useLastQueryFactory) {
        if (useLastQueryFactory && queryFactory != null) {
            return queryFactory.select(userBean).from(users);
        }
        if (configuration.getTemplates() instanceof PostgreSQLTemplates) {
            queryFactory = new PostgreSQLQueryFactory(configuration, new DataSourceProvider(dataSource));
        } else if (configuration.getTemplates() instanceof H2Templates) {
            queryFactory = new CustomH2QueryFactory(configuration, dataSource);
        }
        else queryFactory = new SQLQueryFactory(configuration, dataSource);
        return queryFactory.select(userBean).from(users);
    }

    private SQLDeleteClause prepareDeleteQuery() {
        if (configuration.getTemplates() instanceof PostgreSQLTemplates) {
            queryFactory = new PostgreSQLQueryFactory(configuration, new DataSourceProvider(dataSource));
        }
        else queryFactory = new SQLQueryFactory(configuration, dataSource);

        return queryFactory.delete(new RelationalPathBase<User>(users.getType(), users.getMetadata(),
                "","users"));
    }

    private static class DataSourceProvider implements Provider<Connection> {

        private final DataSource ds;

        public DataSourceProvider(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection get() {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
