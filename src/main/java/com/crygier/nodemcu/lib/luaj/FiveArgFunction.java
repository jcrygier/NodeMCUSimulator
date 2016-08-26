package com.crygier.nodemcu.lib.luaj;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

/** Abstract base class for Java function implementations that take one argument and
 * return one value.
 * <p>
 * Subclasses need only implement {@link LuaValue#call(LuaValue)} to complete this class,
 * simplifying development.
 * All other uses of {@link #call()}, {@link #invoke(Varargs)},etc,
 * are routed through this method by this class,
 * dropping or extending arguments with {@code nil} values as required.
 * <p>
 * If more than one argument are required, or no arguments are required,
 * or variable argument or variable return values,
 * then use one of the related function
 * {@link ZeroArgFunction}, {@link TwoArgFunction}, {@link ThreeArgFunction}, or {@link VarArgFunction}.
 * <p>
 * See {@link LibFunction} for more information on implementation libraries and library functions.
 * @see #call(LuaValue)
 * @see LibFunction
 * @see ZeroArgFunction
 * @see TwoArgFunction
 * @see ThreeArgFunction
 * @see VarArgFunction
 */
abstract public class FiveArgFunction extends LibFunction {

    abstract public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5);

    /** Default constructor */
    public FiveArgFunction() {
    }

    public final LuaValue call() {
        return call(NIL, NIL, NIL, NIL, NIL);
    }

    public final LuaValue call(LuaValue arg) {
        return call(arg, NIL, NIL, NIL, NIL);
    }

    public final LuaValue call(LuaValue arg1, LuaValue arg2) {
        return call(arg1, arg2, NIL, NIL, NIL);
    }

    public final LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        return call(arg1, arg2, arg3, NIL, NIL);
    }

    public final LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4) {
        return call(arg1, arg2, arg3, arg4, NIL);
    }

    public Varargs invoke(Varargs varargs) {
        return call(varargs.arg1(),varargs.arg(2),varargs.arg(3),varargs.arg(4),varargs.arg(5));
    }
}
