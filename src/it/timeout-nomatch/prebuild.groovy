import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Method
import java.util.Map

NanoHTTPD server = new NanoHTTPD(8080) {
  @Override
  public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> params, Map<String, String> files) {
    return new Response("Bye world!")
  }
}

server.start()
context.put("server", server)
