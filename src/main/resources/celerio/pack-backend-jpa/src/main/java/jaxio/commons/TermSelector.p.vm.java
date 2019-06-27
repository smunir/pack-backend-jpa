$output.java($JaxioCommons, "TermSelector")##

$output.require("javax.persistence.metamodel.SingularAttribute")##
$output.require("java.io.Serializable")##
$output.require("java.util.Arrays")##
$output.require("java.util.List")##

$output.require("static com.google.common.collect.Lists.newArrayList")##
$output.require("static org.apache.commons.lang3.StringUtils.isNotBlank")##

public class $output.currentClass implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PathHolder pathHolder;
    private List<String> selected = newArrayList();
    private boolean orMode = true;

    public TermSelector() {
        this.pathHolder = null;
    }

    public TermSelector(SingularAttribute<?, ?> attribute) {
        this.pathHolder = new PathHolder(attribute);
    }

    public SingularAttribute<?, ?> getAttribute() {
        return pathHolder != null ? (SingularAttribute<?, ?>) pathHolder.getAttributes().get(0) : null;
    }

    public boolean isOrMode() {
        return orMode;
    }

    public void setOrMode(boolean orMode) {
        this.orMode = orMode;
    }

    public TermSelector or() {
        setOrMode(true);
        return this;
    }

    public TermSelector and() {
        setOrMode(false);
        return this;
    }

    /*
     * Get the possible candidates for property.
     */
    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = newArrayList(selected);
    }

    /*
     * Set the possible candidates for property.
     */
    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public TermSelector selected(String... selected) {
        setSelected(newArrayList(selected));
        return this;
    }

    public boolean isNotEmpty() {
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        for (String word : selected) {
            if (isNotBlank(word)) {
                return true;
            }
        }
        return false;
    }

    public void clearSelected() {
        if (selected != null) {
            selected.clear();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (selected != null) {
            s.append("term");
            if (selected.size() > 1) {
                s.append('s');
            }
            s.append(": ");
            s.append(Arrays.toString(selected.toArray()));
        }
        if (pathHolder != null) {
            if (s.length() > 0) {
                s.append(' ');
            }
            s.append("on ");
            s.append(pathHolder.getPath());
        }
        return s.toString();
    }
}