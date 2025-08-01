package com.hanihome.hanihome_au_api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -476611763L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final StringPath address = createString("address");

    public final StringPath bio = createString("bio");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isEmailVerified = createBoolean("isEmailVerified");

    public final DateTimePath<java.time.LocalDateTime> lastLoginAt = createDateTime("lastLoginAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> lockedUntil = createDateTime("lockedUntil", java.time.LocalDateTime.class);

    public final NumberPath<Integer> loginAttempts = createNumber("loginAttempts", Integer.class);

    public final StringPath name = createString("name");

    public final EnumPath<com.hanihome.hanihome_au_api.domain.enums.OAuthProvider> oauthProvider = createEnum("oauthProvider", com.hanihome.hanihome_au_api.domain.enums.OAuthProvider.class);

    public final StringPath oauthProviderId = createString("oauthProviderId");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final ListPath<UserPreferredRegion, QUserPreferredRegion> preferredRegions = this.<UserPreferredRegion, QUserPreferredRegion>createList("preferredRegions", UserPreferredRegion.class, QUserPreferredRegion.class, PathInits.DIRECT2);

    public final QUserPrivacySettings privacySettings;

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.hanihome.hanihome_au_api.domain.enums.UserRole> role = createEnum("role", com.hanihome.hanihome_au_api.domain.enums.UserRole.class);

    public final BooleanPath twoFactorEnabled = createBoolean("twoFactorEnabled");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.privacySettings = inits.isInitialized("privacySettings") ? new QUserPrivacySettings(forProperty("privacySettings"), inits.get("privacySettings")) : null;
    }

}

