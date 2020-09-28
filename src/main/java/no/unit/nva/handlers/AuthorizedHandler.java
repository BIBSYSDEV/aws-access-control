package no.unit.nva.handlers;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.Tag;
import java.util.List;
import java.util.Optional;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import org.slf4j.Logger;

public abstract class AuthorizedHandler<I, O> extends ApiGatewayHandler<I, O> {

    public static final String ASSUMED_ROLE_ARN_ENV_VAR = "ASSUMED_ROLE_ARN";
    public static final String UNIDENTIFIED_SESSION = "Unidentified session";
    private final AWSSecurityTokenService stsClient;

    protected AuthorizedHandler(Class<I> iclass,
                             Environment environment,
                             AWSSecurityTokenService stsClient,
                             Logger logger
    ) {
        super(iclass, environment, logger);
        this.stsClient = stsClient;
    }

    @Override
    protected final O processInput(I input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        STSAssumeRoleSessionCredentialsProvider credentialsProvider =
            new STSAssumeRoleSessionCredentialsProvider.Builder(assumedRoleArn(), session(context))
                .withSessionTags(sessionTags(requestInfo))
                .withStsClient(stsClient).build();

        return processInput(input, requestInfo, credentialsProvider, context);
    }

    protected abstract O processInput(I input,
                                      RequestInfo requestInfo,
                                      STSAssumeRoleSessionCredentialsProvider credentialsProvider,
                                      Context context) throws ApiGatewayException;

    protected abstract List<Tag> sessionTags(RequestInfo requestInfo);

    protected String assumedRoleArn() {
        return environment.readEnv(ASSUMED_ROLE_ARN_ENV_VAR);
    }

    private String session(Context context) {
        return Optional.ofNullable(context).map(Context::getAwsRequestId).orElse(UNIDENTIFIED_SESSION);
    }
}
