$output.java($JaxioCommons, "MetamodelUtil")##

$output.require("com.google.common.base.Splitter")##
$output.require("org.springframework.context.annotation.Lazy")##

$output.require("javax.inject.Named")##
$output.require("javax.inject.Singleton")##
$output.require("javax.persistence.*")##
$output.require("javax.persistence.metamodel.Attribute")##
$output.require("javax.persistence.metamodel.PluralAttribute")##
$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.lang.reflect.AccessibleObject")##
$output.require("java.lang.reflect.Field")##
$output.require("java.util.Collection")##
$output.require("java.util.List")##
$output.require("java.util.Map")##

$output.requireStatic("com.google.common.collect.Lists.newArrayList")##
$output.requireStatic("com.google.common.collect.Maps.newHashMap")##

@Named
@Singleton
@Lazy(false)
public class $output.currentClass {
    private static MetamodelUtil instance;

    public static MetamodelUtil getInstance() {
        return instance;
    }

    private Map<Class<?>, Class<?>> metamodelCache = newHashMap();

    public MetamodelUtil() {
        instance = this;
    }

    public SingularAttribute<?, ?> toAttribute(String property, Class<?> from) {
        try {
            Class<?> metamodelClass = getCachedClass(from);
            Field field = metamodelClass.getField(property);
            return (SingularAttribute<?, ?>) field.get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Attribute<?, ?>> toAttributes(String path, Class<?> from) {
        try {
            List<Attribute<?, ?>> attributes = newArrayList();
            Class<?> current = from;
            for (String pathItem : Splitter.on(".").split(path)) {
                Class<?> metamodelClass = getCachedClass(current);
                Field field = metamodelClass.getField(pathItem);
                Attribute<?, ?> attribute = (Attribute<?, ?>) field.get(null);
                attributes.add(attribute);
                if (attribute instanceof PluralAttribute) {
                    current = ((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType();
                } else {
                    current = attribute.getJavaType();
                }
            }
            return attributes;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String toPath(List<Attribute<?, ?>> attributes) {
        StringBuilder path = new StringBuilder();
        for (Attribute<?, ?> attribute : attributes) {
            if (path.length() > 0) {
                path.append(".");
            }
            path.append(attribute.getName());
        }
        return path.toString();
    }

    private Class<?> getCachedClass(Class<?> current) throws ClassNotFoundException {
        if (metamodelCache.containsKey(current)) {
            return metamodelCache.get(current);
        }
        Class<?> metamodelClass = Class.forName(current.getName() + "_");
        metamodelCache.put(current, metamodelClass);
        return metamodelClass;
    }

    /**
     * Retrieves cascade from metamodel attribute
     *
     * @param attribute given pluaral attribute
     * @return an empty collection if no jpa relation annotation can be found.
     */
    public Collection<CascadeType> getCascades(PluralAttribute<?, ?, ?> attribute) {
        if (attribute.getJavaMember() instanceof AccessibleObject) {
            AccessibleObject accessibleObject = (AccessibleObject) attribute.getJavaMember();
            OneToMany oneToMany = accessibleObject.getAnnotation(OneToMany.class);
            if (oneToMany != null) {
                return newArrayList(oneToMany.cascade());
            }
            ManyToMany manyToMany = accessibleObject.getAnnotation(ManyToMany.class);
            if (manyToMany != null) {
                return newArrayList(manyToMany.cascade());
            }
        }
        return newArrayList();
    }

    /**
     * Retrieves cascade from metamodel attribute on a xToMany relation.
     *
     * @param attribute given singular attribute
     * @return an empty collection if no jpa relation annotation can be found.
     */
    public Collection<CascadeType> getCascades(SingularAttribute<?, ?> attribute) {
        if (attribute.getJavaMember() instanceof AccessibleObject) {
            AccessibleObject accessibleObject = (AccessibleObject) attribute.getJavaMember();
            OneToOne oneToOne = accessibleObject.getAnnotation(OneToOne.class);
            if (oneToOne != null) {
                return newArrayList(oneToOne.cascade());
            }
            ManyToOne manyToOne = accessibleObject.getAnnotation(ManyToOne.class);
            if (manyToOne != null) {
                return newArrayList(manyToOne.cascade());
            }
        }
        return newArrayList();
    }

    public boolean isOrphanRemoval(PluralAttribute<?, ?, ?> attribute) {
        if (attribute.getJavaMember() instanceof AccessibleObject) {
            AccessibleObject accessibleObject = (AccessibleObject) attribute.getJavaMember();
            OneToMany oneToMany = accessibleObject.getAnnotation(OneToMany.class);
            if (oneToMany != null) {
                return oneToMany.orphanRemoval();
            }
        }
        return true;
    }
}
