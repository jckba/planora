package com.planora.backend.category.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Category {
    private UUID id;
    private UUID userId;
    private String name;
    private String color;
    private String icon;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer version;

    private Category(
        UUID id,
        UUID userId,
        String name,
        String color,
        String icon,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt,
        Integer version
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Category create(
        UUID userId,
        String name,
        String color,
        String icon
    ) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");
        Objects.requireNonNull(icon, "Icon cannot be null");

        String normalizedName = normalize(name);
        String normalizedColor = normalize(color);
        String normalizedIcon = normalize(icon);

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if (normalizedColor.isEmpty()) {
            throw new IllegalArgumentException("Color cannot be blank");
        }

        if (normalizedIcon.isEmpty()) {
            throw new IllegalArgumentException("Icon cannot be blank");
        }

        Instant now = Instant.now();

        return new Category(
            UUID.randomUUID(),
            userId,
            normalizedName,
            normalizedColor,
            normalizedIcon,
            null,
            now,
            now,
            0
        );
    }

    public static Category restore(
        UUID id,
        UUID userId,
        String name,
        String color,
        String icon,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt,
        Integer version
    ) {
        return new Category(
            id,
            userId,
            name,
            color,
            icon,
            deletedAt,
            createdAt,
            updatedAt,
            version
        );
    }

    public void delete() {
        if (deletedAt != null) {
            return;
        }

        deletedAt = Instant.now();
        updatedAt = Instant.now();
        version++;
    }

    public void update(
        String name,
        String color,
        String icon
    ) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");
        Objects.requireNonNull(icon, "Icon cannot be null");

        String normalizedName = normalize(name);
        String normalizedColor = normalize(color);
        String normalizedIcon = normalize(icon);

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if (normalizedColor.isEmpty()) {
            throw new IllegalArgumentException("Color cannot be blank");
        }

        if (normalizedIcon.isEmpty()) {
            throw new IllegalArgumentException("Icon cannot be blank");
        }

        this.name = normalizedName;
        this.color = normalizedColor;
        this.icon = normalizedIcon;
        this.updatedAt = Instant.now();
        this.version++;
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
