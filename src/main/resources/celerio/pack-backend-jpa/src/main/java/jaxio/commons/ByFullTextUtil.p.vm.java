$output.java($JaxioCommons, "ByFullTextUtil")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.Embeddable")##
$output.require("javax.persistence.EntityManager")##
$output.require("javax.persistence.PersistenceContext")##
$output.require("javax.persistence.criteria.CriteriaBuilder")##
$output.require("javax.persistence.criteria.Path")##
$output.require("javax.persistence.criteria.Predicate")##
$output.require("javax.persistence.criteria.Root")##
$output.require("javax.persistence.metamodel.ManagedType")##
$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.io.Serializable")##
$output.require("java.util.List")##

$output.requireStatic("com.google.common.collect.Lists.newArrayList")##
$output.requireStatic("java.util.Collections.emptyList")##

/**
 * @author Nicolas Romanetti
 * @author Florent Ramière
 * @author Sébastien Péralta
 * @author Jean-Louis Boudart
 */
@Named
@Singleton
public class $output.currentClass {
    @PersistenceContext
    private EntityManager em;

    @Inject
    protected HibernateSearchUtil hibernateSearchUtil;
    @Inject
    private JpaUtil jpaUtil;

    public <T extends Identifiable<?>> Predicate byFullText(Root<T> root, CriteriaBuilder builder, SearchParameters sp, T entity,
                                                            List<SingularAttribute<?, ?>> indexedAttributes) {
        if (!hasNonEmptyTerms(sp)) {
            return null;
        }

        if (jpaUtil.hasSimplePk(entity)) {
            return onSimplePrimaryKey(root, builder, sp, indexedAttributes);
        } else {
            return onCompositePrimaryKeys(root, builder, sp, indexedAttributes);
        }
    }

    private boolean hasNonEmptyTerms(SearchParameters sp) {
        for (TermSelector termSelector : sp.getTerms()) {
            if (termSelector.isNotEmpty()) {
                return true;
            }
        }
        return false;
    }

    private <T extends Identifiable<?>> Predicate onCompositePrimaryKeys(Root<T> root, CriteriaBuilder builder, SearchParameters sp,
                                                                         List<SingularAttribute<?, ?>> properties) {
        List<? extends T> found = hibernateSearchUtil.find(root.getJavaType(), sp, properties);
        if (found == null) {
            return null;
        } else if (found.isEmpty()) {
            return builder.disjunction();
        }

        List<Predicate> predicates = newArrayList();
        for (T t : found) {
            predicates.add(byExampleOnEntity(root, t, sp, builder));
        }
        return jpaUtil.concatPredicate(sp, builder, jpaUtil.orPredicate(builder, predicates));
    }

    private <T> Predicate onSimplePrimaryKey(Root<T> root, CriteriaBuilder builder, SearchParameters sp, List<SingularAttribute<?, ?>> properties) {
        List<Serializable> ids = hibernateSearchUtil.findId(root.getJavaType(), sp, properties);
        if (ids == null) {
            return null;
        } else if (ids.isEmpty()) {
            return builder.disjunction();
        }

        return jpaUtil.concatPredicate(sp, builder, root.get("id").in(ids));
    }

    public <T extends Identifiable<?>> Predicate byExampleOnEntity(Root<T> rootPath, T entityValue, SearchParameters sp, CriteriaBuilder builder) {
        if (entityValue == null) {
            return null;
        }

        Class<T> type = rootPath.getModel().getBindableJavaType();
        ManagedType<T> mt = em.getMetamodel().entity(type);

        List<Predicate> predicates = newArrayList();
        predicates.addAll(byExample(mt, rootPath, entityValue, sp, builder));
        predicates.addAll(byExampleOnCompositePk(rootPath, entityValue, sp, builder));
        return jpaUtil.orPredicate(builder, predicates);
    }

    protected <T extends Identifiable<?>> List<Predicate> byExampleOnCompositePk(Root<T> root, T entity, SearchParameters sp, CriteriaBuilder builder) {
        String compositePropertyName = jpaUtil.compositePkPropertyName(entity);
        if (compositePropertyName == null) {
            return emptyList();
        } else {
            return newArrayList(byExampleOnEmbeddable(root.get(compositePropertyName), entity.getId(), sp, builder));
        }
    }

    public <E> Predicate byExampleOnEmbeddable(Path<E> embeddablePath, E embeddableValue, SearchParameters sp, CriteriaBuilder builder) {
        if (embeddableValue == null) {
            return null;
        }

        Class<E> type = embeddablePath.getModel().getBindableJavaType();
        ManagedType<E> mt = em.getMetamodel().embeddable(type); // note: calling .managedType() does not work
        return jpaUtil.orPredicate(builder, byExample(mt, embeddablePath, embeddableValue, sp, builder));
    }

    /*
     * Add a predicate for each simple property whose value is not null.
     */
    public <T> List<Predicate> byExample(ManagedType<T> mt, Path<T> mtPath, T mtValue, SearchParameters sp, CriteriaBuilder builder) {
        List<Predicate> predicates = newArrayList();
        for (SingularAttribute<? super T, ?> attr : mt.getSingularAttributes()) {
            if (!isPrimaryKey(mt, attr)) {
                continue;
            }

            Object attrValue = jpaUtil.getValue(mtValue, attr);
            if (attrValue != null) {
                predicates.add(builder.equal(mtPath.get(jpaUtil.attribute(mt, attr)), attrValue));
            }
        }
        return predicates;
    }

    private <T> boolean isPrimaryKey(ManagedType<T> mt, SingularAttribute<? super T, ?> attr) {
        if (mt.getJavaType().getAnnotation(Embeddable.class) != null) {
            return true;
        }
        return jpaUtil.isPk(mt, attr);
    }
}