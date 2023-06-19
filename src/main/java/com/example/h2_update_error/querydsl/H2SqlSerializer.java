package com.example.h2_update_error.querydsl;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.SerializerBase;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;

import java.lang.reflect.Field;
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

            boolean modify = false;
            int length = 0;
            String value = "";
            if (update.getValue() instanceof StringPath || update.getValue() instanceof DateTimePath) {
                //TODO: try this...
                /*
                String value = ((StringPath) update.getValue()).getMetadata().getName();
                if (!value.startsWith("'")) {
                    value = "'" + value + "'";
                }
                update.setValue(QuerydslUtil.createStringPath((value)));
                */
                if (update.getValue() instanceof StringPath) {
                    value = ((StringPath) update.getValue()).getMetadata().getName();
                } else {
                    value = ((DateTimePath) update.getValue()).getMetadata().getName();
                }

                length = value.length();
                modify = true;
            }

            handle(update.getValue());
            if (modify) {

                StringBuilder builder = new StringBuilder();
                try {
                    Class<?> c = SerializerBase.class;
                    Field builderField = c.getDeclaredField("builder");
                    builderField.setAccessible(true);
                    builder = (StringBuilder) builderField.get(this);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                //System.out.println();
                if (builder.charAt(builder.length() - 1) == '"') {
                    System.out.println("TEST!!!");
                    builder.replace(builder.length() - length - 2, builder.length(), "'" + value + "'");
                    System.out.println("New builder: " + builder);
                } else {
                    builder.replace(builder.length() - length, builder.length(), "'" + value + "'");
                }
//                builder.setLength(builder.length() - );
//                builder.append();
            }
            first = false;
        }
        skipParent = false;

        if (metadata.getWhere() != null) {
            serializeForWhere(metadata);
        }
    }

}
