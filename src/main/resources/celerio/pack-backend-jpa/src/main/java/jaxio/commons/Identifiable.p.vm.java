$output.java($JaxioCommons, "Identifiable")##

$output.require("java.io.Serializable")##

/**
 * By making entities implement this interface we can easily retrieve from the
 * {@link GenericRepository} the identifier property of the entity.
 */

public interface $output.currentClass<PK extends Serializable> {

    /**
     * @return the primary key
     */
    PK getId();

    /**
     * Sets the primary key
     *
     * @param id primary key
     */
    void setId(PK id);

    /**
     * Helper method to know whether the primary key is set or not.
     *
     * @return true if the primary key is set, false otherwise
     */
    boolean isIdSet();
}