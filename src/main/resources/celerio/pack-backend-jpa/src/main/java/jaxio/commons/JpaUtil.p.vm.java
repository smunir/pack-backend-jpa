$output.java($JaxioCommons, "JpaUtil")##

$output.require("org.apache.commons.beanutils.MethodUtils")##
$output.require("org.apache.commons.beanutils.PropertyUtils")##
$output.require("org.apache.commons.text.WordUtils")##
$output.require("org.springframework.context.annotation.Lazy")##
$output.require("org.springframework.context.i18n.LocaleContextHolder")##

$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.*")##
$output.require("javax.persistence.criteria.*")##
$output.require("javax.persistence.metamodel.Attribute")##
$output.require("javax.persistence.metamodel.ManagedType")##
$output.require("javax.persistence.metamodel.PluralAttribute")##
$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.beans.PropertyDescriptor")##
$output.require("java.lang.reflect.Field")##
$output.require("java.lang.reflect.Method")##
$output.require("java.util.List")##
$output.require("java.util.Map")##

$output.require("static com.google.common.base.Predicates.notNull")##
$output.require("static com.google.common.base.Throwables.propagate")##
$output.require("static com.google.common.collect.Iterables.filter")##
$output.require("static com.google.common.collect.Iterables.toArray")##
$output.require("static com.google.common.collect.Lists.newArrayList")##
$output.require("static com.google.common.collect.Maps.newHashMap")##
$output.require("static java.lang.reflect.Modifier.isPublic")##
$output.require("static org.apache.commons.lang3.StringUtils.isBlank")##
$output.require("static org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy")##

@Named
@Singleton
@Lazy(false)
public class $output.currentClass {

    private Map<Class<?>, String> compositePkCache = newHashMap();
    private static JpaUtil instance;

    public static JpaUtil getInstance() {
        return instance;
    }

    public JpaUtil() {
        instance = this;
    }

