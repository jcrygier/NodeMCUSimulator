package com.crygier.nodemcu.emu;

import com.crygier.nodemcu.emu.http.HttpRequest;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.Arrays;

import static com.crygier.nodemcu.util.LuaFunctionUtil.*;

public class Http extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable http = new LuaTable();

        http.set("delete", fourArgFunction(this::delete));
        http.set("get", fourArgFunction(this::get));
        http.set("post", fourArgFunction(this::post));
        http.set("put", fourArgFunction(this::put));
        http.set("request", fiveArgFunction(this::request));

        env.set("http", http);
        env.get("package").get("loaded").set("http", http);

        return http;
    }

    /**
     * Executes a HTTP DELETE request. Note that concurrent requests are not supported.
     *
     * @param args Parameters
     *             1) url: The URL to fetch, including the http:// or https:// prefix
     *             2) headers: Optional additional headers to append, including \r\n; may be nil
     *             3) body: The body to post; must already be encoded in the appropriate format, but may be empty
     *             4) callback: The callback function to be invoked when the response has been received; it is invoked with the arguments status_code and body
     * @return LuaValue.NIL
     */
    private LuaValue delete(Varargs args) {
        return parseArgsAndDoRequest("DELETE", args);
    }

    /**
     * Executes a HTTP GET request. Note that concurrent requests are not supported.
     *
     * @param args Parameters
     *             1) url: The URL to fetch, including the http:// or https:// prefix
     *             2) headers: Optional additional headers to append, including \r\n; may be nil
     *             3) callback: The callback function to be invoked when the response has been received; it is invoked with the arguments status_code and body
     * @return LuaValue.NIL
     */
    private LuaValue get(Varargs args) {
        return parseArgsAndDoRequest("GET", args);
    }

    /**
     * Executes a HTTP POST request. Note that concurrent requests are not supported.
     *
     * @param args Parameters
     *             1) url: The URL to fetch, including the http:// or https:// prefix
     *             2) headers: Optional additional headers to append, including \r\n; may be nil
     *             3) body: The body to post; must already be encoded in the appropriate format, but may be empty
     *             4) callback: The callback function to be invoked when the response has been received; it is invoked with the arguments status_code and body
     * @return LuaValue.NIL
     */
    private LuaValue post(Varargs args) {
        return parseArgsAndDoRequest("POST", args);
    }

    /**
     * Executes a HTTP PUT request. Note that concurrent requests are not supported.
     *
     * @param args Parameters
     *             1) url: The URL to fetch, including the http:// or https:// prefix
     *             2) headers: Optional additional headers to append, including \r\n; may be nil
     *             3) body: The body to post; must already be encoded in the appropriate format, but may be empty
     *             4) callback: The callback function to be invoked when the response has been received; it is invoked with the arguments status_code and body
     * @return LuaValue.NIL
     */
    private LuaValue put(Varargs args) {
        return parseArgsAndDoRequest("PUT", args);
    }

    /**
     * Execute a custom HTTP request for any HTTP method. Note that concurrent requests are not supported.
     *
     * @param args Parameters
     *             1) url: The URL to fetch, including the http:// or https:// prefix
     *             2) method: The HTTP method to use, e.g. "GET", "HEAD", "OPTIONS" etc
     *             3) headers: Optional additional headers to append, including \r\n; may be nil
     *             4) body: The body to post; must already be encoded in the appropriate format, but may be empty
     *             5) callback: The callback function to be invoked when the response has been received; it is invoked with the arguments status_code and body
     * @return LuaValue.NIL
     */
    private LuaValue request(Varargs args) {
        return parseArgsAndDoRequest(null, args);
    }

    private LuaValue parseArgsAndDoRequest(String method, Varargs args) {
        if (method == null) {
            String urlArg = args.arg(1).toString();
            String methodArg = args.arg(2).toString();
            String extraHeadersArg = args.arg(3).toString();
            String bodyArg = args.arg(4).toString();
            LuaClosure callbackArg = (LuaClosure) args.arg(5);
            (new HttpRequest(methodArg, urlArg, extraHeadersArg, bodyArg)).execute(callbackArg);
        }

        if (Arrays.asList("PUT", "POST", "DELETE", "PATCH").contains(method)) {
            String urlArg = args.arg(1).toString();
            String extraHeadersArg = args.arg(2).toString();
            String bodyArg = args.arg(3).toString();
            LuaClosure callbackArg = (LuaClosure) args.arg(4);
            (new HttpRequest(method, urlArg, extraHeadersArg, bodyArg)).execute(callbackArg);
        }

        if (Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE").contains(method)) {
            String urlArg = args.arg(1).toString();
            String extraHeadersArg = args.arg(2).toString();
            LuaClosure callbackArg = (LuaClosure) args.arg(3);
            (new HttpRequest(method, urlArg, extraHeadersArg, null)).execute(callbackArg);
        }
        return NIL;
    }
}