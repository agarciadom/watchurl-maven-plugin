import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Method
import java.util.Map

NanoHTTPD server = new NanoHTTPD(8080) {
  boolean first = true

  @Override
  public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> params, Map<String, String> files) {
    return new Response(true ? "Hello world!" : "Bye world!")
  }
}
context.put("server", server)

new Thread(new Runnable() {
  public void run() {
    Thread.sleep(2000)
    server.start()
  }
}).start()
