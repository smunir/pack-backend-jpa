$output.java($JaxioCommons, "ByPatternUtil")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.EntityManager")##
$output.require("javax.persistence.PersistenceContext")##
$output.require("javax.persistence.criteria.CriteriaBuilder")##
$output.require("javax.persistence.criteria.Expression")##
$output.require("javax.persistence.criteria.Predicate")##
$output.require("javax.persistence.criteria.Root")##
$output.require("javax.persistence.metamodel.EntityType")##
$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.util.List")##

$output.requireStatic("com.google.common.collect.Lists.newArrayList")##
$output.requireStatic("javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE")##
$output.requireStatic("javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE")##

@Named
@Singleton
public class $output.currentClass {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private JpaUtil jpaUtil;

    /*
     * Lookup entities having at least one String attribute matching the passed sp's pattern
     */
    @SuppressWarnings("unchecked")
    public <T> Predicate byPattern(Root<T> root, CriteriaBuilder builder, SearchParameters sp, Class<T> type) {
        if (!sp.hasSearchPattern()) {
            return null;
        }

        List<Predicate> predicates = newArrayList();
        EntityType<T> entity = em.getMetamodel().entity(type);
        String pattern = sp.getSearchPattern();

        for (SingularAttribute<? super T, ?> attr : entity.getSingularAttributes()) {
            if (attr.getPersistentAttributeType() == MANY_TO_ONE || attr.getPersistentAttributeType() == ONE_TO_ONE) {
                continue;
            }

            if (attr.getJavaType() == String.class) {
                predicates.add(jpaUtil.stringPredicate((Expression<String>) root.get(jpaUtil.attribute(entity, attr)), pattern, sp, builder));
            }
        }

        return jpaUtil.orPredicate(builder, predicates);
    }
}