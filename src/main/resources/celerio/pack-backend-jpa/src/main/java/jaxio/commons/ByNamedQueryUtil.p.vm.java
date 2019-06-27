$output.java($JaxioCommons, "ByNamedQueryUtil")##

$output.require("org.slf4j.Logger")##
$output.require("org.slf4j.LoggerFactory")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.EntityManager")##
$output.require("javax.persistence.PersistenceContext")##
$output.require("javax.persistence.Query")##
$output.require("java.util.List")##
$output.require("java.util.Map")##
$output.require("java.util.Map.Entry")##

/**
 * Helper class to create named query supporting dynamic sort order and pagination.
 */
@Named
@Singleton
public class $output.currentClass {
    private static final Logger log = LoggerFactory.getLogger(ByNamedQueryUtil.class);

    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private JpaUtil jpaUtil;

    public ByNamedQueryUtil() {
    }

    public ByNamedQueryUtil(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public <T> List<T> findByNamedQuery(SearchParameters sp) {
        if (sp == null || !sp.hasNamedQuery()) {
            throw new IllegalArgumentException("searchParameters must be non null and must have a namedQuery");
        }

        Query query = entityManager.createNamedQuery(sp.getNamedQuery());
        String queryString = getQueryString(query);

        // append order by if needed
        if (queryString != null && sp.hasOrders()) {
            // create the sql restriction clausis
            StringBuilder orderClausis = new StringBuilder("order by ");
            boolean first = true;
            for (OrderBy orderBy : sp.getOrders()) {
                if (!first) {
                    orderClausis.append(", ");
                }
                orderClausis.append(orderBy.getPath());
                orderClausis.append(orderBy.isOrderDesc() ? " desc" : " asc");
                first = false;
            }

            log.debug("appending: [{}] to {}", orderClausis, queryString);

            query = recreateQuery(query, queryString + " " + orderClausis.toString());
        }

        // pagination
        jpaUtil.applyPagination(query, sp);

        // named parameters
        setQueryParameters(query, sp);

        // execute
        @SuppressWarnings("unchecked")
        List<T> result = query.getResultList();

        if (result != null) {
            log.debug("{} returned a List of size: {}", sp.getNamedQuery(), result.size());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T byNamedQuery(SearchParameters sp) {
        return (T) objectByNamedQuery(sp);
    }

    public Number numberByNamedQuery(SearchParameters sp) {
        return (Number) objectByNamedQuery(sp);
    }

    public Object objectByNamedQuery(SearchParameters sp) {
        if (sp == null || !sp.hasNamedQuery()) {
            throw new IllegalStateException("Invalid search template provided: could not determine which namedQuery to use");
        }

        Query query = entityManager.createNamedQuery(sp.getNamedQuery());
        String queryString = getQueryString(query);

        // append select count if needed
        if (queryString != null && queryString.toLowerCase().startsWith("from") && !queryString.toLowerCase().contains("count(")) {
            query = recreateQuery(query, "select count(*) " + queryString);
        }

        setQueryParameters(query, sp);

        log.debug("objectNamedQuery : {}", sp.toString());

        // execute
        Object result = query.getSingleResult();

        if (log.isDebugEnabled()) {
            log.debug("{} returned a {} object", sp.getNamedQuery(), result == null ? "null" : result.getClass());
            if (result instanceof Number) {
                log.debug("{} returned a number with value : {}", sp.getNamedQuery(), result);
            }
        }

        return result;
    }

    private void setQueryParameters(Query query, SearchParameters sp) {
        // add parameters for the named query
        for (Entry<String, Object> entrySet : sp.getNamedQueryParameters().entrySet()) {
            query.setParameter(entrySet.getKey(), entrySet.getValue());
        }
    }

    /**
     * If the named query has the "query" hint, it uses the hint value (which must be jpa QL) to create a new query and append to it the proper order by clause.
     */
    private String getQueryString(Query query) {
        Map<String, Object> hints = query.getHints();
        return hints != null ? (String) hints.get("query") : null;
    }

    private Query recreateQuery(Query current, String newSqlString) {
        Query result = entityManager.createQuery(newSqlString);
        for (Entry<String, Object> hint : current.getHints().entrySet()) {
            result.setHint(hint.getKey(), hint.getValue());
        }
        return result;
    }
}