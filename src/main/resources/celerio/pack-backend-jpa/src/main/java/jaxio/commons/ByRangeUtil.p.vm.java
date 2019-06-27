$output.java($JaxioCommons, "ByRangeUtil")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.criteria.CriteriaBuilder")##
$output.require("javax.persistence.criteria.Path")##
$output.require("javax.persistence.criteria.Predicate")##
$output.require("javax.persistence.criteria.Root")##
$output.require("java.util.List")##

$output.require("static com.google.common.collect.Lists.newArrayList")##
$output.require("static java.lang.Boolean.FALSE")##
$output.require("static java.lang.Boolean.TRUE")##

/**
 * Helper to create a predicate out of {@link Range}s.
 */
@SuppressWarnings("unchecked")
@Named
@Singleton
public class $output.currentClass {

    @Inject
    private JpaUtil jpaUtil;

    public <E> Predicate byRanges(Root<E> root, CriteriaBuilder builder, SearchParameters sp, Class<E> type) {
        List<Range<?, ?>> ranges = sp.getRanges();
        List<Predicate> predicates = newArrayList();
        for (Range<?, ?> r : ranges) {
            Range<E, ?> range = (Range<E, ?>) r;
            if (range.isSet()) {
                Predicate rangePredicate = buildRangePredicate(range, root, builder);
                if (rangePredicate != null) {
                    predicates.add(rangePredicate);
                }
            }
        }

        return jpaUtil.concatPredicate(sp, builder, predicates);
    }

    private static <D extends Comparable<? super D>, E> Predicate buildRangePredicate(Range<E, D> range, Root<E> root, CriteriaBuilder builder) {
        Predicate rangePredicate = null;
        Path<D> path = JpaUtil.getInstance().getPath(root, range.getAttributes());
        if (range.isBetween()) {
            rangePredicate = builder.between(path, range.getFrom(), range.getTo());
        } else if (range.isFromSet()) {
            rangePredicate = builder.greaterThanOrEqualTo(path, range.getFrom());
        } else if (range.isToSet()) {
            rangePredicate = builder.lessThanOrEqualTo(path, range.getTo());
        }

        if (rangePredicate != null) {
            if (!range.isIncludeNullSet() || range.getIncludeNull() == FALSE) {
                return rangePredicate;
            } else {
                return builder.or(rangePredicate, builder.isNull(path));
            }
        } else {
            // no from/to is set, but include null or not could be:
            if (TRUE == range.getIncludeNull()) {
                return builder.isNull(path);
            } else if (FALSE == range.getIncludeNull()) {
                return builder.isNotNull(path);
            }
        }
        return null;
    }
}