    public boolean isEntityIdManuallyAssigned(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (isPrimaryKey(method)) {
                return isManuallyAssigned(method);
            }
        }
        return false; // no pk found, should not happen
    }

    private boolean isPrimaryKey(Method method) {
        return isPublic(method.getModifiers()) && (method.getAnnotation(Id.class) != null || method.getAnnotation(EmbeddedId.class) != null);
    }

    private boolean isManuallyAssigned(Method method) {
        if (method.getAnnotation(Id.class) != null) {
            return method.getAnnotation(GeneratedValue.class) == null;
        }

        return method.getAnnotation(EmbeddedId.class) != null;
    }

    public Predicate concatPredicate(SearchParameters sp, CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return concatPredicate(sp, builder, newArrayList(predicatesNullAllowed));
    }

    public Predicate concatPredicate(SearchParameters sp, CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        if (sp.isAndMode()) {
            return andPredicate(builder, predicatesNullAllowed);
        } else {
            return orPredicate(builder, predicatesNullAllowed);
        }
    }

    public Predicate andPredicate(CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return andPredicate(builder, newArrayList(predicatesNullAllowed));
    }

    public Predicate orPredicate(CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return orPredicate(builder, newArrayList(predicatesNullAllowed));
    }

    public Predicate andPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = newArrayList(filter(predicatesNullAllowed, notNull()));
        if (predicates == null || predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {
            return builder.and(toArray(predicates, Predicate.class));
        }
    }

    public Predicate orPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = newArrayList(filter(predicatesNullAllowed, notNull()));
        if (predicates == null || predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {
            return builder.or(toArray(predicates, Predicate.class));
        }
    }

    public <E> Predicate stringPredicate(Expression<String> path, Object attrValue, SearchMode searchMode, SearchParameters sp, CriteriaBuilder builder) {
        if (sp.isCaseInsensitive()) {
            path = builder.lower(path);
            attrValue = ((String) attrValue).toLowerCase(LocaleContextHolder.getLocale());
        }

        switch (searchMode != null ? searchMode : sp.getSearchMode()) {
            case EQUALS:
                return builder.equal(path, attrValue);
            case ENDING_LIKE:
                return builder.like(path, "%" + attrValue);
            case STARTING_LIKE:
                return builder.like(path, attrValue + "%");
            case ANYWHERE:
                return builder.like(path, "%" + attrValue + "%");
            case LIKE:
                return builder.like(path, (String) attrValue); // assume user provide the wild cards
            default:
                throw new IllegalStateException("expecting a search mode!");
        }
    }

    public <E> Predicate stringPredicate(Expression<String> path, Object attrValue, SearchParameters sp, CriteriaBuilder builder) {
        return stringPredicate(path, attrValue, null, sp, builder);
    }

    /*
     * Convert the passed propertyPath into a JPA path.
     * <p>
     * Note: JPA will do joins if the property is in an associated entity.
     */
    @SuppressWarnings("unchecked")
    public <E, F> Path<F> getPath(Root<E> root, List<Attribute<?, ?>> attributes) {
        Path<?> path = root;
        for (Attribute<?, ?> attribute : attributes) {
            boolean found = false;
            if (path instanceof FetchParent) {
                for (Fetch<E, ?> fetch : ((FetchParent<?, E>) path).getFetches()) {
                    if (attribute.getName().equals(fetch.getAttribute().getName()) && (fetch instanceof Join<?, ?>)) {
                        path = (Join<E, ?>) fetch;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (attribute instanceof PluralAttribute) {
                    path = ((From<?, ?>) path).join(attribute.getName(), JoinType.LEFT);
                } else {
                    path = path.get(attribute.getName());
                }
            }
        }
        return (Path<F>) path;
    }

    public void verifyPath(Attribute<?, ?>... path) {
        verifyPath(newArrayList(path));
    }

    public void verifyPath(List<Attribute<?, ?>> path) {
        List<Attribute<?, ?>> attributes = newArrayList(path);
        Class<?> from = null;
        if (attributes.get(0).isCollection()) {
            from = ((PluralAttribute) attributes.get(0)).getElementType().getJavaType();
        } else {
            from = attributes.get(0).getJavaType();
        }
        attributes.remove(0);
        for (Attribute<?, ?> attribute : attributes) {
            if (!attribute.getDeclaringType().getJavaType().isAssignableFrom(from)) {
                throw new IllegalStateException("Wrong path.");
            }
            from = attribute.getJavaType();
        }
    }

    public <T extends Identifiable<?>> String compositePkPropertyName(T entity) {
        Class<?> entityClass = entity.getClass();
        if (compositePkCache.containsKey(entityClass)) {
            return compositePkCache.get(entityClass);
        }

        for (Method m : entity.getClass().getMethods()) {
            if (m.getAnnotation(EmbeddedId.class) != null) {
                String propertyName = methodToProperty(m);
                compositePkCache.put(entityClass, propertyName);
                return propertyName;
            }
        }
        for (Field f : entity.getClass().getFields()) {
            if (f.getAnnotation(EmbeddedId.class) != null) {
                String propertyName = f.getName();
                compositePkCache.put(entityClass, propertyName);
                return propertyName;
            }
        }
        compositePkCache.put(entityClass, null);
        return null;
    }

    public <T> boolean isPk(ManagedType<T> mt, SingularAttribute<? super T, ?> attr) {
        try {
            Method m = MethodUtils.getAccessibleMethod(mt.getJavaType(), "get" + WordUtils.capitalize(attr.getName()), (Class<?>) null);
            if (m != null && m.getAnnotation(Id.class) != null) {
                return true;
            }

            Field field = mt.getJavaType().getField(attr.getName());
            return field.getAnnotation(Id.class) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public <T> Object getValue(T example, Attribute<? super T, ?> attr) {
        try {
            if (attr.getJavaMember() instanceof Method) {
                return ((Method) attr.getJavaMember()).invoke(example);
            } else {
                return ((Field) attr.getJavaMember()).get(example);
            }
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    public <T, A> SingularAttribute<? super T, A> attribute(ManagedType<? super T> mt, Attribute<? super T, A> attr) {
        return mt.getSingularAttribute(attr.getName(), attr.getJavaType());
    }

    public <T> SingularAttribute<? super T, String> stringAttribute(ManagedType<? super T> mt, Attribute<? super T, ?> attr) {
        return mt.getSingularAttribute(attr.getName(), String.class);
    }

    public <T extends Identifiable<?>> boolean hasSimplePk(T entity) {
        for (Method m : entity.getClass().getMethods()) {
            if (m.getAnnotation(Id.class) != null) {
                return true;
            }
        }
        for (Field f : entity.getClass().getFields()) {
            if (f.getAnnotation(Id.class) != null) {
                return true;
            }
        }
        return false;
    }

    public String[] toNames(Attribute<?, ?>... attributes) {
        return toNamesList(newArrayList(attributes)).toArray(new String[0]);
    }

    public List<String> toNamesList(List<Attribute<?, ?>> attributes) {
        List<String> ret = newArrayList();
        for (Attribute<?, ?> attribute : attributes) {
            ret.add(attribute.getName());
        }
        return ret;
    }

    public String getEntityName(Identifiable<?> entity) {
        Entity entityAnnotation = entity.getClass().getAnnotation(Entity.class);
        if (isBlank(entityAnnotation.name())) {
            return getClassWithoutInitializingProxy(entity).getSimpleName();
        }
        return entityAnnotation.name();
    }

    public String methodToProperty(Method m) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(m.getDeclaringClass());
        for (PropertyDescriptor pd : pds) {
            if (m.equals(pd.getReadMethod()) || m.equals(pd.getWriteMethod())) {
                return pd.getName();
            }
        }
        return null;
    }

    public Object getValueFromField(Field field, Object object) {
        boolean accessible = field.isAccessible();
        try {
            return getField(field, object);
        } finally {
            field.setAccessible(accessible);
        }
    }

    public void applyPagination(Query query, SearchParameters sp) {
        if (sp.getFirst() > 0) {
            query.setFirstResult(sp.getFirst());
        }
        if (sp.getPageSize() > 0) {
            query.setMaxResults(sp.getPageSize());
        } else if (sp.getMaxResults() > 0) {
            query.setMaxResults(sp.getMaxResults());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Field field, Object target) {
        try {
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}