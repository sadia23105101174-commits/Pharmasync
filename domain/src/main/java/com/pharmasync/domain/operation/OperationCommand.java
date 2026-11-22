package com.pharmasync.domain.operation;

/**
 * Command pattern interface representing an executable operation.
 * Encapsulates the execution logic and the underlying Operation audit record.
 */
public interface OperationCommand<C> {
    /**
     * Returns the Operation metadata record.
     */
    Operation getOperation();

    /**
     * Executes the business logic encapsulated by this command against the given execution context.
     *
     * @param context execution context (e.g. repository, inventory service)
     */
    void execute(C context);
}
