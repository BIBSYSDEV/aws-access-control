package no.unit.nva.database.interfaces;

import static java.util.Objects.isNull;

public abstract class DynamoEntry implements WithType {

    @SuppressWarnings("PMD.ConstantsInInterface")
    public static String FIELD_DELIMITER = "#";

    public abstract String getPrimaryHashKey();

    public abstract String getPrimaryRangeKey();

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public final void setPrimaryRangeKey(String primaryRangeKey) {
        // DO NOTHING.
    }

    @Override
    public abstract String getType();

    /**
     * Do not use. Intented only for use from DynamoDB. This method has no effect as the type is always ROLE.
     *
     * @param type ignored parameter.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public final void setType(String type) {
        // DO NOTHING
    }

    protected boolean primaryKeyHasNotBeenSet() {
        return isNull(getPrimaryHashKey());
    }
}
