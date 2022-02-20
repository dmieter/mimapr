
package project.utils.function;

/**
 *
 * @author emelyanov
 */
@FunctionalInterface
public interface ToDoubleTripleFunction <T extends Object, U extends Object, R extends Object>{
    public double applyAsDouble(T t, U u, R r);
}
