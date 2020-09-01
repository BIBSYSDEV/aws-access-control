package no.unit.nva.handlers.authorizer;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import nva.commons.exceptions.ForbiddenException;
import nva.commons.handlers.authentication.RequestAuthorizer;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.aws.SecretsReader;

public class LambdaAuthorizer extends RequestAuthorizer {

    public static final String DEFAULT_PRINCIPAL_ID = "ServiceAccessingUsersAndRoles";
    public static final String AWS_SECRET_NAME_ENV_VAR = "API_SECRET_NAME";
    public static final String AWS_SECRET_KEY_ENV_VAR = "API_SECRET_KEY";
    private final AWSSecretsManager awsSecretsManager;

    @JacocoGenerated
    public LambdaAuthorizer() {
        this(newAwsSecretsManager(), new Environment());
    }

    public LambdaAuthorizer(AWSSecretsManager awsSecretsManager, Environment environment) {
        super(environment);
        this.awsSecretsManager = awsSecretsManager;
    }

    @Override
    protected String principalId() {
        return DEFAULT_PRINCIPAL_ID;
    }

    @Override
    protected String fetchSecret() throws ForbiddenException {
        final String secretName = environment.readEnv(AWS_SECRET_NAME_ENV_VAR);
        final String secretKey = environment.readEnv(AWS_SECRET_KEY_ENV_VAR);
        SecretsReader secretsReader = new SecretsReader(awsSecretsManager);
        return secretsReader.fetchSecret(secretName, secretKey);
    }

    @JacocoGenerated
    private static AWSSecretsManager newAwsSecretsManager() {
        return AWSSecretsManagerClientBuilder.defaultClient();
    }
}
