package no.unit.nva.database;

import static java.util.Objects.requireNonNull;
import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import nva.commons.utils.Environment;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DatabaseServiceWithTableNameOverride implements DatabaseService {

    public static final String DYNAMO_DB_CLIENT_NOT_SET_ERROR = "DynamoDb client has not been set";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceWithTableNameOverride.class);

    /**
     * DynamoDbMapper requires the classes to contain an {@code @DynamoDBTable} annotation. To allow more flexibility
     * regarding the table name we expect the table name to be provided by an environment variable. This method allows
     * us to override the hard-coded table name given in the model class definitions.
     *
     * @param dynamoDbClient any AmazonDynamoDB client implementation.
     * @param environment    environment containing the table name.
     * @return a DynamoDBMapper that is connected to the table specified by the env variable.
     */
    public static DynamoDBMapper createMapperOverridingHardCodedTableName(AmazonDynamoDB dynamoDbClient,
                                                                          Environment environment) {
        String tableName = environment.readEnv(USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE);
        attempt(() -> requireNonNull(dynamoDbClient))
            .orElseThrow(DatabaseServiceWithTableNameOverride::logErrorAndThrowException);
        DynamoDBMapperConfig dynamoDbMapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new TableNameOverride(tableName))
            .build();
        return new DynamoDBMapper(dynamoDbClient, dynamoDbMapperConfig);
    }

    private static RuntimeException logErrorAndThrowException(Failure<AmazonDynamoDB> failure) {
        logger.error(DYNAMO_DB_CLIENT_NOT_SET_ERROR);
        throw new RuntimeException(failure.getException());
    }
}
