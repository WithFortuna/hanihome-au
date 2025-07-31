package com.hanihome.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDatabaseInfo is a Querydsl query type for DatabaseInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDatabaseInfo extends EntityPathBase<DatabaseInfo> {

    private static final long serialVersionUID = 1148592242L;

    public static final QDatabaseInfo databaseInfo = new QDatabaseInfo("databaseInfo");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> initializedAt = createDateTime("initializedAt", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath version = createString("version");

    public QDatabaseInfo(String variable) {
        super(DatabaseInfo.class, forVariable(variable));
    }

    public QDatabaseInfo(Path<? extends DatabaseInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDatabaseInfo(PathMetadata metadata) {
        super(DatabaseInfo.class, metadata);
    }

}

