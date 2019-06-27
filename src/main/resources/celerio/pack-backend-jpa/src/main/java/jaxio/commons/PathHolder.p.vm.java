$output.java($JaxioCommons, "PathHolder")##

$output.require("javax.persistence.metamodel.Attribute")##
$output.require("java.io.Serializable")##
$output.require("java.util.List")##

$output.require("static com.google.common.base.Preconditions.checkNotNull")##
$output.require("static com.google.common.collect.Lists.newArrayList")##

/**
 * Holder class for path used by the {@link OrderBy}, {@link PropertySelector}, {@link TermSelector} and {@link SearchParameters}.
 */
public class $output.currentClass implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String path;
    private final Class<?> from;
    private transient List<Attribute<?, ?>> attributes;

    public PathHolder(Attribute<?, ?>... attributes) {
        this(newArrayList(attributes));
    }

    public PathHolder(List<Attribute<?, ?>> attributes) {
        JpaUtil.getInstance().verifyPath(checkNotNull(attributes));
        this.attributes = newArrayList(attributes);
        this.path = MetamodelUtil.getInstance().toPath(attributes);
        this.from = attributes.get(0).getDeclaringType().getJavaType();
    }

    public PathHolder(String path, Class<?> from) {
        this.path = path;
        this.from = from;
        // to verify path
        getAttributes();
    }

    public List<Attribute<?, ?>> getAttributes() {
        if (attributes == null) {
            attributes = MetamodelUtil.getInstance().toAttributes(path, from);
        }
        return attributes;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PathHolder other = (PathHolder) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

}