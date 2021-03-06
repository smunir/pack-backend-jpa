$output.java($JaxioCommons, "JpaUniqueUtil")##

$output.require("org.apache.commons.text.WordUtils")##
$output.require("org.springframework.context.i18n.LocaleContextHolder")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.*")##
$output.require("java.lang.reflect.Field")##
$output.require("java.lang.reflect.Method")##
$output.require("java.util.List")##
$output.require("java.util.Map")##

$output.requireStatic("com.google.common.collect.Iterables.concat")##
$output.requireStatic("com.google.common.collect.Lists.newArrayList")##
$output.requireStatic("com.google.common.collect.Maps.newHashMap")##
$output.requireStatic("java.util.Collections.emptyList")##
$output.requireStatic("org.apache.commons.lang3.StringUtils.equalsIgnoreCase")##
$output.requireStatic("org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy")##

@Named
@Singleton
public class $output.currentClass {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private JpaUtil jpaUtil;

    /*
     * Return the error code if the given property is already present in the database, returns null otherwise.
     */
    public String validateSimpleUnique(Identifiable<?> entity, String property, Object value) {
        Map<String, Object> values = newHashMap();
        values.put(property, value);
        return existsInDatabaseOnAllObjects(entity, values) ? simpleUniqueConstraintError(entity, property) : null;
    }

    /*
     * Return a list of error codes for all composite unique and simple unique constraints violations.
     */
    public List<String> validateUniques(Identifiable<?> entity) {
        return newArrayList(concat( //
                validateCompositeUniqueConstraints(entity), //
                validateSimpleUniqueConstraints(entity) //
        ));
    }

    private List<String> validateSimpleUniqueConstraints(Identifiable<?> entity) {
        return newArrayList(concat( //
                validateSimpleUniqueConstraintsDefinedOnMethods(entity), //
                validateSimpleUniqueConstraintsDefinedOnFields(entity)));
    }

    private List<String> validateSimpleUniqueConstraintsDefinedOnFields(Identifiable<?> entity) {
        Class<?> entityClass = getClassWithoutInitializingProxy(entity);
        List<String> errors = newArrayList();
        for (Field field : entityClass.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.unique()) {
                Map<String, Object> values = newHashMap();
                values.put(field.getName(), jpaUtil.getValueFromField(field, entity));
                if (existsInDatabaseOnAllObjects(entity, values)) {
                    errors.add(simpleUniqueConstraintError(entity, field.getName()));
                }
            }
        }
        return errors;
    }

    private List<String> validateSimpleUniqueConstraintsDefinedOnMethods(Identifiable<?> entity) {
        Class<?> entityClass = getClassWithoutInitializingProxy(entity);
        List<String> errors = newArrayList();
        for (Method method : entityClass.getMethods()) {
            Column column = entityClass.getAnnotation(Column.class);
            if (column != null && column.unique()) {
                Map<String, Object> values = newHashMap();
                String property = jpaUtil.methodToProperty(method);
                values.put(property, invokeMethod(method, entity));
                if (existsInDatabaseOnAllObjects(entity, values)) {
                    errors.add(simpleUniqueConstraintError(entity, property));
                }
            }
        }
        return errors;
    }

    private String simpleUniqueConstraintError(Identifiable<?> entity, String property) {
        return WordUtils.uncapitalize(jpaUtil.getEntityName(entity)) + "_" + property + "_already_exists";
    }

    private List<String> validateCompositeUniqueConstraints(Identifiable<?> entity) {
        Class<?> entityClass = getClassWithoutInitializingProxy(entity);
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            return emptyList();
        }
        List<String> errors = newArrayList();
        for (UniqueConstraint uniqueConstraint : table.uniqueConstraints()) {
            if (!checkCompositeUniqueConstraint(entity, entityClass, uniqueConstraint)) {
                errors.add(compositeUniqueConstraintErrorCode(entity, uniqueConstraint));
            }
        }
        return errors;
    }

    private String compositeUniqueConstraintErrorCode(Identifiable<?> entity, UniqueConstraint uniqueConstraint) {
        return WordUtils.uncapitalize(jpaUtil.getEntityName(entity)) + "_"
                + (uniqueConstraint.name() == null ? "composite_unique_constraint_error" : uniqueConstraint.name().toLowerCase());
    }

    private boolean checkCompositeUniqueConstraint(Identifiable<?> entity, Class<?> entityClass, UniqueConstraint u) {
        Map<String, Object> values = newHashMap();
        values.putAll(getPropertyConstraints(entity, entityClass, u, ""));
        return !existsInDatabaseOnAllObjects(entity, values);
    }

    private Map<String, Object> getPropertyConstraints(Object entity, Class<?> entityClass, UniqueConstraint u, String prefix) {
        Map<String, Object> values = newHashMap();
        for (String column : u.columnNames()) {
            Method method = columnNameToMethod(entityClass, column);
            if (method != null) {
                values.put(prefix + jpaUtil.methodToProperty(method), invokeMethod(method, entity));
            } else {
                Field field = columnNameToField(entityClass, column);
                if (field != null) {
                    values.put(prefix + field.getName(), jpaUtil.getValueFromField(field, entity));
                }
            }
        }
        return values;
    }

    private Method columnNameToMethod(Class<?> clazz, String columnName) {
        for (Method method : clazz.getMethods()) {
            Column column = method.getAnnotation(Column.class);
            if (column != null && equalsIgnoreCase(columnName, column.name())) {
                return method;
            }
        }
        return null;
    }

    private Field columnNameToField(Class<?> clazz, String columnName) {
        for (Field field : clazz.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (equalsIgnoreCase(columnName, column.name())) {
                return field;
            }
        }
        return null;
    }

    private boolean existsInDatabaseOnAllObjects(Identifiable<?> entity, Map<String, Object> values) {
        if (entity == null || values == null || values.isEmpty()) {
            return false;
        }
        String entityName = jpaUtil.getEntityName(entity);
        String sqlQuery = "select count(c) from " + entityName + " c where";
        boolean first = true;
        for (Map.Entry<String, Object> property : values.entrySet()) {
            sqlQuery += !first ? " and " : " ";
            if (property.getValue() instanceof String) {
                sqlQuery += "upper(" + property.getKey() + ")=:" + property.getKey();
            } else {
                sqlQuery += property.getKey() + "=:" + property.getKey();
            }
            first = false;
        }
        if (entity.isIdSet()) {
            if (!first) {
                sqlQuery += " and";
            }
            sqlQuery += " id<>:id";
        }
        TypedQuery<Long> query = entityManager.createQuery(sqlQuery, Long.class);
        for (Map.Entry<String, Object> property : values.entrySet()) {
            String propertyName = property.getKey();
            Object value = property.getValue();
            if (value instanceof String) {
                value = ((String) value).toUpperCase(LocaleContextHolder.getLocale());
            }
            query.setParameter(propertyName, value);
        }
        if (entity.isIdSet()) {
            query.setParameter("id", entity.getId());
        }
        return query.getSingleResult() > 0;
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeMethod(Method method, Object target) {
        try {
            return (T) method.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}