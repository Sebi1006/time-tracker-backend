package de.htwg.cad.exceptions;

/**
 * <h2>TenantNotFoundException</h2>
 *
 * @author aek
 * <p>
 * Description: trigger exception when tenant not found
 */
public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public TenantNotFoundException(String msg) {
        super(msg);
    }
}
