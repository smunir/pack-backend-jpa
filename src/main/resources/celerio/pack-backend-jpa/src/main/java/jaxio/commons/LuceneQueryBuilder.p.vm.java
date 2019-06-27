$output.java($JaxioCommons, "LuceneQueryBuilder")##

$output.require("org.apache.lucene.search.Query")##
$output.require("org.hibernate.search.jpa.FullTextEntityManager")##

$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.io.Serializable")##
$output.require("java.util.List")##

public interface $output.currentClass extends Serializable {

    Query build(FullTextEntityManager fullTextEntityManager, SearchParameters searchParameters, List<SingularAttribute<?, ?>> availableProperties);
}