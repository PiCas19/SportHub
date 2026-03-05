package ch.supsi.sporthub.backend.service.api;


/**
 * A generic interface representing a command that takes an input of type {@code T}
 * and returns a result of type {@code R} after execution.
 *
 * <p>This interface follows the Command design pattern, which encapsulates a request as an object,
 * thereby allowing for parameterization of clients with different requests and the queuing or logging of requests.</p>
 *
 * @param <T> the type of the input to the command
 * @param <R> the type of the result returned by the command
 */
public interface ICommand<T, R> {

    /**
     * Executes the command with the specified input.
     *
     * @param input the input data required to execute the command
     * @return the result of the command execution
     */
    R execute(T input);
}