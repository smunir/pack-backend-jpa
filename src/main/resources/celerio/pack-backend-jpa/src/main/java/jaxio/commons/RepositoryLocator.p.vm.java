$output.java($JaxioCommons, "RepositoryLocator")##

$output.require("javax.inject.Inject")##
$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("java.io.Serializable")##
$output.require("java.util.List")##
$output.require("java.util.Map")##

$output.require("static com.google.common.collect.Maps.newHashMap")##
$output.require("static org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy")##

@Named
@Singleton
public class $output.currentClass {
    private Map<Class<?>, GenericRepository<?, ?>> repositories = newHashMap();

    @Inject
    void buildCache(List<GenericRepository<?, ?>> registredRepositories) {
        for (GenericRepository<?, ?> repository : registredRepositories) {
            repositories.put(repository.getType(), repository);
        }
    }

    @SuppressWarnings("unchecked")
    public <PK extends Serializable, E extends Identifiable<PK>> GenericRepository<E, PK> getRepository(Class<? extends E> clazz) {
        return (GenericRepository<E, PK>) repositories.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <PK extends Serializable, E extends Identifiable<PK>> GenericRepository<E, PK> getRepository(E entity) {
        return (GenericRepository<E, PK>) repositories.get(getClassWithoutInitializingProxy(entity));
    }
}