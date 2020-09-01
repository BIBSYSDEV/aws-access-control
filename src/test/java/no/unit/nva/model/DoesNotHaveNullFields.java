package no.unit.nva.model;

import static java.util.Objects.isNull;
import static nva.commons.utils.attempt.Try.attempt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class DoesNotHaveNullFields<T> extends BaseMatcher<T> {

    public static final String GETTER_GET_PREFIX = "get";
    public static final String GETTER_IS_PREFIX = "is";

    public static <R> DoesNotHaveNullFields<R> doesNotHaveNullFields() {
        return new DoesNotHaveNullFields<>();
    }

    @Override
    public boolean matches(Object actual) {
        return assertThatNoPublicFieldIsNull(actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("All fields to be non empty");
    }

    private boolean assertThatNoPublicFieldIsNull(Object insertedUser) {
        Method[] methods = insertedUser.getClass().getMethods();
        Stream<MethodInvocationResult> getterInvocations = Arrays.stream(methods)
            .filter(this::isAGetter)
            .map(attempt(method -> invokeMethod(insertedUser, method)))
            .map(attempt -> attempt.orElseThrow(fail -> new RuntimeException(fail.getException())));

        List<MethodInvocationResult> emptyArgs = getterInvocations.filter(this::isEmpty).collect(
            Collectors.toList());
        return emptyArgs.isEmpty();
    }

    private MethodInvocationResult invokeMethod(Object insertedUser, Method method)
        throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(insertedUser);
        return new MethodInvocationResult(method.getName(), result);
    }

    private boolean isAGetter(Method m) {
        return m.getName().startsWith(GETTER_GET_PREFIX) || m.getName().startsWith(GETTER_IS_PREFIX);
    }

    private boolean isEmpty(MethodInvocationResult mir) {
        if (isNull(mir.result)) {
            return true;
        } else {
            if (mir.result instanceof Collection<?>) {
                Collection col = (Collection) mir.result;
                return col.isEmpty();
            } else if (mir.result instanceof Map<?, ?>) {
                Map map = (Map) mir.result;
                return map.isEmpty();
            } else {
                return false;
            }
        }
    }

    private static class MethodInvocationResult {

        public final String methodName;
        public final Object result;

        public MethodInvocationResult(String methodName, Object result) {
            this.methodName = methodName;
            this.result = result;
        }

        public String toString() {
            return this.methodName;
        }
    }
}
