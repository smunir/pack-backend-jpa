$output.java($JaxioCommons, "HibernateSearchUtil")##

$output.require("org.apache.lucene.search.Query")##
$output.require("org.hibernate.search.jpa.FullTextEntityManager")##
$output.require("org.hibernate.search.jpa.FullTextQuery")##
$output.require("org.slf4j.Logger")##
$output.require("org.slf4j.LoggerFactory")##

$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.EntityManager")##
$output.require("javax.persistence.PersistenceContext")##
$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.io.Serializable")##
$output.require("java.util.List")##

$output.requireStatic("com.google.common.collect.Lists.newArrayList")##
$output.requireStatic("org.hibernate.search.jpa.Search.getFullTextEntityManager")##

@Named
@Singleton
public class $output.currentClass {
    private static final Logger log = LoggerFactory.getLogger(HibernateSearchUtil.class);

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> clazz, SearchParameters sp, List<SingularAttribute<?, ?>> availableProperties) {
        log.info("Searching {} with terms : {} with available Properties: {}", new Object[]{clazz.getSimpleName(), sp.getTerms(), availableProperties});
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(entityManager);
        Query query = sp.getLuceneQueryBuilder().build(fullTextEntityManager, sp, availableProperties);

        if (query == null) {
            return null;
        }

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery( //
                query, clazz);
        if (sp.getMaxResults() > 0) {
            ftq.setMaxResults(sp.getMaxResults());
        }
        return ftq.getResultList();
    }

    /*
     * Same as {@link \#find(Class, SearchParameters, List)} but will return only the id
     */
    @SuppressWarnings("unchecked")
    public <T> List<Serializable> findId(Class<T> clazz, SearchParameters sp, List<SingularAttribute<?, ?>> availableProperties) {
        log.info("Searching {} with terms : {} with available Properties: {}", new Object[]{clazz.getSimpleName(), sp.getTerms(), availableProperties});
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(entityManager);
        Query query = sp.getLuceneQueryBuilder().build(fullTextEntityManager, sp, availableProperties);

        if (query == null) {
            return null;
        }

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery( //
                query, clazz);
        ftq.setProjection("id");
        if (sp.getMaxResults() > 0) {
            ftq.setMaxResults(sp.getMaxResults());
        }
        List<Serializable> ids = newArrayList();
        List<Object[]> resultList = ftq.getResultList();
        for (Object[] result : resultList) {
            ids.add((Serializable) result[0]);
        }
        return ids;
    }

}