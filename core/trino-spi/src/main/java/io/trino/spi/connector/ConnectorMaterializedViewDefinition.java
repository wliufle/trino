/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.spi.connector;

import io.trino.spi.type.TypeId;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import static io.trino.spi.connector.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class ConnectorMaterializedViewDefinition
{
    private final String originalSql;
    private final Optional<CatalogSchemaTableName> storageTable;
    private final Optional<String> catalog;
    private final Optional<String> schema;
    private final List<Column> columns;
    private final Optional<Duration> gracePeriod;
    private final Optional<String> comment;
    private final Optional<String> owner;
    private final List<CatalogSchemaName> path;
    private final Map<String, Object> properties;

    public ConnectorMaterializedViewDefinition(
            String originalSql,
            Optional<CatalogSchemaTableName> storageTable,
            Optional<String> catalog,
            Optional<String> schema,
            List<Column> columns,
            Optional<Duration> gracePeriod,
            Optional<String> comment,
            Optional<String> owner,
            List<CatalogSchemaName> path,
            Map<String, Object> properties)
    {
        this.originalSql = requireNonNull(originalSql, "originalSql is null");
        this.storageTable = requireNonNull(storageTable, "storageTable is null");
        this.catalog = requireNonNull(catalog, "catalog is null");
        this.schema = requireNonNull(schema, "schema is null");
        this.columns = List.copyOf(requireNonNull(columns, "columns is null"));
        checkArgument(gracePeriod.isEmpty() || !gracePeriod.get().isNegative(), "gracePeriod cannot be negative: %s", gracePeriod);
        this.gracePeriod = gracePeriod;
        this.comment = requireNonNull(comment, "comment is null");
        this.owner = requireNonNull(owner, "owner is null");
        this.path = List.copyOf(path);
        this.properties = requireNonNull(properties, "properties are null");

        if (catalog.isEmpty() && schema.isPresent()) {
            throw new IllegalArgumentException("catalog must be present if schema is present");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns list is empty");
        }
    }

    public String getOriginalSql()
    {
        return originalSql;
    }

    public Optional<CatalogSchemaTableName> getStorageTable()
    {
        return storageTable;
    }

    public Optional<String> getCatalog()
    {
        return catalog;
    }

    public Optional<String> getSchema()
    {
        return schema;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public Optional<Duration> getGracePeriod()
    {
        return gracePeriod;
    }

    public Optional<String> getComment()
    {
        return comment;
    }

    public Optional<String> getOwner()
    {
        return owner;
    }

    public List<CatalogSchemaName> getPath()
    {
        return path;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    @Override
    public String toString()
    {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        joiner.add("originalSql=[" + originalSql + "]");
        storageTable.ifPresent(value -> joiner.add("storageTable=" + value));
        catalog.ifPresent(value -> joiner.add("catalog=" + value));
        schema.ifPresent(value -> joiner.add("schema=" + value));
        joiner.add("columns=" + columns);
        gracePeriod.ifPresent(value -> joiner.add("gracePeriod=" + gracePeriod));
        comment.ifPresent(value -> joiner.add("comment=" + value));
        joiner.add("owner=" + owner);
        joiner.add("properties=" + properties);
        joiner.add(path.stream().map(CatalogSchemaName::toString).collect(joining(", ", "path=(", ")")));
        return getClass().getSimpleName() + joiner.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectorMaterializedViewDefinition that = (ConnectorMaterializedViewDefinition) o;
        return Objects.equals(originalSql, that.originalSql) &&
                Objects.equals(storageTable, that.storageTable) &&
                Objects.equals(catalog, that.catalog) &&
                Objects.equals(schema, that.schema) &&
                Objects.equals(columns, that.columns) &&
                Objects.equals(gracePeriod, that.gracePeriod) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(path, that.path) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(originalSql, storageTable, catalog, schema, columns, gracePeriod, comment, owner, path, properties);
    }

    public static final class Column
    {
        private final String name;
        private final TypeId type;
        private final Optional<String> comment;

        @Deprecated
        public Column(String name, TypeId type)
        {
            this(name, type, Optional.empty());
        }

        public Column(String name, TypeId type, Optional<String> comment)
        {
            this.name = requireNonNull(name, "name is null");
            this.type = requireNonNull(type, "type is null");
            this.comment = requireNonNull(comment, "comment is null");
        }

        public String getName()
        {
            return name;
        }

        public TypeId getType()
        {
            return type;
        }

        public Optional<String> getComment()
        {
            return comment;
        }

        @Override
        public String toString()
        {
            return name + " " + type;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Column column = (Column) o;
            return Objects.equals(name, column.name) &&
                    Objects.equals(type, column.type) &&
                    Objects.equals(comment, column.comment);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, type, comment);
        }
    }
}
