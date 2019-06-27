$output.java($JaxioCommons, "OrderByUtil")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.criteria.CriteriaBuilder")##
$output.require("javax.persistence.criteria.Order")##
$output.require("javax.persistence.criteria.Path")##
$output.require("javax.persistence.criteria.Root")##
$output.require("java.util.List")##

$output.requireStatic("com.google.common.collect.Lists.newArrayList")##

/**
 * Helper to create list of {@link javax.persistence.criteria.Order} out of {@link OrderBy}s.
 */
@Named
@Singleton
public class $output.currentClass {

    @Inject
    private JpaUtil jpaUtil;

    public <E> List<Order> buildJpaOrders(Iterable<OrderBy> orders, Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        List<Order> jpaOrders = newArrayList();
        for (OrderBy ob : orders) {
            Path<?> path = jpaUtil.getPath(root, ob.getAttributes());
            jpaOrders.add(ob.isOrderDesc() ? builder.desc(path) : builder.asc(path));
        }
        return jpaOrders;
    }
}