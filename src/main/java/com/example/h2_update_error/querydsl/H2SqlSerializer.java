package com.example.h2_update_error.querydsl;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;

import java.util.Map;

/**
 * @author vssavin on 19.06.2023
 */
public class H2SqlSerializer extends SQLSerializer {
    public H2SqlSerializer(Configuration conf) {
        super(conf);
    }

    public H2SqlSerializer(Configuration conf, boolean dml) {
        super(conf, dml);
    }

    /*
    @Override
    public void serializeUpdate(QueryMetadata metadata, RelationalPath<?> entity, Map<Path<?>, Expression<?>> updates) {
        super.serializeUpdate(metadata, entity, updates);
    }
    */


    @Override
    protected void serializeForUpdate(QueryMetadata metadata, RelationalPath<?> entity, Map<Path<?>, Expression<?>> updates) {
        //super.serializeForUpdate(metadata, entity, updates);
        this.entity = entity;

        serialize(QueryFlag.Position.START, metadata.getFlags());

        if (!serialize(QueryFlag.Position.START_OVERRIDE, metadata.getFlags())) {
            append(templates.getUpdate());
        }
        serialize(QueryFlag.Position.AFTER_SELECT, metadata.getFlags());

        boolean originalDmlWithSchema = dmlWithSchema;
        dmlWithSchema = true;
        handle(entity);
        dmlWithSchema = originalDmlWithSchema;
        append("\n");
        append(templates.getSet());
        boolean first = true;
        skipParent = true;
        for (final Map.Entry<Path<?>,Expression<?>> update : updates.entrySet()) {
            if (!first) {
                append(COMMA);
            }
            handle(update.getKey());
            append(" = ");
            if (!useLiterals && update.getValue() instanceof Constant<?>) {
                constantPaths.add(update.getKey());
            }

            if (update.getValue() instanceof StringPath) {
                //TODO: try this...
                //new StringPath(new PathImpl<String>(String.class, ((StringPath) update.getValue()).getMetadata()))
            }

            handle(update.getValue());
            first = false;
        }
        skipParent = false;

        if (metadata.getWhere() != null) {
            serializeForWhere(metadata);
        }
    }

}
